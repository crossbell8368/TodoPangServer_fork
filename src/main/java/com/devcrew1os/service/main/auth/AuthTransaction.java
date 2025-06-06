package com.devcrew1os.service.main.auth;

import com.devcrew1os.common.enums.users.UserRole;
import com.devcrew1os.common.enums.users.UserStatus;
import com.devcrew1os.common.enums.users.UserWithdrawReason;
import com.devcrew1os.dto.main.auth.SignupReq;
import com.devcrew1os.entity.project.Project;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.entity.user.UserStat;
import com.devcrew1os.repository.project.ProjectRepository;
import com.devcrew1os.repository.users.UsersRepository;
import com.devcrew1os.repository.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthTransaction {

    private final UsersRepository usersRepo;
    private final UserStatRepository userStatRepo;
    private final ProjectRepository projectRepo;

    private static final Logger logger = LoggerFactory.getLogger(AuthTransaction.class);

    /*===========================
       회원가입
    ===========================*/
    @Transactional
    public void signupProcess(String userId, SignupReq req, boolean isAdminReq) {
        Optional<Users> candidateUsersOpt = usersRepo.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        // 데이터 존재여부 확인
        if(candidateUsersOpt.isPresent()){
            Users existingUser = candidateUsersOpt.get();

            // 관리자 요청 확인
            if(isAdminReq) {
                if(existingUser.getRole() == UserRole.ADMIN.getValue()){
                    logger.info("[AuthTrans][{}] User is already an ADMIN. No changes made.", userId);
                    return;
                } else {
                    existingUser.setRole(UserRole.ADMIN.getValue());
                    existingUser.setUpdatedAt(now);
                    logger.info("[AuthTrans][{}] User promoted to ADMIN.", userId);
                    return;
                }
            // 일반 사용자 요청: 중복ID case
            } else {
                throw new RuntimeException("User with id(" + userId + ") already exists");
            }
        // 신규회원가입
        } else {
            Users newUser = Users.builder()
                    .userId(userId)
                    .userEmail(req.getEmail())
                    .userName(req.getName())
                    .role(isAdminReq ? UserRole.ADMIN.getValue() : UserRole.USER.getValue())
                    .status(UserStatus.ACTIVE.getValue())
                    .socialType(req.getSocialType())
                    .createdAt(now)
                    .updatedAt(now)
                    .withdrawReason(UserWithdrawReason.REASON1.getValue())
                    .build();
            Users savedUser = usersRepo.save(newUser);

            UserStat newStat = UserStat.builder()
                    .users(savedUser)
                    .serviceTerm(1)
                    .lastLoginAt(now)
                    .lastLogoutAt(now)
                    .registeredChallenges(0)
                    .completedChallenges(0)
                    .registeredReviews(0)
                    .build();
            userStatRepo.save(newStat);

            Project newProject = Project.builder()
                    .users(savedUser)
                    .build();
            projectRepo.save(newProject);

            logger.info("[AuthTrans][{}] New user created. Role: {}", userId, isAdminReq ? "ADMIN" : "USER");
        }
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
       로그아웃
    ===========================*/
    @Transactional
    public void logoutProcess(String userId) {

        UserStat userStat = userStatRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("UserStat (" + userId + ") not found")
        );
        LocalDateTime now = LocalDateTime.now();
        userStatRepo.updateLastLogout(userId, now);
    }
}
