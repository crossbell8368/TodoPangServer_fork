package com.devcrew1os.dto.main.home;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.BaseResponse;

import java.util.List;

public class HomeRankingRes extends BaseResponse<List<HomeChallengeData>> {
    public HomeRankingRes(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ){
        super(isSuccess, message, errorCode, null);
    }
}
