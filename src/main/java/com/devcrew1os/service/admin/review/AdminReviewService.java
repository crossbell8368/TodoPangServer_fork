package com.devcrew1os.service.admin.review;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.PageResponse;
import com.devcrew1os.dto.admin.review.*;
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
public class AdminReviewService {

    private final AdminReviewTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(AdminReviewService.class);

    /*===========================
       리뷰 조회
    ===========================*/
    public GetAdminReviewRes getAdminReview(String adminId, Pageable pageable) {
        GetAdminReviewRes res = new GetAdminReviewRes(false, "[Info] Get admin reviews initiated", ErrorCode.OK);

        try {
            Page<GetAdminReviewData> dataPage = transaction.getAdminReviewProcess(pageable);
            PageResponse<GetAdminReviewData> pageResponse = new PageResponse<>(dataPage);

            res.setData(pageResponse);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully retrieved reviews");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] Failed to get review data.");
            logger.error("[AdminReview][{}] Failed to retrieved admin reviews: {}", adminId, err.getMessage());
            return res;
        }
    }

    /*===========================
       리뷰 추가
    ===========================*/
    public SetAdminReviewRes setAdminReview(String adminId, SetAdminReviewReq req) {
        SetAdminReviewRes res = new SetAdminReviewRes(false, "[Info] Set admin reviews initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setReviewProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully set admin review");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to set new review data.");
            logger.error("[AdminReview][{}] Set new review failed: {}", adminId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, SetAdminReviewReq req, SetAdminReviewRes res) {
        List<String> errors = new ArrayList<>();

        if(req.getTitle() == null || req.getTitle().isEmpty()) {
            errors.add("[Error] Title required");
        }
        if(req.getEmoji() == null) {
            errors.add("[Error] Emoji required");
        }
        if(!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminReview][{}] Invalid argument detected, at set review request: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Set review request is valid");
        return true;
    }

    /*===========================
       리뷰 업데이트
    ===========================*/
    public UpdateAdminReviewRes updateAdminReview(String adminId, UpdateAdminReviewReq req) {
        UpdateAdminReviewRes res = new UpdateAdminReviewRes(false, "[Info] Update admin reviews initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.updateReviewProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully updated admin review");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminReview][{}] {}", adminId, err.getMessage());
            return res;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Review data not updated: " + err.getMessage());
            logger.error("[AdminReview][{}] Failed to update review data: {}", adminId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, UpdateAdminReviewReq req, UpdateAdminReviewRes res) {
        List<String> errors = new ArrayList<>();

        if(req.getReviewId() == null){
            errors.add("[Error] Target reviewId required");
        }
        if(req.getNewTitle() == null || req.getNewTitle().isEmpty()) {
            errors.add("[Error] NewTitle required");
        }
        if(req.getNewEmoji() == null) {
            errors.add("[Error] NewEmoji required");
        }
        if(!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminReview][{}] Invalid argument detected, at update review: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Update review request is valid");
        return true;
    }

    /*===========================
       리뷰 배포
    ===========================*/
    public DeployAdminReviewRes deployAdminReview(String adminId, DeployAdminReviewReq req) {
        DeployAdminReviewRes res = new DeployAdminReviewRes(false, "[Info] Deploy admin reviews initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;
        try {
            transaction.deployReviewProcess(adminId, req, res);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully deployed admin review");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminReview][{}] {}", adminId, err.getMessage());
            return res;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Review data not deployed: " + err.getMessage());
            logger.error("[AdminReview][{}] Failed to deploy review data: {}", adminId, err.getMessage());
            return res;
        }
    }
    private boolean isRequestValid(String adminId, DeployAdminReviewReq req, DeployAdminReviewRes res) {
        List<String> errors = new ArrayList<>();

        for(DeployAdminReviewData data : req.getData()) {
            if(data.getReviewId() == null) {
                errors.add("[Error] Target reviewLid required");
            }
            if(data.getNewStatus() == null) {
                errors.add("[Error] NewStatus required");
            }
        }
        if(!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminReview][{}] Invalid argument detected, at deploy review: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Deploy review request is valid");
        return true;
    }
}
