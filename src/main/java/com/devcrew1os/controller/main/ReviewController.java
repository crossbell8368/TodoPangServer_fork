package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.review.GetReviewRes;
import com.devcrew1os.dto.main.review.UpdateReviewReq;
import com.devcrew1os.dto.main.review.UpdateReviewRes;
import com.devcrew1os.service.main.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/review")
public class ReviewController {

    private final Response<?> response;
    private final ReviewService service;

    /*===========================
       리뷰 조회
    ===========================*/
    @PostMapping("/list")
    public ResponseEntity<?> getReview() {
        GetReviewRes res = service.getReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       리뷰 등록
    ===========================*/
    @PostMapping("/update")
    public ResponseEntity<?> updateReview(
            @RequestBody UpdateReviewReq req
    ) {
        UpdateReviewRes res = service.updateReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }
}
