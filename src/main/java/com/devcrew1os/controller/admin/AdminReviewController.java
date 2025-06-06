package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.review.*;
import com.devcrew1os.service.admin.review.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/review")
public class AdminReviewController {

    private final Response<?> response;
    private final AdminReviewService service;

    /*===========================
       리뷰 목록조회
    ===========================*/
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchReviews(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        GetAdminReviewRes res = service.getAdminReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                pageable
        );
        return response.handleResult(res);
    }

    /*===========================
       리뷰 추가
    ===========================*/
    @PostMapping("/add")
    public ResponseEntity<?> addReview(
            @RequestBody SetAdminReviewReq req
    ) {
        SetAdminReviewRes res = service.setAdminReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }

    /*===========================
       리뷰 업데이트
    ===========================*/
    @PostMapping("/update")
    public ResponseEntity<?> updateReview(
            @RequestBody UpdateAdminReviewReq req
    ) {
        UpdateAdminReviewRes res = service.updateAdminReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }

    /*===========================
       리뷰 배포
    ===========================*/
    @PostMapping("/deploy")
    public ResponseEntity<?> deployReview(
            @RequestBody DeployAdminReviewReq req
    ) {
        DeployAdminReviewRes res = service.deployAdminReview(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }
}
