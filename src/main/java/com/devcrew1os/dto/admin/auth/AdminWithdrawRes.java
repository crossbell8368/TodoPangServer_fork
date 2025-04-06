package com.devcrew1os.dto.admin.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminWithdrawRes extends BaseResponse {

    public AdminWithdrawRes(boolean isSuccess, String message, ErrorCode errorCode) {
        super(isSuccess, false, message, errorCode);
    }
}
