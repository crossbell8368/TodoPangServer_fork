package com.devcrew1os.service;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.dto.auth.*;
import com.devcrew1os.entity.Users;
import com.devcrew1os.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 회원가입
    ===========================*/
    public SignupRes signup(SignupReq req) {
        SignupRes res = new SignupRes(false, "[Success] Signup initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. Id값 검증
        if(!isUserIdExist(req, res)) return res;

        // 3. 데이터 생성
        Users newUser = createUser(req);

        // 4. DB 적용
        if(!applyToStorage(newUser, res)) return res;
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
        if (req.getSocialType() == null) {
            errors.add("[Failed] Social Type must not be null");
        }
        if (errors.isEmpty()) {
            res.addMessage("[Success] Valid Signup request");
            logger.info("[AuthService][{}] Valid Signup request", req.getUserId());
            return true;
        } else {
            res.setStatus(false);
            res.setError(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected during signupReq: {}", req.getUserId(), res.getMessage());
            return false;
        }
    }

    private boolean isUserIdExist(SignupReq req, SignupRes res) {
        try {
            if (userRepo.existsByUserId(req.getUserId())){
                res.setStatus(false);
                res.setError(ErrorCode.DUPLICATE_USER);
                res.addMessage("[Failed] UserId already exists");
                logger.warn("[AuthService][{}] Duplicated UserId detected", req.getUserId());
                return false;
            }
            res.setStatus(true);
            res.addMessage("[Success] Available userId");
            logger.info("[AuthService][{}] Available userId", req.getUserId());
            return true;

        } catch (DataAccessException err) {
            res.setStatus(false);
            res.setError(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to access Database");
            logger.error("[AuthService][{}] Database connection error: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private Users createUser(SignupReq dto) {
        LocalDateTime now = LocalDateTime.now();
        return Users.builder()
                .userId(dto.getUserId())
                .userEmail(dto.getEmail())
                .userName(dto.getName())
                .userSocialType(UserSocialType.fromValue(dto.getSocialType()))
                .userStatus(UserStatus.ACTIVE)
                .userCreatedAt(now)
                .userUpdatedAt(now)
                .userDeletedAt(now)
                .userLastLoginAt(now)
                .userLastLogoutAt(now)
                .build();
    }

    private boolean applyToStorage(Users newUser, SignupRes res) {
        try {
            userRepo.save(newUser);
            res.setStatus(true);
            res.addMessage("[Success] Successfully Signed up");
            logger.info("[AuthService][{}] Successfully saved userData", newUser.getUserId());
            return true;
        } catch (DataAccessException err) {
            res.setStatus(false);
            res.setError(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to save userData");
            logger.error("[AuthService][{}] Failed to save userData: {}", newUser.getUserId(), err.getMessage());
            return false;
        }
    }

    /*===========================
       사용자 로그인
    ===========================*/
    public LoginRes login(LoginReq req) {
        LoginRes res = new LoginRes(false, "[Success] Login initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. Token 값 검증
        if(!isIdTokenValid(req, res)) return res;

        // 3. Id 값 검증
        if(!isUserIdExist(req, res)) return res;
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
        if (errors.isEmpty()) {
            res.addMessage("[Success] Valid Login request");
            logger.info("[AuthService][{}] Valid Login request", req.getUserId());
            return true;
        } else {
            res.setStatus(false);
            res.setError(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected while process login request: {}", req.getUserId(), res.getMessage());
            return false;
        }
    }

    private boolean isIdTokenValid(LoginReq req, LoginRes res) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(req.getIdToken());
            if(Objects.equals(decodedToken.getUid(), req.getUserId())){
                res.setStatus(true);
                res.addMessage("[Success] Successfully verified IdToken");
                logger.info("[AuthService][{}] Successfully verified IdToken", req.getUserId());
                return true;
            } else {
                res.setStatus(false);
                res.setError(ErrorCode.INVALID_TOKEN);
                res.addMessage("[Failed] Provided IdToken UID mismatched");
                logger.warn("[AuthService][{}] Provided IdToken UID mismatched", req.getUserId());
                return false;
            }
        } catch (FirebaseAuthException err) {
            res.setStatus(false);
            res.setError(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] Firebase authentication failed");
            logger.error("[AuthService][{}] Firebase authentication error: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private boolean isUserIdExist(LoginReq req, LoginRes res) {
        try {
            if (userRepo.existsByUserId(req.getUserId())) {
                res.setStatus(true);
                res.addMessage("[Success] Exist userId");
                logger.info("[AuthService][{}] Valid userId, while process login request", req.getUserId());
                return true;
            } else {
                res.setStatus(false);
                res.setError(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] User not found");
                logger.warn("[AuthService][{}] User not found, while process login request", req.getUserId());
                return false;
            }
        } catch (DataAccessException err) {
            res.setStatus(false);
            res.setError(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to access Database");
            logger.error("[AuthService][{}] Database connection error, while process login request: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    /*===========================
       사용자 상태변경
    ===========================*/

    /*===========================
       사용자 회원탈퇴
    ===========================*/
    public WithdrawRes withdraw(WithdrawReq req) {
        WithdrawRes res = new WithdrawRes(false, "[Success] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(req, res)) return res;

        // 2. ID 값 검증
        if(!isUserIdExist(req, res)) return res;

        // 3. FirebaseAuth 제거
        if(!deleteUserAtFirebase(req, res)) return res;

        // 4. 데이터베이스 상태값 변경
        if(!deleteUserAtDatabase(req, res)) return res;

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
        if (errors.isEmpty()) {
            res.addMessage("[Success] Valid Withdraw request");
            logger.info("[AuthService][{}] Valid Withdraw request", req.getUserId());
            return true;
        } else {
            res.setStatus(false);
            res.setError(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected, while withdrawReq: {}", req.getUserId(), res.getMessage());
            return false;
        }
    }

    private boolean isUserIdExist(WithdrawReq req, WithdrawRes res) {
        try {
            if (userRepo.existsByUserId(req.getUserId())) {
                res.setStatus(true);
                res.addMessage("[Success] Exist userId");
                logger.info("[AuthService][{}] Valid userId, while process withdraw request", req.getUserId());
                return true;
            } else {
                res.setStatus(false);
                res.setError(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] User not found");
                logger.warn("[AuthService][{}] User not found, while process withdraw request", req.getUserId());
                return false;
            }
        } catch (DataAccessException err) {
            res.setStatus(false);
            res.setError(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to access Database");
            logger.error("[AuthService][{}] Database connection error, while process withdraw request: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private boolean deleteUserAtFirebase(WithdrawReq req, WithdrawRes res) {
        try {
            FirebaseAuth.getInstance().deleteUser(req.getUserId());
            res.setStatus(true);
            res.addMessage("[Success] Successfully delete user at firebase");
            logger.info("[AuthService][{}] Successfully delete user at firebase", req.getUserId());
            return true;
        } catch (FirebaseAuthException err) {
            res.setStatus(false);
            res.setError(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] Failed to delete user at Firebase");
            logger.error("[AuthService][{}] Failed to delete user at Firebase: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    private boolean deleteUserAtDatabase(WithdrawReq req, WithdrawRes res) {
        try {
            if (userRepo.updateUserStatus(req.getUserId(), UserStatus.DELETED) > 0) {
                res.setStatus(true);
                res.addMessage("[Success] Successfully change user status");
                logger.info("[AuthService][{}] Successfully change user status", req.getUserId());
                return true;
            } else {
                res.setStatus(false);
                res.setError(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] User not found, while change user status");
                logger.warn("[AuthService][{}] User not found, while change user status", req.getUserId());
                return false;
            }
        } catch (DataAccessException err) {
            res.setStatus(false);
            res.setError(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Database connection error, while change user status");
            logger.error("[AuthService][{}] Database connection error, while change user status: {}", req.getUserId(), err.getMessage());
            return false;
        }
    }

    /*===========================
       유틸리티
    ===========================*/

}
