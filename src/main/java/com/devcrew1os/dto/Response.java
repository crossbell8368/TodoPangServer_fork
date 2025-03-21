package com.devcrew1os.dto;

import com.devcrew1os.dto.admin.category.GetAdminCategoryRes;
import com.devcrew1os.dto.main.auth.LoginRes;
import com.devcrew1os.dto.main.auth.SignupRes;
import com.devcrew1os.dto.main.auth.WithdrawRes;
import com.devcrew1os.dto.main.challenge.GetChallengesRes;
import com.devcrew1os.dto.main.home.HomeRes;
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

    private <T> ResponseEntity<Body<T>> success(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(new Body<>(status.value(), ResponseResult.SUCCESS, message, data));
    }

    private ResponseEntity<Body<T>> success(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new Body<>(status.value(), ResponseResult.SUCCESS, message, (T) Boolean.TRUE));
    }

    private ResponseEntity<Body<T>> failed(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new Body<>(status.value(), ResponseResult.FAIL, message, (T) Boolean.FALSE));
    }

    /*===========================
       Result 객체 처리: Main
    ===========================*/

    // signup
    public ResponseEntity<?> handleResult(SignupRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    // login
    public ResponseEntity<?> handleResult(LoginRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    // withdraw
    public ResponseEntity<?> handleResult(WithdrawRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    // home
    public ResponseEntity<?> handleResult(HomeRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage(), res.getData())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    // challenge
    public ResponseEntity<?> handleResult(GetChallengesRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage(), res.getData())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }

    /*===========================
       Result 객체 처리: Admin
    ===========================*/

    // category
    public ResponseEntity<?> handleResult(GetAdminCategoryRes res) {
        return res.isSuccess()
                ? success(res.getErrorCode().getHttpStatus(), res.getMessage(), res.getData())
                : failed(res.getErrorCode().getHttpStatus(), res.getMessage());
    }
}
