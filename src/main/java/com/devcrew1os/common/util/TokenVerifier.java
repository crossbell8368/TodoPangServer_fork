package com.devcrew1os.common.util;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.util.TokenVerifyReq;
import com.devcrew1os.dto.main.util.TokenVerifyRes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenVerifier {

    private final StringRedisTemplate redisTemplate;
    private static final long TOKEN_EXPIRED_TIME = 3500;
    private static final Logger logger = LoggerFactory.getLogger(TokenVerifier.class);

    public TokenVerifyRes isValid(TokenVerifyReq req) {
        TokenVerifyRes res = new TokenVerifyRes(true, "[Info] IdToken verify initiated", ErrorCode.OK);
        String redisKey = String.format("id_token:user:%s", req.getToken());

        // Step1. Check Redis
        String cachedToken = redisTemplate.opsForValue().get(redisKey);
        if(cachedToken != null && cachedToken.equals(req.getToken())) {
            res.addMessage("[Success] Token verified by cached in Redis");
            logger.info("[TokenVerifier][{}] Provided IdToken UID matched at {}", req.getUserId(), req.getService());
            return res;
        }

        try {
            // Step2. Check Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(req.getToken());
            if(!Objects.equals(decodedToken.getUid(), req.getUserId())) {
                res.setStatus(false);
                res.setCode(ErrorCode.INVALID_TOKEN);
                res.addMessage("[Failed] Provided IdToken UID mismatched");
                logger.warn("[TokenVerifier][{}] Provided IdToken UID mismatched for user, at {}", req.getUserId(), req.getService());
                return res;
            }

            // Step3. Update Redis
            redisTemplate.opsForValue().set(redisKey, req.getToken(), TOKEN_EXPIRED_TIME, TimeUnit.SECONDS);
            res.addMessage("[Success] Token verified by Firebase and update redis");
            logger.info("[TokenVerifier][{}] Token verified and update Redis {}", req.getUserId(), req.getService());
            return res;

        } catch (FirebaseAuthException err) {
            res.setStatus(false);
            res.setCode(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] Firebase authentication failed");
            logger.error("[TokenVerifier][{}] Failed to verify token at {}", req.getUserId(), req.getService());
            return res;
        }
    }
}
