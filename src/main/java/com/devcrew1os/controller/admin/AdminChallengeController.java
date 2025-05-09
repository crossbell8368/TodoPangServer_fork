package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.challenge.*;
import com.devcrew1os.service.admin.challenge.AdminChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/challenge")
public class AdminChallengeController {

    private final Response<?> response;
    private final AdminChallengeService service;

    /*===========================
       도전과제 목록조회
    ===========================*/
    @GetMapping("/list")
    public ResponseEntity<?> getChallenges(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        GetAdminChallengeRes res = service.getAdminChallenges(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                pageable
        );
        return response.handleResult(res);
    }

    /*===========================
       도전과제 등록
    ===========================*/
    @PostMapping("/add")
    public ResponseEntity<?> setChallenge(
            @RequestBody SetAdminChallengeReq req
    ) {
        SetAdminChallengeRes res = service.setAdminChallenges(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    @PostMapping("/update")
    public ResponseEntity<?> updateChallenge(
            @RequestBody UpdateAdminChallengeReq req
    ){
        UpdateAdminChallengeRes res = service.updateAdminChallenges(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }
}
