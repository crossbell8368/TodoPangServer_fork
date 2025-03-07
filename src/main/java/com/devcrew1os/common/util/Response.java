package com.devcrew1os.common.util;

import com.devcrew1os.dto.AbstractResponse;
import com.devcrew1os.dto.auth.LoginRes;
import com.devcrew1os.dto.auth.SignupRes;
import com.devcrew1os.dto.auth.WithdrawRes;
import com.devcrew1os.dto.home.HomeRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class Response<T> {

    @Getter
    @Builder
    @AllArgsConstructor
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
       Response 분기
    ===========================*/

    private <T> ResponseEntity<Body<T>> success(HttpStatus isSuccess, String message, Class<T> dataType, T data) {
        return ResponseEntity.status(isSuccess)
                .body(new Body<>(isSuccess.value(), ResponseResult.SUCCESS, message, data));
    }

    private <T> ResponseEntity<Body<T>> success(HttpStatus isSuccess, String message) {
        return ResponseEntity.status(isSuccess)
                .body(new Body<>(isSuccess.value(), ResponseResult.SUCCESS, message, (T) Boolean.TRUE));
    }

    private <T> ResponseEntity<Body<T>> failed(HttpStatus isSuccess, String message, Class<T> dataType, T data) {
        return ResponseEntity.status(isSuccess)
                .body(new Body<>(isSuccess.value(), ResponseResult.FAIL, message, data));
    }

    private <T> ResponseEntity<Body<T>> failed(HttpStatus isSuccess, String message) {
        return ResponseEntity.status(isSuccess)
                .body(new Body<>(isSuccess.value(), ResponseResult.SUCCESS, message, (T) Boolean.FALSE));
    }

    /*===========================
       Result 객체 처리
    ===========================*/

    public ResponseEntity<?> handleResult(SignupRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    public ResponseEntity<?> handleResult(LoginRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    public ResponseEntity<?> handleResult(WithdrawRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }
}
