package com.devcrew1os.dto;

import com.devcrew1os.common.enums.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public abstract class BaseResponse {
    protected boolean isSuccess;
    protected boolean isDataExist;
    protected StringBuilder message;
    protected ErrorCode errorCode;

    public BaseResponse(boolean isSuccess, boolean isDataExist, String message, ErrorCode errorCode) {
        this.message = new StringBuilder();
        this.isSuccess = isSuccess;
        this.isDataExist = isDataExist;
        this.message.append(message);
        this.errorCode = errorCode;
    }
    public void addMessage(String message) {
        if (this.message.length() > 0) {
            this.message.append(" ");
        }
        this.message.append(message);
    }
    public String getMessage() {
        return message.toString();
    }
}
