package com.devcrew1os.dto.admin.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class AdminLoginRes extends BaseResponse<Void> {

    public AdminLoginRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
