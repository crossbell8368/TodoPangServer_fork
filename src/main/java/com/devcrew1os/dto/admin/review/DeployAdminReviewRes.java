package com.devcrew1os.dto.admin.review;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class DeployAdminReviewRes extends BaseResponse<Void> {
    public DeployAdminReviewRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode) {
        super(isSuccess, message, errorCode, null);
    }
}
