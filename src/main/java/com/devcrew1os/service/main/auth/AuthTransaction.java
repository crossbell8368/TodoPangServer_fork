package com.devcrew1os.service.main.auth;

import com.devcrew1os.entity.log.UserWithdrawLog;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.entity.main.user.UserStat;
import com.devcrew1os.repository.log.UserWithdrawRepository;
import com.devcrew1os.repository.main.project.ProjectInfoRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import com.devcrew1os.repository.main.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthTransaction {

    private final UserInfoRepository userInfoRepo;
    private final UserStatRepository userStatRepo;
    private final ProjectInfoRepository projectRepo;
    private final UserWithdrawRepository withdrawRepo;

    /*===========================
       Signup
    ===========================*/
    public boolean checkUserExist(String userId){
        return userInfoRepo.existsUserInfoByUserId(userId);
    }

    @Transactional
    public void saveUserData(UserInfo newUser, UserStat newUserStat, ProjectInfo newProject) {
        try {
            userInfoRepo.save(newUser);
            userStatRepo.save(newUserStat);
            projectRepo.save(newProject);
        } catch (Exception err) {
            throw new RuntimeException("Signup transaction failed, initiate rolling back: " + err.getMessage(), err);
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
        userStatRepo.save(userStat);
    }

    /*===========================
       Withdraw
    ===========================*/
    public UserInfo getUsersByUserId(String userId) {
        return userInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User(Info) not found: " + userId)
        );
    }

    @Transactional
    public void updateWithdrawData(UserInfo user, UserWithdrawLog reason) {
        try {
            userInfoRepo.save(user);
            withdrawRepo.save(reason);
        } catch (Exception err) {
            throw new RuntimeException("Withdraw transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }
}
