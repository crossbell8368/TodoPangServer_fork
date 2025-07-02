package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.challenge.*;
import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.service.main.challenge.ChallengeCacheManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChallengeService {

    private final ChallengeCacheManager cacheManager;
    private final AdminChallengeTransaction transaction;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetAdminChallengeRes getAdminChallenges(String adminId, Pageable pageable) {
        GetAdminChallengeRes res = new GetAdminChallengeRes(false, "[Info] Get admin challenges initiated", ErrorCode.OK);

        try {
            GetAdminChallengeData data = transaction.getChallengeProcess(pageable);
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully retrieved challenges");
            return res;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] Failed to get challenge data.");
            logger.error("[AdminChallenge][{}] Failed to retrieved admin challenges: ", adminId, err);
            return res;
        }
    }

    /*===========================
       도전과제 등록
    ===========================*/
    public SetAdminChallengeRes setAdminChallenge(String adminId, SetAdminChallengeReq req) {
        SetAdminChallengeRes res = new SetAdminChallengeRes(false, "[Info] Set admin challenges initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setChallengeProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully add challenge");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected while add challenge");
            logger.error("[AdminCategory][{}] Failed to add challenge: {}", adminId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, SetAdminChallengeReq req, SetAdminChallengeRes res) {
        List<String> errors = new ArrayList<>();

        if(req.getTitle() == null || req.getTitle().isEmpty()) {
            errors.add("[Error] Title is required");
        }
        if(req.getCategoryId() == null) {
            errors.add("[Error] Category is required");
        }
        if(req.getDiff() == null) {
            errors.add("[Error] Diff is required");
        }
        if(req.getTerm() == null) {
            errors.add("[Error] Term is required");
        }

        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminChallenge][{}] Invalid argument detected, at add challenge: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Add challenge request is valid");
        return true;
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    public UpdateAdminChallengeRes updateAdminChallenge(String adminId, UpdateAdminChallengeReq req) {
        UpdateAdminChallengeRes res = new UpdateAdminChallengeRes(false, "[Info] Update admin challenges initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.updateChallengeProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully update challenge");
            return res;

        } catch (RuntimeException err){
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] Invalid argument detected, at update admin challenges: "+ err.getMessage());
            logger.error("[AdminChallenge][{}] Invalid argument detected, at update admin challenges: {}", adminId, err.getMessage());
            return res;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] ServerError detected, at update admin challenges");
            logger.error("[AdminChallenge][{}] ServerError detected, at update admin challenges: {}", adminId, err.getMessage());
            return res;
        }
    }

    // Check necessary field
    private boolean isRequestValid(String adminId, UpdateAdminChallengeReq req, UpdateAdminChallengeRes res) {
        List<String> errors = new ArrayList<>();

        if(req.getChallengeId() == null) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Error] Target ChallengeId required");
            logger.error("[AdminChallenge][{}] ChallengeID is null, during update", adminId);
            return false;
        }

        if(!req.getTodosToAdd().isEmpty()) {
            for(UpdateAdminChallengeTodo addTodo : req.getTodosToAdd()) {
                if(addTodo.getNewTodoOrder() == null){
                    errors.add("[Error] Todo order is null, at Add Todo");
                }
                if(addTodo.getNewTodoTitle() == null){
                    errors.add("[Error] Todo title is null, at Add Todo");
                }
            }
        }
        if(!req.getTodosToUpdate().isEmpty() || !req.getTodosToDelete().isEmpty()) {
            for(UpdateAdminChallengeTodo updateTodo : req.getTodosToUpdate()) {
                if(updateTodo.getTodoId() == null){
                    errors.add("[Error] TodoId is null, at Update Todo");
                }
            }
        }
        if(!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            String errorMessage = String.join("\n", errors);
            res.addMessage(errorMessage);
            logger.error("[AdminChallenge][{}] Invalid arguments detected during challenge update validation:\n{}", adminId, errorMessage);
            return false;
        }
        return true;
    }

    /*===========================
       도전과제 배포
    ===========================*/
    public DeployAdminChallengeRes deployAdminChallenges(String adminId, DeployAdminChallengeReq req) {
        DeployAdminChallengeRes res = new DeployAdminChallengeRes(false, "[Info] Deploy admin challenges initiated", ErrorCode.OK);
        LocalDateTime now = LocalDateTime.now();

        if(!isRequestValid(adminId, req, res)) return res;
        Set<Integer> targetChallengeIds = extractIdsFromDto(req);

        try {
            List<Challenge> targetChallengeData = transaction.getRequestChallenges(targetChallengeIds, adminId);
            transaction.deployChallengeProcess(targetChallengeData, req, res, now, adminId);
            updateCache(req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully deploy admin challenges");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminChallenge][{}] Failed to deploy challenge data: {}", adminId, err.getMessage());
            return res;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Review data not deployed: " + err.getMessage());
            logger.error("[AdminChallenge][{}] Failed to deploy challenge data: {}", adminId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, DeployAdminChallengeReq req, DeployAdminChallengeRes res) {
        List<String> errors = new ArrayList<>();

        for(DeployAdminChallengeData dto : req.getData()){
            if(dto.getChallengeId() == null){
                errors.add("[Error] Target ChallengeId required");
            }
            if(dto.getNewStatus() == null){
                errors.add("[Error] New Status required");
            }
        }
        if(!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            String errorMessage = String.join("\n", errors);
            res.addMessage(errorMessage);
            logger.error("[AdminChallenge][{}] Invalid arguments detected during challenge deploy validation:\n{}", adminId, errorMessage);
            return false;
        }
        return true;
    }

    private Set<Integer> extractIdsFromDto(DeployAdminChallengeReq req) {
        return req.getData().stream()
                .map(DeployAdminChallengeData::getChallengeId)
                .collect(Collectors.toSet());
    }

    private void updateCache(DeployAdminChallengeReq req) {
        // 1. group by status
        Map<DataStatus, List<DeployAdminChallengeData>> groupByStatus = req.getData().stream()
                .collect(Collectors.groupingBy(dto -> DataStatus.fromValue(dto.getNewStatus())));

        // 2. proceed update by status
        for(Map.Entry<DataStatus, List<DeployAdminChallengeData>> entry : groupByStatus.entrySet()) {
            DataStatus newStatus = entry.getKey();
            List<DeployAdminChallengeData> dtoList = entry.getValue();
            List<Integer> challengeIds = dtoList.stream()
                    .map(DeployAdminChallengeData::getChallengeId).collect(Collectors.toList());

            switch (newStatus) {
                case DEPLOYED:
                    cacheManager.addChallengeData(challengeIds);
                    break;
                case PREPARE:
                    cacheManager.deleteChallenges(challengeIds);
                    break;
                default:
                    break;
            }
        }
    }
}
