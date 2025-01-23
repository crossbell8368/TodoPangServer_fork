package com.devcrew1os.service;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.util.Result;
import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.common.util.Response;
import com.devcrew1os.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;

    @Autowired
    private Response<?> response;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 로그인
    ===========================*/
    public Result login(UserReqDTO.Login dto, String idToken) {
        try {
            // Token verified
            if (!verifyIdToken(idToken, dto.getUserId())) {
                return Result.failure("Provided IdToken UID mismatched", com.devcrew1os.common.enums.ErrorCode.INVALID_TOKEN);
            }
            // DB verified
            if(!checkData(dto.getUserId())) {
                logger.info("[AuthService] Failed to search registered userData: {}", dto.getUserId());
                return Result.failure("Failed to search registered UserID", com.devcrew1os.common.enums.ErrorCode.USER_NOT_FOUND);
            }
            // Success
            logger.info("[AuthService] Successfully logged in : {}", dto.getUserId());
            return Result.success("Successfully Logged in!");

        } catch (FirebaseAuthException err) {
            // FirebaseAuth
            logger.error("[AuthService] Firebase authentication error: {}", err.getMessage());
            return Result.failure("Authentication failed", com.devcrew1os.common.enums.ErrorCode.UNAUTHORIZED);

        } catch (Exception err) {
            // Etc
            logger.error("[AuthService] Unexpected error during login: {}", err.getMessage());
            return Result.failure("Internal server error", ErrorCode.INTERNAL_ERROR);
        }
    }

    // FBAuth 정보확인
    private boolean verifyIdToken(String idToken, String uid) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

        if(!Objects.equals(decodedToken.getUid(), uid)) {
            logger.warn("[AuthService] Provided IdToken UID mismatch: expected={}, actual={}", uid, decodedToken.getUid());
            return false;
        }
        logger.info("[AuthService] IdToken verified successfully for user: {}", uid);
        return true;
    }

    // DB 정보확인
    private boolean checkData(String userId) {
        try {
            if (!userRepo.existsByUserId(userId)) {
                logger.warn("[AuthService] Attempt to login with unregistered userId: {}",userId);
                return false;
            }
            logger.info("[AuthService] Successfully search userData from storage: {}", userId);
            return true;

        } catch (DataAccessException err) {
            logger.error("[AuthService] Database access error: {}", err.getMessage());
            return false;
        } catch (PersistenceException err) {
            logger.error("[AuthService] JPA persistence error: {}", err.getMessage());
            return false;
        } catch (Exception err) {
            logger.error("[AuthService] Fail tp process Database related task: {}", err.getMessage());
            return false;
        }
    }
}
