package com.devcrew1os.dto;

import com.devcrew1os.common.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public abstract class BaseResponse<T> {
    protected boolean isSuccess;
    protected ErrorCode errorCode;
    protected  T data;

    @JsonIgnore
    protected StringBuilder message = new StringBuilder();

    public BaseResponse(
            boolean isSuccess,
            String message,
            ErrorCode errorCode
    ) {
        this.isSuccess = isSuccess;
        this.message.append(message);
        this.errorCode = errorCode;
    }

    public BaseResponse(
            boolean isSuccess,
            String message,
            ErrorCode errorCode,
            T data
    ) {
        this(isSuccess, message, errorCode);
        this.data = data;
    }

    public void addMessage(String message) {
        if (this.message.length() > 0) {
            this.message.append(" ");
        }
        this.message.append(message);
    }

    @JsonProperty("message")
    public String getMessage() {
        return message.toString();
    }
}
