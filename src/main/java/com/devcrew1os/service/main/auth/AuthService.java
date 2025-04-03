package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.Location;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.dto.main.auth.*;
import com.devcrew1os.dto.util.TokenReq;
import com.devcrew1os.dto.util.TokenRes;
import com.devcrew1os.dto.util.ValidationResult;
import com.devcrew1os.entity.main.user.UserStat;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import com.devcrew1os.service.util.TokenService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
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

    private final TokenService tokenService;

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
        if (userRepo.existsUserInfoByUserId(req.getUserId())) {
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
        TokenReq request = new TokenReq(
                req.getIdToken(),
                req.getUserId(),
                Location.MAIN_AUTH.getVal()
        );
        TokenRes response = tokenService.tokenVerifier(request);

        res.addMessage(response.getMessage());
        if(response.isStatus()){
            return true;
        } else {
            res.setErrorCode(response.getCode());
            return false;
        }
    }

    private boolean isUserIdExist(LoginReq req, LoginRes res) {
        if (userRepo.existsUserInfoByUserId(req.getUserId())) {
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
    public WithdrawRes withdraw(String token, WithdrawReq req) {
        UserInfo userInfo = new UserInfo();
        WithdrawRes res = new WithdrawRes(false, "[Info] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. TokenId & User 값 검증
        ValidationResult result = isUserValid(token, req, res);
        if(!result.isValid() || result.getUserInfo() == null) return res;

        // 3. Firebase & Database 업데이트
        if(!isUserDeleted(result.getUserInfo(), req, res))return res;

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

    private ValidationResult isUserValid(String token, WithdrawReq req, WithdrawRes res) {
        ValidationResult result = new ValidationResult(false, null);
        StringBuilder tokenMsg = new StringBuilder();
        StringBuilder userMsg = new StringBuilder();

        try {
            CompletableFuture<Boolean> tokenFuture = withdrawAsync.isTokenValid(token, req.getUserId(), tokenMsg);
            CompletableFuture<UserInfo> userFuture = withdrawAsync.isUserExist(req.getUserId(), userMsg);
            CompletableFuture.allOf(tokenFuture, userFuture).join();

            Boolean tokenResult = tokenFuture.get();
            UserInfo userResult = userFuture.get();

            if (!tokenResult && userResult == null) {
                res.setErrorCode(ErrorCode.UNAUTHORIZED);
                res.addMessage("[Failed] Token invalid & User not found");
                logger.error("[AuthService][{}] Withdraw user token is invalid and user not found at server: {} \n {}", req.getUserId(), tokenMsg.toString(), userMsg.toString());
            } else if(!tokenResult) {
                res.setErrorCode(ErrorCode.UNAUTHORIZED);
                res.addMessage("[Failed] Token invalid");
                logger.error("[AuthService][{}] Withdraw user token is invalid: {}", req.getUserId(), tokenMsg.toString());
            } else if(userResult == null) {
                res.setErrorCode(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] User not found");
                logger.error("[AuthService][{}] Withdraw user not exist at server: {}", req.getUserId(), userMsg.toString());
            } else {
                result.setValid(true);
                result.setUserInfo(userResult);
                res.addMessage("[Info] Validation complete");
                logger.info("[AuthService][{}] Withdraw user validation complete \n {} \n {}", req.getUserId(), tokenMsg.toString(), userMsg.toString());
            }
            return result;
        } catch (Exception err) {
            res.addMessage("[Failed] Validation process exception");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Withdraw validation process exception: {} \n {} \n {}", req.getUserId(), err.getMessage(), tokenMsg.toString(), userMsg.toString());
            return result;
        }
    }

    private boolean isUserDeleted(UserInfo entity, WithdrawReq req, WithdrawRes res) {
        boolean status = false;
        StringBuilder firebaseMsg = new StringBuilder();
        StringBuilder databaseMsg = new StringBuilder();

        try {
            CompletableFuture<Boolean> firebaseFuture = withdrawAsync.isUserDeletedFromFirebase(req.getUserId(), firebaseMsg);
            CompletableFuture<Boolean> databaseFuture = withdrawAsync.isUserDeletedFromDatabase(entity, databaseMsg);
            CompletableFuture.allOf(firebaseFuture, databaseFuture).join();

            Boolean firebaseResult = firebaseFuture.get();
            Boolean databaseResult = databaseFuture.get();

            if(!firebaseResult && !databaseResult) {
                res.setErrorCode(ErrorCode.INTERNAL_ERROR);
                res.addMessage("[Failed] Firebase & database error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase & database: {} \n {}", req.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            } else if (!firebaseResult) {
                res.setErrorCode(ErrorCode.FIREBASE_ERROR);
                res.addMessage("[Failed] Firebase error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase: {}", req.getUserId(), firebaseMsg.toString());
            } else if (!databaseResult) {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] Database error detected");
                logger.error("[AuthService][{}] Failed to delete user from database: {}", req.getUserId(), databaseMsg.toString());
            } else {
                status = true;
                res.addMessage("[Info] User data deleted");
                logger.info("[AuthService][{}] Withdraw user data deleted: \n {} \n {}", req.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            }
            return status;

        } catch (Exception err) {
            res.addMessage("[Failed] Delete process exception");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Withdraw delete process exception: {} \n {} \n {}", req.getUserId(), err.getMessage(), firebaseMsg.toString(), databaseMsg.toString());
            return false;
        }
    }
    /*===========================
       유틸리티
    ===========================*/
}
