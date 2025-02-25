package com.devcrew1os.controller;

import com.devcrew1os.common.util.Response;
import com.devcrew1os.dto.auth.*;
import com.devcrew1os.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final Response<?> response;
    private final AuthService authService;

    /*===========================
       회원가입
    ===========================*/
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupReq req
    ) {
        SignupRes res = authService.signup(req);
        return response.handleResult(res);
    }

    /*===========================
       로그인
    ===========================*/
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader("Authorization") String token,
            @RequestBody LoginReq req
    ) {
        req.setIdToken(token.substring(7));
        LoginRes res = authService.login(req);
        return response.handleResult(res);
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody WithdrawReq req
            ) {
        WithdrawRes res = authService.withdraw(req);
        return response.handleResult(res);
    }
}
