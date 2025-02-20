package com.devcrew1os.dto;

import com.devcrew1os.common.enums.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public abstract class AbstractResponse {
    protected boolean status;
    protected StringBuilder message;
    protected ErrorCode errorCode;

    public AbstractResponse(boolean status, String message, ErrorCode errorCode) {
        this.message = new StringBuilder();
        this.status = status;
        this.message.append(message);
        this.errorCode = errorCode;
    }

    public void addMessage(String message) {
        if (this.message.length() > 0) {
            this.message.append(" ");
        }
        this.message.append(message);
    }

    public void setError(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message.toString();
    }
}
