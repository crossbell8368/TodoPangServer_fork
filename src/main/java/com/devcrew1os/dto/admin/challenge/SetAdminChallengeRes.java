package com.devcrew1os.dto.admin.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class SetAdminChallengeRes extends BaseResponse<Void> {
    public SetAdminChallengeRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode) {
        super(isSuccess, message, errorCode, null);
    }
}
