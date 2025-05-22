package com.devcrew1os.service.main.mypage;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.main.mypage.AddUserOpinionReq;
import com.devcrew1os.dto.main.mypage.GetWithdrawReasonData;
import com.devcrew1os.entity.user.UserWithdrawReason;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.entity.user.UsersOpinion;
import com.devcrew1os.repository.main.users.UserWithdrawReasonRepository;
import com.devcrew1os.repository.main.users.UsersOpinionRepository;
import com.devcrew1os.repository.main.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageTransaction {

    private final UsersRepository userRepo;
    private final UsersOpinionRepository userOpinionRepo;
    private final UserWithdrawReasonRepository userWithdrawReasonRepo;

    private static final Logger logger = LoggerFactory.getLogger(MypageTransaction.class);

    /*===========================
       계정명 변경
    ===========================*/
    @Transactional
    public boolean renameUserProcess(String userId, String newUserName) {
        // 1. fetch data
        Optional<Users> userOpt = userRepo.findByUserId(userId);
        if(userOpt.isEmpty()){
            throw new RuntimeException("User not found");
        }
        // 2. check request validation
        Users user = userOpt.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);
        if(user.getUpdatedAt().isAfter(tenDaysAgo)) {
            return false;
        }
        // 2. Update
        user.setUserName(newUserName);

        // 3. record
        user.setUpdatedAt(now);
        userRepo.save(user);
        return true;
    }

    /*===========================
       사용자 의견 등록
    ===========================*/
    @Transactional
    public boolean addUserOpinionProcess(String userId, AddUserOpinionReq req) {
        // 1. fetch data
        Optional<UsersOpinion> userOpinionOpt = userOpinionRepo.findLastRegisteredByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        // 2. check register condition
        if(userOpinionOpt.isPresent()){
            UsersOpinion userOpinion = userOpinionOpt.get();
            LocalDateTime monthAgo = now.minusDays(30);
            if(userOpinion.getLastRegisteredAt().isAfter(monthAgo)){
                logger.warn("[MypageService][{}] Recent add user opinion exist, last registered at: {}, attempt to register at: {}", userId, userOpinion.getLastRegisteredAt(), now);
                return false;
            }
        }

        // 3. register opinion
        Users userReference = userRepo.getReferenceById(userId);
        UsersOpinion userOpinion = UsersOpinion.builder()
                .satisfactionRatio(req.getSatisfiedRating())
                .opinionDesc(req.getComment())
                .lastRegisteredAt(now)
                .user(userReference)
                .build();
        userOpinionRepo.save(userOpinion);
        return true;
    }

    /*===========================
       탈퇴사유 조회
    ===========================*/
    @Transactional(readOnly = true)
    public List<GetWithdrawReasonData> getWithdrawReason() {
        // 1. fetch data
        List<UserWithdrawReason> reasonList = userWithdrawReasonRepo.findAllByStatus(DataStatus.DEPLOYED.getValue());
        if(reasonList.isEmpty()){
            throw new RuntimeException("User withdraw reason not found");
        }

        // 2. struct data
        return reasonList.stream()
                .map(entity -> new GetWithdrawReasonData(entity.getId(), entity.getDesc()))
                .collect(Collectors.toList());
    }

    /*===========================
        회원탈퇴
    ===========================*/
    public Users getUsersByUserId(String userId) {
        return userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Users not found: " + userId)
        );
    }

    @Transactional
    public void updateWithdrawData(Users user) {
        try {
            userRepo.save(user);
        } catch (Exception err) {
            throw new RuntimeException("Withdraw transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }
}
