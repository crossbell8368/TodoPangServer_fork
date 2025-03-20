package com.devcrew1os.dto.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.AbstractResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetChallengesRes extends AbstractResponse {

    public GetChallengesRes(boolean isSuccess, boolean isDataExist, String message, ErrorCode errorCode) {
        super(isSuccess, isDataExist, message, errorCode);
    }
    private GetChallengesData data;
}
