package com.devcrew1os.dto.main.review;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class UpdateReviewRes extends BaseResponse<Void> {
    public UpdateReviewRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode);
    }
}
