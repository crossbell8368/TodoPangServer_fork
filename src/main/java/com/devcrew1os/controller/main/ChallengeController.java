package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.challenge.GetChallengesReq;
import com.devcrew1os.dto.main.challenge.GetChallengesRes;
import com.devcrew1os.service.main.challenge.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge")
public class ChallengeController {

    private final Response<?> response;
    private final ChallengeService challengeService;

    /*===========================
       도전과제 목록조회
    ===========================*/
    @PostMapping("/info/list")
    public ResponseEntity<?> getChallenges(
            @RequestBody GetChallengesReq req
    ) {
        GetChallengesRes res = challengeService.getChallenges(req);
        return response.handleResult(res);
    }
}
