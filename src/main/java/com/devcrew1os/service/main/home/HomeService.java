package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.home.*;
import com.devcrew1os.entity.challenge.ChallengeCategory;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.user.UserInfo;
import com.devcrew1os.repository.UserRepository;
import com.devcrew1os.repository.challenge.ChallengeCategoryRepository;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ChallengeCategoryRepository categoryRepo;
    private final ChallengeStatRepository statRepo;
    private final UserRepository userRepo;
    private final HomeTransaction homeTrans;

    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);

    /*===========================
       사용자 정보조회
    ===========================*/
    public HomeRes getHomes(HomeReq req) {
        HomeRes res = new HomeRes(false, false, "[Info] Get homes initiated", ErrorCode.OK);

        if(!isRequestValid(req, res)) return res;

        if(!isUserIdExist(req, res)) return res;

        HomeData data = getHomeData(req, res);
        if(data == null) {
            res.addMessage("[Failed] user data is unstable");
            logger.error("[HomeService][{}] failed to prepared home data", req.getUserId());
            return res;
        }

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully get home data");
        logger.info("[HomeService][{}] Successfully get home data", req.getUserId());
        return res;
    }

    private boolean isRequestValid(HomeReq req, HomeRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getUserId() == null || req.getUserId().isEmpty()) {
            errors.add("[Failed] UserID must not be null or empty");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[HomeService][{}] Invalid argument detected during getHomes: {}", req.getUserId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] GetChallengeList request is valid");
        logger.info("[HomeService][{}] getHomes request is valid", req.getUserId());
        return true;
    }

    private boolean isUserIdExist(HomeReq req, HomeRes res) {
        if (userRepo.existsByUserId(req.getUserId())) {
            res.addMessage("[Success] User(Info) exists");
            logger.info("[HomeService][{}] User exists, at getHomes", req.getUserId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] User(Info) not found");
        logger.warn("[HomeService][{}] User not found, at getHomes", req.getUserId());
        return false;
    }

    private HomeData getHomeData(HomeReq req, HomeRes res) {

        HomeUserData userData = getUserData(req);
        if (userData == null) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User(Info) not found");
            return null;
        }

        Map<Integer, String> categories = getChallengeCategories(req);
        if (categories == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Categories not found");
            return null;
        }

        List<HomeChallengeData> popularChallenges = getChallengeStats(req);
        if(popularChallenges == null) {
            res.setErrorCode(ErrorCode.FIREBASE_ERROR);
            res.addMessage("[Failed] ChallengeStat not found");
            return null;
        }

        logger.info("[HomeService][{}] Successfully prepared home data", req.getUserId());
        return new HomeData(userData, categories, popularChallenges);
    }

    private HomeUserData getUserData(HomeReq req) {

        try {
            UserInfo userData = homeTrans.getUserInfo(req);
            HomeUserData data = new HomeUserData(
                    userData.getUserName(),
                    userData.getStat().getTotalFinishedChallenges(),
                    userData.getStat().getTotalRegisteredChallenges()
            );
            logger.info("[HomeService][{}] Successfully retrieved userData", req.getUserId());
            return data;
        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search user data: {}", req.getUserId(), err.getMessage());
            return null;
        }
    }

    private Map<Integer, String> getChallengeCategories(HomeReq req) {

        Map<Integer, String> dataMap = new HashMap<>();
        try {
            List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
            for(ChallengeCategory category : categoryList) {
                dataMap.put(category.getId(), category.getTitle());
            }
            logger.info("[HomeService][{}] Successfully retrieved {} categories", req.getUserId(), categoryList.size());
            return dataMap;

        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search challenge category data", req.getUserId());
            return null;
        }
    }

    private List<HomeChallengeData> getChallengeStats(HomeReq req) {

        List<HomeChallengeData> dataList = new ArrayList<>();
        try {
            List<ChallengeStat> statList = statRepo.findTop10ByOrderByPopularityDesc();
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
            logger.info("[HomeService][{}] Successfully retrieved {} challenge data", req.getUserId(), dataList.size());
            return dataList;
        } catch (Exception err) {
            logger.error("[HomeService][{}] Failed to search challenge data", req.getUserId());
            return null;
        }
    }
}
