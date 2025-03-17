package com.devcrew1os.service.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.auth.SignupRes;
import com.devcrew1os.entity.user.UserStat;
import com.devcrew1os.entity.user.UserInfo;
import com.devcrew1os.repository.StatRepository;
import com.devcrew1os.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class SignupTransaction {

    private final UserRepository userRepo;
    private final StatRepository statRepo;
    private static final Logger logger = LoggerFactory.getLogger(SignupTransaction.class);

    @Transactional
    public void saveUserData(UserInfo newUser, UserStat newUserStat, SignupRes res) {
        try {
            executeTransaction(() -> userRepo.save(newUser), res, "User", newUser.getUserId());
            executeTransaction(() -> statRepo.save(newUserStat), res, "Stat", newUserStat.getUserId());

        } catch (Exception err) {
            logger.error("[AuthTransaction][{}] Transaction failed, rolling back: {}", newUser.getUserId(), err.getMessage());
        }
    }

    private void executeTransaction(Runnable jpaAction, SignupRes res, String entity, String userId) {
        try {
            jpaAction.run();
            res.addMessage("[Success] Successfully saved " + entity + " data.");
            logger.info("[AuthTransaction][{}] Successfully saved {} data.", userId, entity);

        } catch (DataAccessException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to save " + entity + " data.");
            logger.error("[AuthTransaction][{}] Transaction({}) failed, rolling back: {}", userId, entity, err.getMessage());
            throw err;
        }
    }
}
