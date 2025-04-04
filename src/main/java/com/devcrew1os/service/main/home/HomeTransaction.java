package com.devcrew1os.service.main.home;

import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeStat;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.challenge.ChallengeCategoryRepository;
import com.devcrew1os.repository.main.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeTransaction {

    private final UserInfoRepository userRepo;
    private final ChallengeStatRepository statRepo;
    private final ChallengeCategoryRepository categoryRepo;

    public UserInfo getUserInfo(String userId) {
        return userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
    }

    public List<ChallengeCategory> getChallengeCategories() {
        List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        } else {
            return categoryList;
        }
    }

    public List<ChallengeStat> getChallengeStats() {
        List<ChallengeStat> statList = statRepo.findTop10ByOrderByPopularityDesc();
        if(statList.isEmpty()){
            throw new RuntimeException("Stat list is empty");
        } else {
            return statList;
        }
    }
}
