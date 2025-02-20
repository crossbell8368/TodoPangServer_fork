package com.devcrew1os.common.util;

import com.devcrew1os.dto.auth.LoginRes;
import com.devcrew1os.dto.auth.SignupRes;
import com.devcrew1os.dto.auth.WithdrawRes;
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

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<Body<T>> buildBody(T data, String msg, HttpStatus status, ResponseResult result) {
        T responseData = (data == null) ? (T) (result == ResponseResult.SUCCESS ? Boolean.TRUE : Boolean.FALSE) : data;

        return ResponseEntity.status(status)
                .body(Body.<T>builder()
                        .status(status.value())
                        .result(result)
                        .message(msg)
                        .data(responseData)
                        .build());
    }

    /*===========================
       Response 분기
    ===========================*/

    private ResponseEntity<Body<T>> success(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.SUCCESS);
    }

    private ResponseEntity<Body<T>> fail(T data, String message, HttpStatus status) {
        return buildBody(data, message, status, ResponseResult.FAIL);
    }

    /*===========================
       Result 객체 처리
    ===========================*/

    public ResponseEntity<?> handleResult(SignupRes res) {
        return res.isStatus()
                ? success(null, res.getMessage(), res.getErrorCode().getHttpStatus())
                : fail(null, res.getMessage(), res.getErrorCode().getHttpStatus());
    }

    public ResponseEntity<?> handleResult(LoginRes res) {
        return res.isStatus()
                ? success(null, res.getMessage(), res.getErrorCode().getHttpStatus())
                : fail(null, res.getMessage(), res.getErrorCode().getHttpStatus());
    }

    public ResponseEntity<?> handleResult(WithdrawRes res) {
        return res.isStatus()
                ? success(null, res.getMessage(), res.getErrorCode().getHttpStatus())
                : fail(null, res.getMessage(), res.getErrorCode().getHttpStatus());
    }
}
