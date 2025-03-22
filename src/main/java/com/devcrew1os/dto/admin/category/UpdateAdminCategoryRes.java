package com.devcrew1os.dto.admin.category;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateAdminCategoryRes extends BaseResponse {

    public UpdateAdminCategoryRes(boolean isSuccess, boolean isDataExist, String message, ErrorCode errorCode) {
        super(isSuccess, isDataExist, message, errorCode);
    }
}
