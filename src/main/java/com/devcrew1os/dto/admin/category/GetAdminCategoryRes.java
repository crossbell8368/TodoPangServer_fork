package com.devcrew1os.dto.admin.category;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class GetAdminCategoryRes extends BaseResponse<List<GetAdminCategoryData>> {

    public GetAdminCategoryRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        super(isSuccess, message, errorCode, null);
    }
}
