package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.challenge.*;
import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    /*===========================
       도전과제 목록조회
    ===========================*/
    public ChallengeListRes getChallengeList(String userId) {
        ChallengeListRes res = new ChallengeListRes(false, "[Info] Get challenges initiated", ErrorCode.OK);

        ChallengeListData data = getChallengesData(userId, res);
        if (data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully get challenges");
        logger.info("[ChallengeService][{}] Successfully get challenges", userId);
        return res;
    }

    // Response fetch
    private ChallengeListData getChallengesData(String userId, ChallengeListRes res) {

        Map<Integer, String> categories = getCategories(userId);
        if(categories == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge categories, database error detected");
            return null;
        }

        List<ChallengeListInfo> infos = getInfos(userId);
        if(infos == null || infos.isEmpty()) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge data, database error detected");
            return null;
        }

        res.addMessage("[Info] Successfully get challenge categories & infos");
        return new ChallengeListData(categories, infos);
    }

    // Category fetch
    private Map<Integer, String> getCategories(String userId) {

        Map<Integer, String> categoryMap = new HashMap<>();
        try {
            List<ChallengeCategory> categoryList = transaction.getCategoryList();
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
    private List<ChallengeListInfo> getInfos(String userId) {

        List<ChallengeListInfo> infoDTOList = new ArrayList<>();
        try {
            List<ChallengeInfo> infoEntityList = transaction.getInfoList();
            for(ChallengeInfo entity : infoEntityList) {
                infoDTOList.add(
                        new ChallengeListInfo(
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

    /*===========================
       도전과제 상세조회
    ===========================*/
    public ChallengeDetailRes getChallengeDetail(String userId, ChallengeDetailReq req) {
        ChallengeDetailRes res = new ChallengeDetailRes(false, "[Info] Fetch challenge detail initiated", ErrorCode.OK);

        // 1. req 검증
        if(!isRequestValid(userId, req, res)) return res;

        // 2. 응답객체 준비
        ChallengeDetailData data = getChallengeDetailData(userId, req, res);
        if(data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully fetch challenge detail");
        logger.info("[ChallengeService][{}] Successfully fetch challenge detail", userId);
        return res;
    }

    private boolean isRequestValid(String userId, ChallengeDetailReq req, ChallengeDetailRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getChallengeId() == null) {
            errors.add("[Failed] ChallengeId must not be null");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[ChallengeService][{}] Invalid argument detected, at fetch challenge detail: {}", userId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Withdraw request");
        logger.info("[ChallengeService][{}] Valid fetch challenge detail request", userId);
        return true;
    }

    private ChallengeDetailData getChallengeDetailData(String userId, ChallengeDetailReq req, ChallengeDetailRes res) {
        ChallengeDetailData data = new ChallengeDetailData();
        try {
            data = transaction.getChallengeDetailData(req.getChallengeId());
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to fetch challenge data");
            logger.error("[ChallengeService][{}] Failed to fetch challenge detail: {}", userId, err.getMessage());
            return null;
        }
        return data;
    }
}
