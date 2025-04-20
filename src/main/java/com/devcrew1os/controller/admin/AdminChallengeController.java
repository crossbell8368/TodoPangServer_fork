package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeRes;
import com.devcrew1os.service.admin.challenge.AdminChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/challenge")
public class AdminChallengeController {

    private final Response<?> response;
    private final AdminChallengeService service;

    /*===========================
       도전과제 목록조회
    ===========================*/
    @PostMapping("/fetch")
    public ResponseEntity<?> getChallenges() {
        GetAdminChallengeRes res = service.getAdminChallenges(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }
}
