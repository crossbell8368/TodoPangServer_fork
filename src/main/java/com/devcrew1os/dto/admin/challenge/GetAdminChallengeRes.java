package com.devcrew1os.dto.admin.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import com.devcrew1os.dto.PageResponse;
import lombok.Getter;

@Getter
public class GetAdminChallengeRes extends BaseResponse<GetAdminChallengeData> {
    public GetAdminChallengeRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode,
            GetAdminChallengeData data // Wrapper DTO를 파라미터로 받음
    ) {
        super(isSuccess, message, errorCode, data); // 부모 클래스에 데이터 전달
    }
    public GetAdminChallengeRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode) {
        super(isSuccess, message, errorCode, null);
    }
}
