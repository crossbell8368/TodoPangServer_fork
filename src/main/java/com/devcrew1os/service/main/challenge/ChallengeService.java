package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.challenge.GetChallengesData;
import com.devcrew1os.dto.main.challenge.GetChallengesInfo;
import com.devcrew1os.dto.main.challenge.GetChallengesRes;
import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetChallengesRes getChallenges(String userId) {
        GetChallengesRes res = new GetChallengesRes(false, false, "[Info] Get challenges initiated", ErrorCode.OK);

        GetChallengesData data = getChallengesData(userId, res);
        if (data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully get challenges");
        logger.info("[ChallengeService][{}] Successfully get challenges", userId);
        return res;
    }

    // Response fetch
    private GetChallengesData getChallengesData(String userId, GetChallengesRes res) {

        Map<Integer, String> categories = getCategories(userId);
        if(categories == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge categories, database error detected");
            return null;
        }

        List<GetChallengesInfo> infos = getInfos(userId);
        if(infos == null || infos.isEmpty()) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge data, database error detected");
            return null;
        }

        res.addMessage("[Info] Successfully get challenge categories & infos");
        return new GetChallengesData(categories, infos);
    }

    // Category fetch
    private Map<Integer, String> getCategories(String userId) {

        Map<Integer, String> categoryMap = new HashMap<>();
        try {
            List<ChallengeCategory> categoryList = transaction.getCategories();
            for(ChallengeCategory category : categoryList) {
                categoryMap.put(category.getId(), category.getTitle());
            }
            logger.info("[ChallengeService][{}] Successfully retrieved {} categories", userId, categoryList.size());
            return categoryMap;

        } catch (Exception err) {
            logger.error("[ChallengeService][{}] Failed to retrieved challenge category data: {}", userId, err.getMessage());
            return null;
        }
    }

    // Info fetch
    private List<GetChallengesInfo> getInfos(String userId) {

        List<GetChallengesInfo> infoDTOList = new ArrayList<>();
        try {
            List<ChallengeInfo> infoEntityList = transaction.getInfos();
            for(ChallengeInfo entity : infoEntityList) {
                infoDTOList.add(
                        new GetChallengesInfo(
                                entity.getId(),
                                entity.getCategory().getId(),
                                entity.getTitle(),
                                entity.getDiff(),
                                entity.getStat().getPopularity()
                        )
                );
            };
            logger.info("[ChallengeService][{}] Successfully retrieved {} challenge data", userId, infoDTOList.size());
            return infoDTOList;

        } catch (Exception err) {
            logger.error("[ChallengeService][{}] Failed to search challenge data: {}", userId, err.getMessage());
            return null;
        }
    }
}
