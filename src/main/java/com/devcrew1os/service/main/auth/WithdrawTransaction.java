package com.devcrew1os.service.main.auth;

import com.devcrew1os.entity.admin.auth.AdminWithdrawInfo;
import com.devcrew1os.entity.admin.auth.AdminWithdrawStat;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.admin.AdminWithdrawInfoRepository;
import com.devcrew1os.repository.admin.AdminWithdrawStatRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawTransaction {

    private final UserInfoRepository userRepo;
    private final AdminWithdrawInfoRepository withdrawInfoRepo;
    private final AdminWithdrawStatRepository withdrawStatRepo;

    private static final Logger logger = LoggerFactory.getLogger(WithdrawTransaction.class);

    public UserInfo getUsersByUserId(String userId) {
        return userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Info) not found: " + userId)
        );
    }

    public AdminWithdrawInfo getWithdrawInfoById(Integer infoId) {
        return withdrawInfoRepo.findById(infoId).orElseThrow(
                () -> new RuntimeException("Withdraw(Info) not found: " + infoId)
        );
    }

    @Transactional
    public void updateWithdrawData(UserInfo user, AdminWithdrawInfo reason, AdminWithdrawStat archive) {
        String userId = user.getUserId();
        try {
            executor(() -> userRepo.save(user), "User(Info)", userId);
            executor(() -> withdrawInfoRepo.save(reason), "WithdrawInfo", userId);
            executor(() -> withdrawStatRepo.save(archive), "WithdrawStat", userId);
        } catch (Exception err) {
            logger.error("[WithdrawTrans][{}] Withdraw process failed", userId);
            throw err;
        }
    }

    private void executor(Runnable jpaAction, String entity, String userId) {
        try {
            jpaAction.run();
            logger.info("[WithdrawTrans][{}] Successfully update {} data.", userId, entity);
        } catch (Exception err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("[WithdrawTrans][{}] Transaction({}) rollback initiated.", userId, entity);
            throw err;
        }
    }
}
