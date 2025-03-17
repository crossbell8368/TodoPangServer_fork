package com.devcrew1os.service.auth;

import com.devcrew1os.entity.Stat;
import com.devcrew1os.repository.StatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginTransaction {

    private final StatRepository statRepo;

    public Stat getStatByUserId(String userId) {
        return statRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Stat) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateStatData(Stat stat, LocalDateTime now) {
        stat.setServiceTerm(stat.getServiceTerm() + 1);
        stat.setLastLoginAt(now);
        statRepo.save(stat);  // 명시적으로 저장 호출
    }
}
