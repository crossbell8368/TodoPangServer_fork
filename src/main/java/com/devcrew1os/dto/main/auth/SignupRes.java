package com.devcrew1os.dto.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class SignupRes extends BaseResponse {

    public SignupRes(boolean isSuccess, String message, ErrorCode errorCode) {
        super(isSuccess, false, message, errorCode);
    }
}
