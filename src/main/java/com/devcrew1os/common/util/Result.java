package com.devcrew1os.common.util;

import com.devcrew1os.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Result {

    private final boolean success;
    private final String message;
    private final ErrorCode errorCode;

    public static Result success(){
        return new Result(true, null, null);
    }
    public static Result success(String message){
        return new Result(true, message, null);
    }

    public static Result failure(String message, ErrorCode errorCode){
        return new Result(false, message, errorCode);
    }
}
