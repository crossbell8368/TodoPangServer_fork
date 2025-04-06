package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.home.*;
import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeStat;
import com.devcrew1os.entity.main.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeTransaction homeTrans;

    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);

    /*===========================
       사용자 정보조회
    ===========================*/
    public HomeRes getHomes(String userId) {
        HomeRes res = new HomeRes(false, "[Info] Get homes initiated", ErrorCode.OK);

        HomeData data = getHomeData(userId, res);
        if(data == null) {
            res.addMessage("[Failed] user data is unstable");
            logger.error("[HomeService][{}] failed to prepared home data", userId);
            return res;
        }

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully get home data");
        logger.info("[HomeService][{}] Successfully get home data", userId);
        return res;
    }

    private HomeData getHomeData(String userId, HomeRes res) {

        HomeUserData userData = getUserData(userId);
        if (userData == null) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User(Info) not found");
            return null;
        }

        Map<Integer, String> categories = getChallengeCategories(userId);
        if (categories == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Categories not found");
            return null;
        }

        List<HomeChallengeData> popularChallenges = getChallengeStats(userId);
        if(popularChallenges == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] ChallengeStat not found");
            return null;
        }

        logger.info("[HomeService][{}] Successfully prepared home data", userId);
        return new HomeData(userData, categories, popularChallenges);
    }

    private HomeUserData getUserData(String userId) {

        try {
            UserInfo userData = homeTrans.getUserInfo(userId);
            HomeUserData data = new HomeUserData(
                    userData.getUserName(),
                    userData.getStat().getTotalFinishedChallenges(),
                    userData.getStat().getTotalRegisteredChallenges()
            );
            logger.info("[HomeService][{}] Successfully retrieved userData", userId);
            return data;
        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search user data: {}", userId, err.getMessage());
            return null;
        }
    }

    private Map<Integer, String> getChallengeCategories(String userId) {

        Map<Integer, String> dataMap = new HashMap<>();
        try {
            List<ChallengeCategory> categoryList = homeTrans.getChallengeCategories();
            for(ChallengeCategory category : categoryList) {
                dataMap.put(category.getId(), category.getTitle());
            }
            logger.info("[HomeService][{}] Successfully retrieved {} categories", userId, categoryList.size());
            return dataMap;

        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search challenge category data: {}", userId, err.getMessage());
            return null;
        }
    }

    private List<HomeChallengeData> getChallengeStats(String userId) {

        List<HomeChallengeData> dataList = new ArrayList<>();
        try {
            List<ChallengeStat> statList = homeTrans.getChallengeStats();
            for(ChallengeStat stat : statList) {
                dataList.add(
                        HomeChallengeData.builder()
                                .title(stat.getChallengeInfo().getTitle())
                                .category(stat.getChallengeInfo().getCategory().getId())
                                .diff(stat.getChallengeInfo().getDiff())
                                .popularity(stat.getPopularity())
                                .build()
                );
            }
            logger.info("[HomeService][{}] Successfully retrieved {} challenge data", userId, dataList.size());
            return dataList;
        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search challenge data: {}", userId, err.getMessage());
            return null;
        }
    }
}
