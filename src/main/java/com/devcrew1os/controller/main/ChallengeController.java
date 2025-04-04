package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.challenge.GetChallengesRes;
import com.devcrew1os.service.main.challenge.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/challenge")
public class ChallengeController {

    private final Response<?> response;
    private final ChallengeService challengeService;

    /*===========================
       도전과제 목록조회
    ===========================*/
    @PostMapping("/info/list")
    public ResponseEntity<?> getChallenges() {
        GetChallengesRes res = challengeService.getChallenges(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }
}
