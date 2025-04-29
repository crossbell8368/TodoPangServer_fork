package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.PageResponse;
import com.devcrew1os.dto.admin.challenge.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminChallengeService {

    private final AdminChallengeTransaction transaction;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetAdminChallengeRes getAdminChallenges(String adminId, Pageable pageable) {
        GetAdminChallengeRes res = new GetAdminChallengeRes(false, "[Info] Get admin challenges initiated", ErrorCode.OK);

        try {
            Page<GetAdminChallengeData> dataPage = transaction.getChallengeProcess(pageable);

            PageResponse<GetAdminChallengeData> pageResponse = new PageResponse<>(dataPage);

            res.setData(pageResponse);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully retrieved challenges");
            logger.info("[AdminCategory][{}] Successfully retrieved challenges", adminId);
            return res;

        } catch (RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] Admin challenges not found");
            logger.info("[AdminChallenge][{}] Failed to retrieved challenges: {}", adminId, err.getMessage());
            return res;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] Failed to get challenge data.");
            logger.info("[AdminChallenge][{}] Failed to retrieved admin challenges: {}", adminId, err.getMessage());
            return res;
        }
    }

    /*===========================
       도전과제 추가
    ===========================*/
    public SetAdminChallengeRes setAdminChallenges(String adminId, SetAdminChallengeReq req) {
        SetAdminChallengeRes res = new SetAdminChallengeRes(false, "[Info] Set admin challenges initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setChallengeProcess(adminId, req);

            res.setSuccess(true);
            res.addMessage("[Info] Successfully add challenge");
            logger.info("[AdminChallenge][{}] Successfully add challenge", adminId);
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
        logger.info("[AdminChallenge][{}] Add challenge request is valid", adminId);
        return true;
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    public UpdateAdminChallengeRes updateAdminChallenges(String adminId, UpdateAdminChallengeReq req) {
        UpdateAdminChallengeRes res = new UpdateAdminChallengeRes(false, "[Info] Update admin challenges initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.updateChallengeProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully update challenge");
            logger.info("[AdminChallenge][{}] Successfully update challenge", adminId);
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

    private boolean isRequestValid(String adminId, UpdateAdminChallengeReq req, UpdateAdminChallengeRes res) {
        List<String> errors = new ArrayList<>();

        if(req == null || req.getUpdatedChallenges() == null || req.getUpdatedChallenges().isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Error] UpdatedChallenges is required");
            logger.error("[AdminChallenge][{}] Invalid argument detected, at update challenge: {}", adminId, res.getMessage());
            return false;
        }

        List<UpdateAdminChallengeData> challengesToUpdate = req.getUpdatedChallenges();
        for (int i = 0; i < challengesToUpdate.size(); i++) {
            UpdateAdminChallengeData challenge = challengesToUpdate.get(i);

            if (challenge == null) {
                errors.add("[Error] Challenge data at index " + i + " is null.");
                continue;
            }
            if (challenge.getChallengeId() == null) {
                errors.add("[Error] Challenge ID is required (at index " + i + ").");
                continue;
            }
            if (challenge.getCategoryId() == null) {
                errors.add("[Error] Category ID is required (at index " + i + ").");
                continue;
            }

            List<UpdateAdminChallengeTodoData> todoList = challenge.getNewTodoList();
            if (todoList != null) {
                for (int j = 0; j < todoList.size(); j++) {
                    UpdateAdminChallengeTodoData todo = todoList.get(j);

                    if (todo == null) {
                        String challengeContext = "challenge " + challenge.getChallengeId() + " at index " + i;
                        errors.add("[Error] Todo data at index " + j + " within " + challengeContext + " is null.");
                        continue;
                    }
                    if (todo.getTodoId() == null) {
                        String challengeContext = "challenge " + challenge.getChallengeId() + " at index " + i;
                        errors.add("[Error] Todo ID is required (at index " + j + " within " + challengeContext + ").");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            String errorMessage = String.join("\n", errors);
            res.addMessage(errorMessage);
            logger.error("[AdminChallenge][{}] Invalid arguments detected during challenge update validation:\n{}", adminId, errorMessage);
            return false;
        }
        res.addMessage("[Success] Update challenge request is valid");
        logger.info("[AdminChallenge][{}] Update challenge request is valid", adminId);
        return true;
    }
}
