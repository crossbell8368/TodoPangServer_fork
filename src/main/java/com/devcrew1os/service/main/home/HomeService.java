package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.home.*;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.user.Users;
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
            logger.info("[HomeService][{}] Successfully get home data", userId);
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
}
