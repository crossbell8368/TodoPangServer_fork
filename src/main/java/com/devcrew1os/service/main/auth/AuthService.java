package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.dto.main.auth.*;
import com.devcrew1os.entity.admin.auth.AdminWithdrawInfo;
import com.devcrew1os.entity.admin.auth.AdminWithdrawStat;
import com.devcrew1os.entity.main.user.UserStat;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserInfoRepository userRepo;
    private final SignupTransaction signupTrans;
    private final LoginTransaction loginTrans;
    private final WithdrawTransaction withdrawTrans;

    private final WithdrawAsync withdrawAsync;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 회원가입
    ===========================*/
    public SignupRes signup(String userId, SignupReq req) {
        LocalDateTime now = LocalDateTime.now();
        SignupRes res = new SignupRes(false, "[Info] Signup initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(userId, req, res)) return res;

        // 2. Id값 검증
        if(!isUserIdExist(userId, res)) return res;

        // 3. 데이터 생성
        UserInfo newUser = createUser(userId, req, now);
        UserStat newUserStat = createStat(userId, now);

        // 4. DB 적용
        signupTrans.saveUserData(newUser, newUserStat, res);

        res.setSuccess(true);
        res.addMessage("[Info] Signup finish");
        logger.info("[AuthService][{}] Signup successful", userId);
        return res;
    }

    private boolean isRequestValid(String userId, SignupReq req, SignupRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getEmail() == null || req.getEmail().isEmpty()) {
            errors.add("[Failed] Email must not be null or empty");
        }
        if (req.getName() == null || req.getName().isEmpty()) {
            errors.add("[Failed] Name must not be null or empty");
        }
        if (req.getSocialType() == null || !UserSocialType.contains(req.getSocialType())) {
            errors.add("[Failed] SocialType must not be null or must be specified value");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected during signupReq: {}", userId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Signup request");
        logger.info("[AuthService][{}] Valid Signup request", userId);
        return true;
    }

    private boolean isUserIdExist(String userId, SignupRes res) {
        if (userRepo.existsUserInfoByUserId(userId)) {
            res.setErrorCode(ErrorCode.DUPLICATE_USER);
            res.addMessage("[Failed] Request UserID already exists");
            logger.warn("[AuthService][{}] Request UserID already exists, at Signup", userId);
            return false;
        }
        res.addMessage("[Success] Valid UserId");
        logger.info("[AuthService][{}] Valid UserId, at Signup", userId);
        return true;
    }

    private UserInfo createUser(String userId, SignupReq req, LocalDateTime now) {
        return UserInfo.builder()
                .userId(userId)
                .userEmail(req.getEmail())
                .userName(req.getName())
                .socialType(req.getSocialType())
                .status(UserStatus.ACTIVE.getValue())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
    }

    private UserStat createStat(String userId, LocalDateTime now) {
        return UserStat.builder()
                .userId(userId)
                .serviceTerm(1)
                .lastLoginAt(now)
                .lastLogoutAt(now)
                .totalRegisteredChallenges(0)
                .totalFinishedChallenges(0)
                .totalSuspendChallenges(0)
                .build();
    }

    /*===========================
       사용자 로그인
    ===========================*/
    public LoginRes login(String userId) {
        LoginRes res = new LoginRes(false, "[Info] Login initiated", ErrorCode.OK);

        // 1. 사용자 통계 정보 fetch
        UserStat userStat = getStatIfExists(userId, res);
        if (userStat == null) return res;

        // 2. 정보 업데이트
        if (!updateStatData(userId, userStat, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Login successful");
        logger.info("[AuthService][{}] Login successful", userId);
        return res;
    }

    private UserStat getStatIfExists(String userId, LoginRes res) {
        try {
            UserStat userStat = loginTrans.getStatByUserId(userId);
            res.addMessage("[Success] User(Stat) fetched");
            logger.info("[AuthService][{}] User(Stat) fetched, at Login", userId);
            return userStat;
        } catch (RuntimeException err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User(Stat) not found");
            logger.warn("[AuthService][{}] User(Stat) not found, at Login", userId);
            return null;
        }
    }

    private boolean updateStatData(String userId, UserStat userStat, LoginRes res) {
        try {
            LocalDateTime now = LocalDateTime.now();

            if (!isNewDay(now, userStat.getLastLoginAt())) {
                res.addMessage("[Success] Stat data already up-to-date");
                logger.info("[AuthService][{}] Stat data already up-to-date for user", userId);
                return true;
            }
            loginTrans.updateStatData(userStat, now);
            res.addMessage("[Success] Successfully updated stat data");
            logger.info("[AuthService][{}] Updated stat data for user", userId);
            return true;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to update stat data");
            logger.error("[AuthService][{}] Failed to update stat data for user: {}", userId, err.getMessage());
            return false;
        }
    }

    private boolean isNewDay(LocalDateTime now, LocalDateTime lastLogin) {
        return now.toLocalDate().isAfter(lastLogin.toLocalDate());
    }

    /*===========================
       사용자 회원탈퇴
    ===========================*/
    public WithdrawRes withdraw(String userId, WithdrawReq req) {
        WithdrawRes res = new WithdrawRes(false, "[Info] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(userId, req, res)) return res;

        // 2. 업데이트 데이터 준비
        WithdrawDTO dto = getWithdrawData(userId, req, res);
        if(dto == null) return res;

        // 3. Firebase & Database 업데이트
        if(!isUserDeleted(userId, dto, res))return res;

        res.setSuccess(true);
        res.addMessage("[Info] Withdraw successful");
        logger.info("[AuthService][{}] Withdraw successful", userId);
        return res;
    }

    private boolean isRequestValid(String userId, WithdrawReq req, WithdrawRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getReason() == null) {
            errors.add("[Failed] Reason must not be null");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected, while withdrawReq: {}", userId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Withdraw request");
        logger.info("[AuthService][{}] Valid Withdraw request", userId);
        return true;
    }

    private WithdrawDTO getWithdrawData(String userId, WithdrawReq req, WithdrawRes res) {
        LocalDateTime now = LocalDateTime.now();

        UserInfo user;
        try {
            user = withdrawTrans.getUsersByUserId(userId);
            user.setStatus(UserStatus.DELETED.getValue());
            user.setDeletedAt(now);
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User not found");
            logger.error("[AuthService][{}] User not found at withdraw process: {}", userId, err.getMessage());
            return null;
        }

        AdminWithdrawInfo reason;
        try {
            reason = withdrawTrans.getWithdrawInfoById(req.getReason());
            reason.setCount(reason.getCount() + 1);
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] Reason not found");
            logger.error("[AuthService][{}] Reason found at withdraw process: {}", userId, err.getMessage());
            return null;
        }
        AdminWithdrawStat status = AdminWithdrawStat.builder()
                .userId(userId)
                .userWithdrawInfo(reason)
                .userWithdrawAt(now)
                .build();
        return WithdrawDTO.builder()
                .user(user)
                .status(status)
                .build();
    }

    private boolean isUserDeleted(String userId, WithdrawDTO dto, WithdrawRes res) {
        boolean status = false;
        StringBuilder firebaseMsg = new StringBuilder();
        StringBuilder databaseMsg = new StringBuilder();

        try {
            CompletableFuture<Boolean> firebaseFuture = withdrawAsync.isUserDeletedFromFirebase(userId, firebaseMsg);
            CompletableFuture<Boolean> databaseFuture = withdrawAsync.isUserDeletedFromDatabase(dto, databaseMsg);
            CompletableFuture.allOf(firebaseFuture, databaseFuture).join();

            Boolean firebaseResult = firebaseFuture.get();
            Boolean databaseResult = databaseFuture.get();

            if(!firebaseResult && !databaseResult) {
                res.setErrorCode(ErrorCode.INTERNAL_ERROR);
                res.addMessage("[Failed] Firebase & database error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase & database: {} \n {}", userId, firebaseMsg.toString(), databaseMsg.toString());
            } else if (!firebaseResult) {
                res.setErrorCode(ErrorCode.FIREBASE_ERROR);
                res.addMessage("[Failed] Firebase error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase: {}", userId, firebaseMsg.toString());
            } else if (!databaseResult) {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] Database error detected");
                logger.error("[AuthService][{}] Failed to delete user from database: {}", userId, databaseMsg.toString());
            } else {
                status = true;
                res.addMessage("[Info] User data deleted");
                logger.info("[AuthService][{}] Withdraw user data deleted: \n {} \n {}", userId, firebaseMsg.toString(), databaseMsg.toString());
            }
            return status;

        } catch (Exception err) {
            res.addMessage("[Failed] Delete process exception");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Withdraw delete process exception: {} \n {} \n {}", userId, err.getMessage(), firebaseMsg.toString(), databaseMsg.toString());
            return false;
        }
    }
}
