package com.devcrew1os.controller;

import com.devcrew1os.dto.AuthDTO;
import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.service.AuthService;
import com.devcrew1os.util.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "사용자 로그인",
            description = "FirebaseAuth에서 발급된 IdToken을 활용하여 사용자 검증 및 데이터 업데이트 수행",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "로그인 성공 예제",
                                            value = "{\"status\":200,\"result\":\"SUCCESS\",\"message\":\"User logged in successfully\",\"data\":null}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "인증 실패 예제",
                                            value = "{\"status\":401,\"result\":\"FAIL\",\"message\":\"Invalid IdToken\",\"data\":null}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류 예제",
                                            value = "{\"status\":500,\"result\":\"FAIL\",\"message\":\"Database access failed\",\"data\":null}"
                                    )))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(name = "Token", description = "FirebaseAuth IdToken", required = true, in = ParameterIn.HEADER)
            @RequestHeader("Authorization") String token,
            @Parameter(name = "uid", description = "FirebaseAuth result.identifier", required = true)
            @RequestBody UserReqDTO.Login dto
    ) {
        String idToken = token.replace("Bearer ", "");
        return authService.login(dto, idToken);
    }
}
