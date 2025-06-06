package com.devcrew1os.dto.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;

public class LogoutRes extends BaseResponse<Void> {
    public LogoutRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
