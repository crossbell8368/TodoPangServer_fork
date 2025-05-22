package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.mypage.*;
import com.devcrew1os.service.main.mypage.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/mypage")
public class MypageController {

    private final MypageService service;
    private final Response<?> response;

    /*===========================
       계정이름 변경
    ===========================*/
    @PostMapping("/rename")
    public ResponseEntity<?> updateUserName(
            @RequestBody UpdateUserNameReq req
    ) {
        UpdateUserNameRes res = service.updateUserName(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    /*===========================
       사용자 의견등록
    ===========================*/
    @PostMapping("/opinion")
    public ResponseEntity<?> registerUserOpinion(
            @RequestBody AddUserOpinionReq req
    ) {
        AddUserOpinionRes res = service.addUserOpinion(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    /*===========================
       회원탈퇴 사유조회
    ===========================*/
    @GetMapping("/withdraw/reason")
    public ResponseEntity<?> withdrawUser() {
        GetWithdrawReasonRes res = service.getWithdrawReason(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @PostMapping("/withdraw/process")
    public ResponseEntity<?> withdraw(
            @RequestBody WithdrawReq req
    ) {
        WithdrawRes res = service.withdraw(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }
}
