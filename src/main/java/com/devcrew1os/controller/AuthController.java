package com.devcrew1os.controller;

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
        return authService.login(dto, idToken);
    }

    /*===========================
       회원가입
    ===========================*/
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody UserReqDTO.Signup dto
    ) {
        return userService.signup(dto);
    }
}
