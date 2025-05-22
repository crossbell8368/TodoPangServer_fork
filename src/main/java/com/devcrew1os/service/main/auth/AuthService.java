package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.users.UserSocialType;
import com.devcrew1os.dto.main.auth.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthTransaction transaction;

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
}
