package com.devcrew1os.dto.admin.category;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

@Getter
public class GetAdminCategoryRes extends BaseResponse<GetAdminCategoryData[]> {

    public GetAdminCategoryRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
