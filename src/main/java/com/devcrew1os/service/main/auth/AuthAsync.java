package com.devcrew1os.service.main.auth;

import com.devcrew1os.dto.main.auth.WithdrawDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuthAsync {

    private final AuthTransaction transaction;

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
    public CompletableFuture<Boolean> isUserDeletedFromDatabase(WithdrawDTO dto, StringBuilder msg) {
        try {
            LocalDateTime now = LocalDateTime.now();
            transaction.updateWithdrawData(dto.getUser(),dto.getStatus().getUserWithdrawInfo(), dto.getStatus());
            msg.append("[Success] User deleted from database");
            return CompletableFuture.completedFuture(true);
        } catch (Exception err) {
            msg.append(err.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
