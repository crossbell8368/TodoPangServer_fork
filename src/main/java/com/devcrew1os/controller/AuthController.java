package com.devcrew1os.controller;

import com.devcrew1os.common.util.Response;
import com.devcrew1os.common.util.Result;
import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.service.AuthService;
import com.devcrew1os.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private Response<?> response;
    private final AuthService authService;
    private final UserService userService;

    /*===========================
       로그인
    ===========================*/
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(name = "Authorization", description = "FirebaseAuth IdToken", required = true, in = ParameterIn.HEADER)
            @RequestHeader("Authorization") String token,
            @RequestBody UserReqDTO.Login dto
    ) {
        String idToken = token.replace("Bearer ", "");
        Result result = authService.login(dto, idToken);
        return response.handleResult(result);
    }

    /*===========================
       회원가입
    ===========================*/
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody UserReqDTO.Signup dto
    ) {
        Result result = userService.signup(dto);
        return response.handleResult(result);
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody UserReqDTO.Withdraw dto
    ) {
        Result result = userService.withdraw(dto);
        return response.handleResult(result);
    }
}
