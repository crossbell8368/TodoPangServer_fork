package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.entity.admin.AdminChallenge;
import com.devcrew1os.repository.admin.AdminChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminChallengeTransaction {

    private final AdminChallengeRepository challengeRepo;

    /*===========================
       도전과제 목록
    ===========================*/
    public List<AdminChallenge> getAdminChallengeList() {
        List<AdminChallenge> challengeList = challengeRepo.findAllByOrderByIdDesc();
        if(challengeList.isEmpty()){
            throw new RuntimeException("Admin challenge list is empty");
        } else {
            return challengeList;
        }
    }
}
