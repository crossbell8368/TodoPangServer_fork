package com.devcrew1os.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Schema(name = "Response", description = "공통 API 응답 객체")
public class Response<T> {

    @Getter
    @Builder
    @Schema(name = "ResponseBody", description = "공통 API 응답 Body")
    public static class Body<T> {

        @Schema(description = "HTTP 상태 코드", example = "200")
        private final int status;

        @Schema(description = "응답 결과 (SUCCESS 또는 FAIL)", example = "SUCCESS")
        private final ResponseResult result;

        @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
        private final String message;

        @Schema(description = "실제 데이터 객체")
        private final T data;
    }

    /*===========================
       응답 상태 Enum
    ===========================*/

    @Schema(enumAsRef = true, description = "응답 결과 상태", example = "SUCCESS")
    public enum ResponseResult {
        SUCCESS, FAIL
    }

    /*===========================
       응답 Body 생성
    ===========================*/
    private <T> ResponseEntity<Body<T>> buildBody(T data, String msg, HttpStatus status, ResponseResult result) {
        Body<T> body = Body.<T>builder()
                .status(status.value())
                .result(result)
                .message(msg)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    /*===========================
       Success
    ===========================*/

    public ResponseEntity<Body<T>> success(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.SUCCESS);
    }

    public ResponseEntity<Body<T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    public ResponseEntity<Body<T>> success(T data) {
        return success(data, null, HttpStatus.OK);
    }

    public ResponseEntity<Body<T>> success(String message) {
        return success(null, message, HttpStatus.OK);
    }

    public ResponseEntity<Body<T>> success() {
        return success(null, null, HttpStatus.OK);
    }

    /*===========================
       Fail
    ===========================*/

    public ResponseEntity<Body<T>> fail(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.FAIL);
    }

    public ResponseEntity<Body<T>> fail(String message, HttpStatus status) {
        return fail(null, message, status);
    }

    public ResponseEntity<Body<T>> fail(String message) {
        return fail(null, message, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Body<T>> fail() {
        return fail(null, "An error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
