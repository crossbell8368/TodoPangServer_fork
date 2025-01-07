package com.devcrew1os.service;

import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.util.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private Response<?> response;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /*===========================
       사용자 로그인
    ===========================*/
    public ResponseEntity<?> login(UserReqDTO.Login dto, String idToken) {
        try {
            verifyIdToken(idToken, dto.getUserId());

            if(checkData(dto.getUserId())) {
                logger.info("User logged in successfully: {}", dto.getUserId());
                return response.success("User logged in successfully");
            } else {
                logger.info("User not registered: {}", dto.getUserId());
                return response.fail("Not Registered UserID");
            }
        //TODO: Add 'AuthServiceExceptions'
        } catch(Exception err) {
            return response.fail(err.getMessage());
        }
    }

    // FBAuth 정보확인
    private void verifyIdToken(String idToken, String uid) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            if(decodedToken.getUid().equals(uid)) {
                logger.info("IdToken verified for user: {}", uid);
            } else {
                logger.warn("IdToken UID mismatch for user: {}", uid);
                //TODO: Add 'AuthServiceExceptions'
            }

        } catch(FirebaseAuthException err) {
            logger.error("IdToken verification failed: {}", err.getMessage());
            //TODO: Add 'AuthServiceExceptions'
        }
    }

    // DB 정보확인
    private boolean checkData(String uid) {
        try {
            //TODO: Add Database search process
            return true;

        } catch (Exception err) {
            logger.error("Database access failed: {}", err.getMessage());
            //TODO: Add 'AuthServiceExceptions'

            return false;
        }
    }
}
