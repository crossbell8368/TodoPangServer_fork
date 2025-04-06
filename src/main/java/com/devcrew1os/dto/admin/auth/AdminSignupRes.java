package com.devcrew1os.dto.admin.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class AdminSignupRes extends BaseResponse<Void> {

    public AdminSignupRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ){
        super(isSuccess, message, errorCode, null);
    }
}
