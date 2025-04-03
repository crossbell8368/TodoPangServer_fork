package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.auth.*;
import com.devcrew1os.service.main.auth.AuthService;
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
            @RequestHeader("Authorization") String token,
            @RequestBody WithdrawReq req
            ) {
        WithdrawRes res = authService.withdraw(token.substring(7), req);
        return response.handleResult(res);
    }
}
