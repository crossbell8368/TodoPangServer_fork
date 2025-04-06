package com.devcrew1os.dto.admin.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class AdminSignupRes extends BaseResponse {

    public AdminSignupRes(boolean isSuccess, String message, ErrorCode errorCode){
        super(isSuccess, false, message, errorCode);
    }
}
