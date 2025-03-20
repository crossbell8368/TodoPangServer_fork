package com.devcrew1os.dto.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeRes extends AbstractResponse  {

    private HomeData data;

    public HomeRes(boolean isSuccess, boolean isDataExist, String message, ErrorCode errorCode) {
        super(isSuccess, isDataExist, message, errorCode);
    }
}
