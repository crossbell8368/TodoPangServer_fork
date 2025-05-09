package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.main.home.HomeChallengeData;
import com.devcrew1os.dto.main.home.HomeData;
import com.devcrew1os.dto.main.home.HomeUserData;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.repository.main.challenge.CategoryRepository;
import com.devcrew1os.repository.main.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.main.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeTransaction {

    private final UsersRepository userRepo;
    private final ChallengeStatRepository statRepo;
    private final CategoryRepository categoryRepo;

    /*===========================
       홈화면 데이터 조회
    ===========================*/
    @Transactional(readOnly = true)
    public HomeData getHomeData(String userId) {
        // 1. fetch data
        Users user = userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User not found with id " + userId)
        );

        List<Category> categoryList = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        }
        Pageable top10 = PageRequest.of(0, 10);
        List<ChallengeStat> statList = statRepo.findTop10ByPopularityWithDetail(DataStatus.DEPLOYED.getValue(), top10);
        if(statList.isEmpty()){
            throw new RuntimeException("Stat list is empty");
        }

        // 2. struct data
        HomeUserData userData = new HomeUserData(
                user.getUserName(),
                user.getStat().getCompletedChallenges(),
                user.getStat().getServiceTerm()
        );
        Map<Integer, String> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle)
                );
        List<HomeChallengeData> challengeList = statList.stream()
                .map(stat -> HomeChallengeData.builder()
                        .challengeId(stat.getChallenge().getId())
                        .title(stat.getChallenge().getTitle())
                        .category(stat.getChallenge().getCategory().getId())
                        .diff(stat.getChallenge().getDiff())
                        .popularity(stat.getParticipateUserCount())
                        .build()
                ).collect(Collectors.toList());

        return new HomeData(userData, categoryMap, challengeList);
    }
}
