package com.devcrew1os.service.main.review;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.review.GetReviewData;
import com.devcrew1os.dto.main.review.GetReviewRes;
import com.devcrew1os.dto.main.review.UpdateReviewReq;
import com.devcrew1os.dto.main.review.UpdateReviewRes;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    /*===========================
       리뷰 조회
    ===========================*/
    public GetReviewRes getReview(String userId) {
        GetReviewRes res = new GetReviewRes(false, "[Info] Get review initiated", ErrorCode.OK);

        try {
            List<GetReviewData> dataList = transaction.getReviewProcess();
            res.setData(dataList);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully get reviewList");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] ReviewList not found");
            logger.error("[ReviewService][{}] ", userId, err);
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected, while get reviewList");
            logger.error("[ReviewService][{}] ", userId, err);
            return res;
        }
    }

    /*===========================
       리뷰 업데이트
    ===========================*/
    public UpdateReviewRes updateReview(String userId, UpdateReviewReq req) {
        UpdateReviewRes res = new UpdateReviewRes(false, "[Info] Update review initiated", ErrorCode.OK);

        try {
            transaction.updateReview(userId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully update review");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected, while update review");
            logger.error("[ReviewService][{}] {}", userId, err.getMessage());
            return res;
        }
    }
}
