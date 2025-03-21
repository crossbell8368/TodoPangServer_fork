package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawTransaction {

    private final UserInfoRepository userRepo;

    public UserInfo getUsersByUserId(String userId) {
        return userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Info) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateUsers(UserInfo users, LocalDateTime now) {
        users.setStatus(UserStatus.DELETED.getValue());
        users.setDeletedAt(now);
        userRepo.save(users);
    }
}
