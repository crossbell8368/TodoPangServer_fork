package com.devcrew1os.dto.main.util;

import com.devcrew1os.common.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenVerifyRes {
    private boolean status;
    private StringBuilder message;
    private ErrorCode code;

    public TokenVerifyRes(boolean status, String message, ErrorCode code) {
        this.message = new StringBuilder();
        this.status = status;
        this.code = code;
        this.message.append(message);
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
