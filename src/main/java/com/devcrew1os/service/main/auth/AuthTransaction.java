package com.devcrew1os.service.main.auth;

import com.devcrew1os.entity.admin.auth.AdminWithdrawInfo;
import com.devcrew1os.entity.admin.auth.AdminWithdrawStat;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.entity.main.user.UserStat;
import com.devcrew1os.repository.admin.AdminWithdrawInfoRepository;
import com.devcrew1os.repository.admin.AdminWithdrawStatRepository;
import com.devcrew1os.repository.main.ProjectInfoRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import com.devcrew1os.repository.main.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthTransaction {

    private final UserInfoRepository userInfoRepo;
    private final UserStatRepository userStatRepo;
    private final ProjectInfoRepository projectRepo;
    private final AdminWithdrawInfoRepository withdrawInfoRepo;
    private final AdminWithdrawStatRepository withdrawStatRepo;

    /*===========================
       Signup
    ===========================*/
    public boolean checkUserExist(String userId){
        return userInfoRepo.existsUserInfoByUserId(userId);
    }

    @Transactional
    public void saveUserData(UserInfo newUser, UserStat newUserStat, ProjectInfo newProject) {
        try {
            executeTransaction(() -> userInfoRepo.save(newUser));
            executeTransaction(() -> userStatRepo.save(newUserStat));
            executeTransaction(() -> projectRepo.save(newProject));

        } catch (Exception err) {
            throw new RuntimeException("Transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }

    private void executeTransaction(Runnable jpaAction) {
        try {
            jpaAction.run();

        } catch (DataAccessException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw err;
        }
    }

    /*===========================
       Login
    ===========================*/
    public UserStat getStatByUserId(String userId) {
        return userStatRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Stat) <" + userId + "> not found")
        );
    }

    @Transactional
    public void updateStatData(UserStat userStat, LocalDateTime now) {
        userStat.setServiceTerm(userStat.getServiceTerm() + 1);
        userStat.setLastLoginAt(now);
        userStatRepo.save(userStat);  // 명시적으로 저장 호출
    }

    /*===========================
       Withdraw
    ===========================*/
    public UserInfo getUsersByUserId(String userId) {
        return userInfoRepo.findByUserId(userId).orElseThrow(
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
            executor(() -> userInfoRepo.save(user));
            executor(() -> withdrawInfoRepo.save(reason));
            executor(() -> withdrawStatRepo.save(archive));
        } catch (Exception err) {
            throw new RuntimeException("Transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }

    private void executor(Runnable jpaAction) {
        try {
            jpaAction.run();
        } catch (Exception err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw err;
        }
    }
}
