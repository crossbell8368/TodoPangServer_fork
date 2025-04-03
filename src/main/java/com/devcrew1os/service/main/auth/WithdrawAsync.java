package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.Location;
import com.devcrew1os.dto.util.TokenReq;
import com.devcrew1os.dto.util.TokenRes;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.service.util.TokenService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WithdrawAsync {

    private final TokenService tokenService;
    private final WithdrawTransaction transaction;

    @Async
    public CompletableFuture<Boolean> isTokenValid(String token, String userId, StringBuilder msg) {
        TokenReq request = new TokenReq(
                token,
                userId,
                Location.MAIN_AUTH.getVal()
        );
        TokenRes response = tokenService.tokenVerifier(request);
        msg.append(response.getMessage());
        return response.isStatus()
                ? CompletableFuture.completedFuture(true)
                : CompletableFuture.completedFuture(false);
    }

    @Async
    public CompletableFuture<UserInfo> isUserExist(String userId, StringBuilder msg) {
        try {
            UserInfo userInfo = transaction.getUsersByUserId(userId);
            return CompletableFuture.completedFuture(userInfo);
        } catch (RuntimeException err) {
            msg.append(err.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async
    public CompletableFuture<Boolean> isUserDeletedFromFirebase(String userId, StringBuilder msg) {
        try {
            FirebaseAuth.getInstance().deleteUser(userId);

            msg.append("[Success] User deleted from firebase");
            return CompletableFuture.completedFuture(true);

        } catch (FirebaseAuthException err) {
            msg.append(err.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> isUserDeletedFromDatabase(UserInfo userInfo, StringBuilder msg) {
        try {
            LocalDateTime now = LocalDateTime.now();
            transaction.updateUsers(userInfo, now);

            msg.append("[Success] User deleted from database");
            return CompletableFuture.completedFuture(true);

        } catch (Exception err) {
            msg.append(err.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
