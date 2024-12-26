package com.devcrew1os.controller;

import com.devcrew1os.dto.AuthDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    @Operation(
            summary = "사용자 로그인",
            description = "FirebaseAuth 에서 발급된 IdToken을 활용하여 사용자 검증 및 데이터 업데이트 수행"
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Parameter(name = "Auth", description = "FirebaseAuth IdToken", required = true, in = ParameterIn.HEADER)
            @RequestHeader("Auth") String token,
            @Parameter(name = "uid", description = "FirebaseAuth result.identifier", required = true)
            @RequestBody String uid
    ) {
        return ResponseEntity.ok().build();
    }
}
