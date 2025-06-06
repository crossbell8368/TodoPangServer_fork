package com.devcrew1os.dto.admin.review;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import com.devcrew1os.dto.PageResponse;
import lombok.Getter;

@Getter
public class GetAdminReviewRes extends BaseResponse<PageResponse<GetAdminReviewData>> {
    public GetAdminReviewRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode) {
        super(isSuccess, message, errorCode, null);
    }
}
