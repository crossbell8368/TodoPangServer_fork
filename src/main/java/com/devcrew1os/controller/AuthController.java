package com.devcrew1os.controller;

import com.devcrew1os.common.util.Response;
import com.devcrew1os.dto.auth.*;
import com.devcrew1os.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
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
            @Parameter(name = "Authorization", description = "FirebaseAuth IdToken", required = true, in = ParameterIn.HEADER)
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
