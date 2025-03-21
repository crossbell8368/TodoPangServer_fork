package com.devcrew1os.dto.main.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRes extends BaseResponse {

    public WithdrawRes(boolean isSuccess, String message, ErrorCode errorCode) {
        super(isSuccess, false, message, errorCode);
    }
}
