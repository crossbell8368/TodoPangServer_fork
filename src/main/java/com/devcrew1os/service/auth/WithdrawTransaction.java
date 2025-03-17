package com.devcrew1os.service.auth;

import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.entity.Users;
import com.devcrew1os.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawTransaction {

    private final UserRepository userRepo;

    public Users getUsersByUserId(String userId) {
        return userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Info) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateUsers(Users users, LocalDateTime now) {
        users.setStatus(UserStatus.DELETED.getValue());
        users.setDeletedAt(now);
        userRepo.save(users);
    }
}
