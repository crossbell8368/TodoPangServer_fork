package com.devcrew1os.common.util;

import com.devcrew1os.dto.main.auth.LoginDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private static final long TOKEN_EXPIRED_TIME = 3500;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public LoginDTO tokenVerifier(String idToken, boolean isAdmin) {
        String redisKey = String.format("id_token:%s", idToken);

        // Step1. Check Redis
        String cachedId = redisTemplate.opsForValue().get(redisKey);
        if (cachedId != null) {
            logger.info("[TokenService][{}] Successfully resolve userId from cache", cachedId);
            return new LoginDTO(cachedId, null);
        }

        // Step2. Check Firebase
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String userId = decodedToken.getUid();
            String email = "";
            if(isAdmin) {
                email = decodedToken.getEmail();
            }

            redisTemplate.opsForValue().set(redisKey, userId, TOKEN_EXPIRED_TIME, TimeUnit.SECONDS);
            logger.info("[TokenService][{}] Firebase verified UID and cached", userId);
            if(isAdmin){
                return new LoginDTO(userId, email);
            } else{
                return new LoginDTO(userId, null);
            }
        } catch (FirebaseAuthException err) {
            logger.warn("[TokenService] Firebase token verification failed with provided idToken: {}", err.getMessage());
            return null;
        }
    }

    public void tokenCleaner(String idToken) {
        String redisKey = String.format("id_token:%s", idToken);
        String cachedId = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);
        if (cachedId != null) {
            logger.info("[TokenService][{}] Successfully removed token data", cachedId);
        } else {
            logger.warn("[TokenService] Token not found in cache, already expired or removed: {}", idToken);
        }
    }
}
