package com.devcrew1os.dto.auth.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRes extends AbstractResponse {

    public WithdrawRes(boolean success, String message, ErrorCode errorCode) {
        super(success, message, errorCode);
    }
}
