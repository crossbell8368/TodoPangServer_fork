package com.devcrew1os.service.auth;

import com.devcrew1os.entity.Stat;
import com.devcrew1os.repository.StatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginTransactionService {

    private final StatRepository statRepo;
    private static final Logger logger = LoggerFactory.getLogger(LoginTransactionService.class);

    public Stat getStatByUserId(String userId) {
        return statRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Stat) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateStatData(Stat stat, LocalDateTime now) {
        stat.setStatTerm(stat.getStatTerm() + 1);
        stat.setStatUserLastLoginAt(now);
        statRepo.save(stat);  // 명시적으로 저장 호출
    }
}
