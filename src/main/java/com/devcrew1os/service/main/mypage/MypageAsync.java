package com.devcrew1os.service.main.mypage;

import com.devcrew1os.entity.user.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MypageAsync {

    private final MypageTransaction transaction;

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
    public CompletableFuture<Boolean> isUserDeletedFromDatabase(Users user, StringBuilder msg) {
        try {
            transaction.updateWithdrawData(user);
            msg.append("[Success] User deleted from database");
            return CompletableFuture.completedFuture(true);
        } catch (Exception err) {
            msg.append(err.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}
