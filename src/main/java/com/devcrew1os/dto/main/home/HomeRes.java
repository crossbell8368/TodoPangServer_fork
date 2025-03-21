package com.devcrew1os.dto.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeRes extends BaseResponse {

    private HomeData data;

    public HomeRes(boolean isSuccess, boolean isDataExist, String message, ErrorCode errorCode) {
        super(isSuccess, isDataExist, message, errorCode);
    }
}
