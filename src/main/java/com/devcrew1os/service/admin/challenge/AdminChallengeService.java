package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.AdminStatus;
import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeData;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeRes;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeReq;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeRes;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.entity.admin.AdminChallenge;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChallengeService {

    private final AdminChallengeTransaction transaction;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetAdminChallengeRes getAdminChallenges(String adminId) {
        GetAdminChallengeRes res = new GetAdminChallengeRes(false, "[Info] Get admin challenges initiated", ErrorCode.OK);

        List<GetAdminChallengeData> data = getChallengeData(adminId, res);
        if(data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully retrieved challenges");
        logger.info("[AdminCategory][{}] Successfully retrieved challenges", adminId);
        return res;
    }

    private List<GetAdminChallengeData> getChallengeData(String adminId, GetAdminChallengeRes res) {
        try {
            List<GetAdminChallengeData> adminChallenges = transaction.getAdminChallengeList().stream()
                    .map(challenge -> new GetAdminChallengeData(
                            challenge.getId(),
                            challenge.getCategory().getTitle(),
                            challenge.getTerm(),
                            challenge.getDiff(),
                            challenge.getTodoCount(),
                            challenge.getStatus(),
                            challenge.getLastUpdatedAt(),
                            challenge.getLastUpdatedBy()
                    ))
                    .collect(Collectors.toList());
            logger.info("[AdminChallenge][{}] Successfully retrieved {} challenges", adminId, adminChallenges.size());
            return adminChallenges;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected while retrieving admin challenges");
            logger.info("[AdminChallenge][{}] Failed to retrieved challenges", adminId);
            return null;
        }
    }

    /*===========================
       도전과제 등록
    ===========================*/
    public SetAdminChallengeRes setAdminChallenges(String adminId, SetAdminChallengeReq req) {
        SetAdminChallengeRes res = new SetAdminChallengeRes(false, "[Info] Set admin challenges initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setAdminChallengeProcess(adminId, req);

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
}
