package com.devcrew1os.dto;

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

    public enum ResponseResult {
        SUCCESS, FAIL
    }

    /*===========================
       Response 분기
    ===========================*/

    private <T> ResponseEntity<Body<T>> success (BaseResponse<T> res) {
        HttpStatus status = res.getErrorCode().getHttpStatus();
        T data = res.getData() != null ? res.getData() : (T) Boolean.TRUE;

        return ResponseEntity.status(status)
                .body(new Body<>(
                        status.value(),
                        ResponseResult.SUCCESS,
                        res.getMessage(),
                        data
                ));
    }

    private <T> ResponseEntity<Body<T>> fail (BaseResponse<T> res) {
        HttpStatus status = res.getErrorCode().getHttpStatus();
        T data = res.getData() != null ? res.getData() : (T) Boolean.FALSE;

        return ResponseEntity.status(status)
                .body(new Body<>(
                        status.value(),
                        ResponseResult.FAIL,
                        res.getMessage(),
                        data
                ));
    }

    public <T> ResponseEntity<?> handleResult(BaseResponse<T> res) {
        return res.isSuccess() ? success(res) : fail(res);
    }
}
