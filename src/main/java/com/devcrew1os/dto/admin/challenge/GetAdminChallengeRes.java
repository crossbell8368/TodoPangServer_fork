package com.devcrew1os.dto.admin.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import com.devcrew1os.dto.PageResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class GetAdminChallengeRes extends BaseResponse<PageResponse<GetAdminChallengeData>> {

    public GetAdminChallengeRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode) {
        super(isSuccess, message, errorCode, null);
    }
}
