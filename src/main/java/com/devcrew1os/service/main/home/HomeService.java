package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.home.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);

    /*===========================
       사용자 정보조회
    ===========================*/
    public HomeRes getHomes(String userId) {
        HomeRes res = new HomeRes(false, "[Info] Get homes initiated", ErrorCode.OK);

        try {
            HomeData data = transaction.getHomeData(userId);
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully get home data");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] Home related data not found: " + err.getMessage());
            logger.error("[HomeService][{}] {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected while getting home data");
            logger.error("[HomeService][{}] {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       도전과제 목록 조회
    ===========================*/
    public HomeRankingRes getHomeRanking(String userId) {
        HomeRankingRes res = new HomeRankingRes(false, "[Info] Get home ranking initiated", ErrorCode.OK);

        try {
            List<HomeChallengeData> data = transaction.getHomeRanking(userId);
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully get home ranking data");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Error] Failed to get Home ranking related data: " + err.getMessage());
            logger.error("[HomeService][{}]", userId, err);
            return res;
        }
    }
}
