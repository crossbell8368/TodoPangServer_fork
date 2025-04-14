package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.challenge.*;
import com.devcrew1os.service.main.challenge.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/challenge")
public class ChallengeController {

    private final Response<?> response;
    private final ChallengeService service;

    /*===========================
       도전과제 목록조회
    ===========================*/
    @PostMapping("/info/list")
    public ResponseEntity<?> getChallenges() {
        ChallengeListRes res = service.getChallengeList(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       도전과제 상세조회
    ===========================*/
    @PostMapping("/info/detail")
    public ResponseEntity<?> getChallengeDetails(
            @RequestBody ChallengeDetailReq req
    ){
        ChallengeDetailRes res = service.getChallengeDetail(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    /*===========================
       도전과제 등록
    ===========================*/
    @PostMapping("/info/register")
    public ResponseEntity<?> registerChallenge(
            @RequestBody ChallengeRegisterReq req
    ) {
        ChallengeRegisterRes res = service.registerChallenge(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }
}
