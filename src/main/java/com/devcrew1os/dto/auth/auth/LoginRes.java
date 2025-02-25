package com.devcrew1os.dto.auth.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRes extends AbstractResponse {

    public LoginRes(boolean status, String message, ErrorCode errorCode) {
        super(status, message, errorCode);
    }
}
