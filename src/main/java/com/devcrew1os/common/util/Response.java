package com.devcrew1os.common.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
@Component
public class Response<T> {

    @Getter
    @Builder
    public static class Body<T> {

        private final int status;
        private final ResponseResult result;
        private final String message;
        private final T data;
    }

    /*===========================
       응답 상태 Enum
    ===========================*/

    private enum ResponseResult {
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

    private ResponseEntity<Body<T>> success(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.SUCCESS);
    }

    private ResponseEntity<Body<T>> success(T data, String message) {
        return success(data, message, HttpStatus.OK);
    }

    private ResponseEntity<Body<T>> success(T data) {
        return success(data, "[Response] Successfully processing request!", HttpStatus.OK);
    }

    private ResponseEntity<Body<T>> success(String message) {
        T data = (T) Boolean.TRUE;
        return success(data, message, HttpStatus.OK);
    }

    private ResponseEntity<Body<T>> success() {
        T data = (T) Boolean.TRUE;
        return success(data, "[Response] Successfully processing request!", HttpStatus.OK);
    }

    /*===========================
       Fail
    ===========================*/

    private ResponseEntity<Body<T>> fail(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.FAIL);
    }

    private ResponseEntity<Body<T>> fail(String message, HttpStatus status) {
        T data = (T) Boolean.FALSE;
        return fail(data, message, status);
    }

    private ResponseEntity<Body<T>> fail(String message) {
        T data = (T) Boolean.FALSE;
        return fail(data, message, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Body<T>> fail() {
        T data = (T) Boolean.FALSE;
        return fail(data, "[Response] Failed to processing request!", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*===========================
       Result 객체 처리
    ===========================*/

    public ResponseEntity<?> handleResult(Result result) {
        if (result.isSuccess()) {
            return result.getMessage() == null
                    ? success()
                    : success(result.getMessage());
        }
        return fail(result.getMessage(), result.getErrorCode().getHttpStatus());
    }
}
