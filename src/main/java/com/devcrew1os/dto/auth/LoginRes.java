package com.devcrew1os.dto.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRes extends AbstractResponse {

    public LoginRes(boolean isSuccess, String message, ErrorCode errorCode) {
        super(isSuccess, false, message, errorCode);
    }
}
