package com.devcrew1os.service.main.auth;

import com.devcrew1os.entity.main.user.UserStat;
import com.devcrew1os.repository.main.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginTransaction {

    private final UserStatRepository statRepo;

    public UserStat getStatByUserId(String userId) {
        return statRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Stat) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateStatData(UserStat userStat, LocalDateTime now) {
        userStat.setServiceTerm(userStat.getServiceTerm() + 1);
        userStat.setLastLoginAt(now);
        statRepo.save(userStat);  // 명시적으로 저장 호출
    }
}
