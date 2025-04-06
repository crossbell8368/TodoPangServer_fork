package com.devcrew1os.dto.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class SignupRes extends BaseResponse<Void> {

    public SignupRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
