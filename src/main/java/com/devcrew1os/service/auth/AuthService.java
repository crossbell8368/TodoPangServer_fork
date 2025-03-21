package com.devcrew1os.service.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.dto.auth.*;
import com.devcrew1os.entity.user.UserStat;
import com.devcrew1os.entity.user.UserInfo;
import com.devcrew1os.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final SignupTransaction signupTrans;
    private final LoginTransaction loginTrans;
    private final WithdrawTransaction withdrawTrans;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 회원가입
    ===========================*/
    public SignupRes signup(SignupReq req) {
        LocalDateTime now = LocalDateTime.now();
        SignupRes res = new SignupRes(false, "[Info] Signup initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. Id값 검증
        if(!isUserIdExist(req, res)) return res;

        // 3. 데이터 생성
        UserInfo newUser = createUser(req, now);
        UserStat newUserStat = createStat(req, now);

        // 4. DB 적용
        signupTrans.saveUserData(newUser, newUserStat, res);

        res.setSuccess(true);
        res.addMessage("[Info] Signup successful");
        logger.info("[AuthService][{}] Signup successful", req.getUserId());
        return res;
    }

    private boolean isRequestValid(SignupReq req, SignupRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getUserId() == null || req.getUserId().isEmpty()) {
            errors.add("[Failed] User ID must not be null or empty");
        }
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
            logger.error("[AuthService][{}] Invalid argument detected during signupReq: {}", req.getUserId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Signup request");
        logger.info("[AuthService][{}] Valid Signup request", req.getUserId());
        return true;
    }

    private boolean isUserIdExist(SignupReq req, SignupRes res) {
        if (userRepo.existsByUserId(req.getUserId())) {
            res.setErrorCode(ErrorCode.DUPLICATE_USER);
            res.addMessage("[Failed] Request UserID already exists");
            logger.warn("[AuthService][{}] Request UserID already exists, at Signup", req.getUserId());
            return false;
        }
        res.addMessage("[Success] Valid UserId");
        logger.info("[AuthService][{}] Valid UserId, at Signup", req.getUserId());
        return true;
    }

    private UserInfo createUser(SignupReq req, LocalDateTime now) {
        return UserInfo.builder()
                .userId(req.getUserId())
                .userEmail(req.getEmail())
                .userName(req.getName())
                .socialType(req.getSocialType())
                .status(UserStatus.ACTIVE.getValue())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
    }

    private UserStat createStat(SignupReq req, LocalDateTime now) {
        return UserStat.builder()
                .userId(req.getUserId())
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
    public LoginRes login(LoginReq req) {
        LoginRes res = new LoginRes(false, "[Info] Login initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. Token 값 검증
        if(!isIdTokenValid(req, res)) return res;

        // 3. Id 값 검증
        if(!isUserIdExist(req, res)) return res;

        // 4. 사용자 통계 정보 가져오기
        UserStat userStat = getStatIfExists(req, res);
        if (userStat == null) return res;

        // 5. 정보 업데이트
        if (!updateStatData(userStat, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Login successful");
        logger.info("[AuthService][{}] Login successful", req.getUserId());
        return res;
    }

    private boolean isRequestValid(LoginReq req, LoginRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getUserId() == null || req.getUserId().isEmpty()) {
            errors.add("[Failed] User ID must not be null or empty");
        }
        if (req.getIdToken() == null || req.getIdToken().isEmpty()) {
            errors.add("[Failed] IdToken must not be null or empty");
        }

        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid login request: {}", req.getUserId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid login request");
        logger.info("[AuthService][{}] Valid login request for user", req.getUserId());
        return true;
    }

    private boolean isIdTokenValid(LoginReq req, LoginRes res) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(req.getIdToken());
            if (!Objects.equals(decodedToken.getUid(), req.getUserId())) {
                res.setErrorCode(ErrorCode.INVALID_TOKEN);
                res.addMessage("[Failed] Provided IdToken UID mismatched");
                logger.warn("[AuthService][{}] Provided IdToken UID mismatched for user", req.getUserId());
                return false;
            }
            res.addMessage("[Success] Successfully verified IdToken");
            logger.info("[AuthService][{}] Successfully verified IdToken for user", req.getUserId());
            return true;
        } catch (FirebaseAuthException err) {
            res.setErrorCode(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] Firebase authentication failed");
            logger.error("[AuthService][{}] Firebase authentication error for user({}): {}", req.getUserId(), req.getIdToken(), err.getMessage());
            return false;
        }
    }

    private boolean isUserIdExist(LoginReq req, LoginRes res) {
        if (userRepo.existsByUserId(req.getUserId())) {
            res.addMessage("[Success] User(Info) exists");
            logger.info("[AuthService][{}] User(Info) exists, at Login", req.getUserId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] User(Info) not found");
        logger.warn("[AuthService][{}] User(Info) not found, at Login", req.getUserId());
        return false;
    }

    private UserStat getStatIfExists(LoginReq req, LoginRes res) {
        try {
            UserStat userStat = loginTrans.getStatByUserId(req.getUserId());
            res.addMessage("[Success] User(Stat) exists");
            logger.info("[AuthService][{}] User(Stat) exists, at Login", req.getUserId());
            return userStat;
        } catch (RuntimeException err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User(Stat) not found");
            logger.warn("[AuthService][{}] User(Stat) not found, at Login", req.getUserId());
            return null;
        }
    }

    private boolean updateStatData(UserStat userStat, LoginReq req, LoginRes res) {
        try {
            LocalDateTime now = LocalDateTime.now();

            if (!isNewDay(now, userStat.getLastLoginAt())) {
                res.addMessage("[Success] Stat data already up-to-date");
                logger.info("[AuthService][{}] Stat data already up-to-date for user", req.getUserId());
                return true;
            }
            loginTrans.updateStatData(userStat, now);
            res.addMessage("[Success] Successfully updated stat data");
            logger.info("[AuthService][{}] Updated stat data for user", req.getUserId());
            return true;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to update stat data");
            logger.error("[AuthService][{}] Failed to update stat data for user: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private boolean isNewDay(LocalDateTime now, LocalDateTime lastLogin) {
        return now.toLocalDate().isAfter(lastLogin.toLocalDate());
    }

    /*===========================
       사용자 상태변경
    ===========================*/

    /*===========================
       사용자 회원탈퇴
    ===========================*/
    public WithdrawRes withdraw(WithdrawReq req) {
        WithdrawRes res = new WithdrawRes(false, "[Info] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. ID 값 검증
        UserInfo userInfo = getUsersIfExist(req, res);
        if (userInfo == null) return res;

        // 3. FirebaseAuth 제거
        if(!deleteUserAtFirebase(req, res)) return res;

        // 4. 데이터베이스 상태값 변경
        if(!deleteUserAtDatabase(userInfo, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Withdraw successful");
        logger.info("[AuthService][{}] Withdraw successful", req.getUserId());
        return res;
    }

    private boolean isRequestValid(WithdrawReq req, WithdrawRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getUserId() == null || req.getUserId().isEmpty()) {
            errors.add("[Failed] User ID must not be null or empty");
        }
        if (req.getReason() == null) {
            errors.add("[Failed] Reason must not be null");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected, while withdrawReq: {}", req.getUserId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Withdraw request");
        logger.info("[AuthService][{}] Valid Withdraw request", req.getUserId());
        return true;
    }

    private UserInfo getUsersIfExist(WithdrawReq req, WithdrawRes res) {
        try {
            UserInfo userInfo = withdrawTrans.getUsersByUserId(req.getUserId());
            res.addMessage("[Success] User(Stat) exists");
            logger.info("[AuthService][{}] User(Info) exists, at Withdraw", req.getUserId());
            return userInfo;
        } catch (RuntimeException err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User(Stat) not found");
            logger.warn("[AuthService][{}] User(Info) not found, at Withdraw", req.getUserId());
            return null;
        }
    }

    private boolean deleteUserAtFirebase(WithdrawReq req, WithdrawRes res) {
        try {
            FirebaseAuth.getInstance().deleteUser(req.getUserId());
            res.addMessage("[Success] Successfully delete user at firebase");
            logger.info("[AuthService][{}] Successfully delete user at firebase", req.getUserId());
            return true;
        } catch (FirebaseAuthException err) {
            res.setErrorCode(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] Failed to delete user at Firebase");
            logger.error("[AuthService][{}] Failed to delete user at Firebase: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private boolean deleteUserAtDatabase(UserInfo userInfo, WithdrawReq req, WithdrawRes res) {
        try {
            LocalDateTime now = LocalDateTime.now();

            withdrawTrans.updateUsers(userInfo, now);
            res.addMessage("[Success] Successfully change user status to deleted");
            logger.info("[AuthService][{}] Successfully change user status to deleted", req.getUserId());
            return true;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Database connection error, while change user status to deleted");
            logger.error("[AuthService][{}] Database connection error, while change user status to deleted: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    /*===========================
       유틸리티
    ===========================*/

}
