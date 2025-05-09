package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.users.UserSocialType;
import com.devcrew1os.common.enums.users.UserStatus;
import com.devcrew1os.common.enums.users.UserWithdrawReason;
import com.devcrew1os.dto.main.auth.*;
import com.devcrew1os.entity.user.Users;
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

    private final AuthTransaction transaction;
    private final AuthAsync authAsync;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 회원가입
    ===========================*/
    public SignupRes signup(String userId, SignupReq req) {
        SignupRes res = new SignupRes(false, "[Info] Signup initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(userId, req, res)) return res;

        try {
            transaction.signupProcess(userId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Signup finish");
            logger.info("[AuthService][{}] Signup successful", userId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DUPLICATE_USER);
            res.addMessage("[Failed] Request UserID already exists");
            logger.warn("[AuthService][{}] Request UserID already exists, at Signup", userId);
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Unable to save new user data");
            logger.warn("[AuthService][{}] Unable to save new user data: {}", userId, err.getMessage());
            return res;
        }
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

    /*===========================
       사용자 로그인
    ===========================*/
    public LoginRes login(String userId) {
        LoginRes res = new LoginRes(false, "[Info] Login initiated", ErrorCode.OK);

        try {
            transaction.loginProcess(userId);
            res.setSuccess(true);
            res.addMessage("[Info] Login successful");
            logger.info("[AuthService][{}] Login successful", userId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] UserStat not found");
            logger.warn("[AuthService][{}] UserStat not found, at Login", userId);
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to update stat data");
            logger.error("[AuthService][{}] Failed to update stat data for user: {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       사용자 회원탈퇴
    ===========================*/
    public WithdrawRes withdraw(String userId, WithdrawReq req) {
        LocalDateTime now = LocalDateTime.now();
        WithdrawRes res = new WithdrawRes(false, "[Info] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(userId, req, res)) return res;

        // 2. 업데이트 데이터 준비
        Users data = getWithdrawData(userId, req, res, now);
        if(data == null) return res;

        // 3. Firebase & Database 업데이트
        if(!isWithdrawSuccess(data, res))return res;

        res.setSuccess(true);
        res.addMessage("[Info] Withdraw successful");
        logger.info("[AuthService][{}] Withdraw successful", userId);
        return res;
    }

    private boolean isRequestValid(String userId, WithdrawReq req, WithdrawRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getReason() == null) {
            errors.add("[Failed] Reason must not be null");
        } else {
            if (!UserWithdrawReason.contains(req.getReason())) {
                errors.add("[Failed] Invalid reason");
            }
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

    private Users getWithdrawData(String userId, WithdrawReq req, WithdrawRes res, LocalDateTime now) {
        try {
            Users user = transaction.getUsersByUserId(userId);

            user.setStatus(UserStatus.DELETED.getValue());
            user.setWithdrawReason(req.getReason());
            user.setWithdrawAt(now);
            return user;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User not found");
            logger.error("[AuthService][{}] User not found at withdraw process: {}", userId, err.getMessage());
            return null;
        }
    }

    private boolean isWithdrawSuccess(Users user, WithdrawRes res) {
        boolean status = false;
        StringBuilder firebaseMsg = new StringBuilder();
        StringBuilder databaseMsg = new StringBuilder();

        try {
            CompletableFuture<Boolean> firebaseFuture = authAsync.isUserDeletedFromFirebase(user.getUserId(), firebaseMsg);
            CompletableFuture<Boolean> databaseFuture = authAsync.isUserDeletedFromDatabase(user, databaseMsg);
            CompletableFuture.allOf(firebaseFuture, databaseFuture).join();

            Boolean firebaseResult = firebaseFuture.get();
            Boolean databaseResult = databaseFuture.get();

            if(!firebaseResult && !databaseResult) {
                res.setErrorCode(ErrorCode.INTERNAL_ERROR);
                res.addMessage("[Failed] Firebase & database error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase & database: {} \n {}", user.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            } else if (!firebaseResult) {
                res.setErrorCode(ErrorCode.FIREBASE_ERROR);
                res.addMessage("[Failed] Firebase error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase: {}", user.getUserId(), firebaseMsg.toString());
            } else if (!databaseResult) {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] Database error detected");
                logger.error("[AuthService][{}] Failed to delete user from database: {}", user.getUserId(), databaseMsg.toString());
            } else {
                status = true;
                res.addMessage("[Info] User data deleted");
                logger.info("[AuthService][{}] Withdraw user data deleted: \n {} \n {}", user.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            }
            return status;

        } catch (Exception err) {
            res.addMessage("[Failed] Delete process exception");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Withdraw delete process exception: {} \n {} \n {}", user.getUserId(), err.getMessage(), firebaseMsg.toString(), databaseMsg.toString());
            return false;
        }
    }
}
