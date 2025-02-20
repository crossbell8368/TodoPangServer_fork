package com.devcrew1os.dto.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class SignupRes extends AbstractResponse {

    public SignupRes(boolean status, String message, ErrorCode errorCode) {
        super(status, message, errorCode);
    }
}
