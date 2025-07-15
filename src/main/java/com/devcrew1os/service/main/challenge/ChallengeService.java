package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.challenge.*;
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

        try {
            ChallengeListData data = transaction.getChallengeProcess(userId);
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully get challenges");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] ChallengeList related data not found: " + err.getMessage());
            logger.error("[ChallengeService][{}] ChallengeList related data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected, while access database");
            logger.error("[ChallengeService][{}] Error detected, while access database at 'GetChallengeList': {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       도전과제 상세조회
    ===========================*/
    public ChallengeDetailRes getChallengeDetail(String userId, ChallengeDetailReq req) {
        ChallengeDetailRes res = new ChallengeDetailRes(false, "[Info] Fetch challenge detail initiated", ErrorCode.OK);

        // 1. req 검증
        if(!isRequestValid(userId, req, res)) return res;

        try {
            ChallengeDetailData data = transaction.getChallengeDetailProcess(userId, req.getChallengeId());
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully fetch challenge detail");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] Challenge related data not found: " + err.getMessage());
            logger.error("[ChallengeService][{}] Challenge related data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected, while access database");
            logger.error("[ChallengeService][{}] Error detected, while access database at 'GetChallengeDetail': {}", userId, err.getMessage());
            return res;
        }
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
        res.addMessage("[Success] Valid get challenge detail request");
        return true;
    }

    /*===========================
       도전과제 등록
    ===========================*/
    public ChallengeRegisterRes registerChallenge(String userId, ChallengeRegisterReq req) {
        ChallengeRegisterRes res = new ChallengeRegisterRes(false, "[Info] Challenge register initiated", ErrorCode.OK);

        if(!isRequestValid(userId, req, res)) return res;

        try {
            if(!transaction.registerChallenge(userId, req)){
                res.setErrorCode(ErrorCode.BAD_REQUEST);
                res.addMessage("[Failed] Already registered challenge or todos");
                return res;
            } else {
                res.setSuccess(true);
                res.addMessage("[Info] Successfully registered challenge data");
                logger.info("[ChallengeService][{}] Successfully register challenge data", userId);
                return res;
            }

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[ChallengeService][{}] Failed to register challenge: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[ChallengeService][{}] Server error detected at register challenge: {}", userId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String userId, ChallengeRegisterReq req, ChallengeRegisterRes res) {
        List<String> errors = new ArrayList<>();

        // 1. Request field check
        if(req.getChallengeId() == null) {
            errors.add("[Failed] ChallengeId must not be null");
        }
        if(req.getTodoIds() == null || req.getTodoIds().isEmpty()) {
            errors.add("[Failed] TodoId list must not be null or empty");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[ChallengeService][{}] Invalid argument detected, at register todos: {}", userId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Todo register request is valid");
        return true;
    }
}
