package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.users.UserStatus;
import com.devcrew1os.common.enums.users.UserWithdrawReason;
import com.devcrew1os.dto.main.auth.SignupReq;
import com.devcrew1os.entity.project.Project;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.entity.user.UserStat;
import com.devcrew1os.repository.main.project.ProjectRepository;
import com.devcrew1os.repository.main.users.UsersRepository;
import com.devcrew1os.repository.main.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthTransaction {

    private final UsersRepository usersRepo;
    private final UserStatRepository userStatRepo;
    private final ProjectRepository projectRepo;

    /*===========================
       회원가입
    ===========================*/
    @Transactional
    public void signupProcess(String userId, SignupReq req) {
        LocalDateTime now = LocalDateTime.now();

        if(usersRepo.existsUsersByUserId(userId)){
            throw new RuntimeException("User with id(" + userId + ") already exists");
        }

        Users newUser = Users.builder()
                .userId(userId)
                .userEmail(req.getEmail())
                .userName(req.getName())
                .status(UserStatus.ACTIVE.getValue())
                .socialType(req.getSocialType())
                .createdAt(now)
                .updatedAt(now)
                .withdrawReason(UserWithdrawReason.REASON1.getValue())
                .build();
        UserStat newStat = UserStat.builder()
                .userId(userId)
                .serviceTerm(1)
                .lastLoginAt(now)
                .lastLogoutAt(now)
                .build();
        Project newProject = Project.builder()
                .users(newUser)
                .build();

        usersRepo.save(newUser);
        userStatRepo.save(newStat);
        projectRepo.save(newProject);
    }

    /*===========================
       로그인
    ===========================*/
    @Transactional
    public void loginProcess(String userId) {

        UserStat userStat = userStatRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("UserStat (" + userId + ") not found")
        );
        // check re-login
        LocalDateTime now = LocalDateTime.now();
        if(!now.toLocalDate().isAfter(userStat.getLastLoginAt().toLocalDate())){
            return;
        }
        // update stat data
        userStatRepo.updateTermAndLastLogin(userId, now);
    }

    /*===========================
       회원탈퇴
    ===========================*/
    public Users getUsersByUserId(String userId) {
        return usersRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Users not found: " + userId)
        );
    }

    @Transactional
    public void updateWithdrawData(Users user) {
        try {
            usersRepo.save(user);
        } catch (Exception err) {
            throw new RuntimeException("Withdraw transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }
}
