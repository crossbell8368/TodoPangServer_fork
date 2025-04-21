package com.devcrew1os.dto.main.project;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class UpdateProjectRes extends BaseResponse<Void> {
    public UpdateProjectRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode);
    }
}
