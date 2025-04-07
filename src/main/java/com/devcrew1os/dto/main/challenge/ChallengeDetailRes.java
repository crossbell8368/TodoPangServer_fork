package com.devcrew1os.dto.main.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class ChallengeDetailRes extends BaseResponse<ChallengeDetailData> {

    public ChallengeDetailRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
