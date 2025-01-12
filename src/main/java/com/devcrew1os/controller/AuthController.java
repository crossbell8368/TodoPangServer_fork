package com.devcrew1os.controller;

import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.service.AuthService;
import com.devcrew1os.service.UserService;
import com.devcrew1os.common.util.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(
            summary = "사용자 로그인",
            description = "FirebaseAuth에서 발급된 IdToken을 활용하여 사용자 검증 및 데이터 업데이트 수행",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "로그인 성공 예제",
                                            value = "{\"status\":200," +
                                                    "\"result\":\"SUCCESS\"," +
                                                    "\"message\":\"[Response] Successfully Logged in!\"," +
                                                    "\"data\":(boolean) true" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "인증 실패 예제",
                                            value = "{\"status\":401," +
                                                    "\"result\":\"FAIL\"," +
                                                    "\"message\":\"[Response] Provided IdToken UID mismatched\"," +
                                                    "\"data\":null" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류 예제",
                                            value = "{\"status\":500," +
                                                    "\"result\":\"FAIL\"," +
                                                    "\"message\":\"Database access failed\"," +
                                                    "\"data\":null" +
                                                    "}"
                                    )))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(name = "Authorization", description = "FirebaseAuth IdToken", required = true, in = ParameterIn.HEADER)
            @RequestHeader("Authorization") String token,
            @Parameter(name = "userId(uid)", description = "FirebaseAuth result.identifier", required = true)
            @RequestBody UserReqDTO.Login dto
    ) {
        String idToken = token.replace("Bearer ", "");
        return authService.login(dto, idToken);
    }

    /*===========================
       회원가입
    ===========================*/
    @Operation(
            summary = "신규 회원가입",
            description = "사용자가 제공한 정보를 바탕으로 새로운 계정을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "회원가입 성공 예제",
                                            value = "{\"status\":200," +
                                                    "\"result\":\"SUCCESS\"," +
                                                    "\"message\":\"[Response] Successfully signed up new user\"," +
                                                    "\"data\":(boolean) true" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "400", description = "[BadRequest] 잘못된 요청 데이터",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "잘못된 요청 예제",
                                            value = "{\"status\":400," +
                                                    "\"result\":\"FAIL\"," +
                                                    "\"message\":\"[Response] Invalid signup request\"," +
                                                    "\"data\":(boolean) false" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "409", description = "[Conflict] 요청 데이터가 서버에 이미 존재함",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "중복된 요청 예제",
                                            value = "{\"status\":409," +
                                                    "\"result\":\"FAIL\"," +
                                                    "\"message\":\"[Response] Already exists UserId\"," +
                                                    "\"data\":(boolean) false" +
                                                    "}"
                                    ))),
                    @ApiResponse(responseCode = "500", description = "[InternalServerError] 서버 오류",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Response.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류 예제",
                                            value = "{\"status\":500," +
                                                    "\"result\":\"FAIL\"," +
                                                    "\"message\":\"[Response] An unexpected error occurred during signup\"," +
                                                    "\"data\":(boolean) false" +
                                                    "}"
                                    )))
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Parameter(name = "User Data", description = "회원가입 요청 데이터", required = true)
            @RequestBody UserReqDTO.Signup dto
    ) {
        return userService.signup(dto);
    }
}
