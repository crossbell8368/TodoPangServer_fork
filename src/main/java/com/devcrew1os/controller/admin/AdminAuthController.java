package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.auth.AdminLoginRes;
import com.devcrew1os.dto.admin.auth.AdminSignupReq;
import com.devcrew1os.dto.admin.auth.AdminSignupRes;
import com.devcrew1os.dto.admin.auth.AdminWithdrawRes;
import com.devcrew1os.service.admin.auth.AdminAuthService;
import com.devcrew1os.service.util.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final Response<?> response;
    private final AdminAuthService authService;
    private final TokenService tokenService;

    /*===========================
       회원가입
    ===========================*/
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody AdminSignupReq req
    ) {
        AdminSignupRes res = authService.signup(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), req
        );
        return response.handleResult(res);
    }

    /*===========================
       로그인
    ===========================*/
    @PostMapping("/login")
    public ResponseEntity<?> login(){
        AdminLoginRes res = authService.login(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       로그아웃
    ===========================*/
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String header
    ){
        if(header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    new Response.Body<>(
                            HttpStatus.BAD_REQUEST.value(),
                            Response.ResponseResult.FAIL,
                            "[Failed] No token",
                            Boolean.FALSE
                    )
            );
        }
        String token = header.substring(7);
        tokenService.tokenCleaner(token);
        return ResponseEntity.ok().body(
                new Response.Body<>(
                        HttpStatus.OK.value(),
                        Response.ResponseResult.SUCCESS,
                        "[Success] Logout finish",
                        Boolean.TRUE
                )
        );
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(){
        AdminWithdrawRes res = authService.withdraw(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }
}
