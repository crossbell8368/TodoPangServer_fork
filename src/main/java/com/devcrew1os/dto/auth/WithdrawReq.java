package com.devcrew1os.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WithdrawReq {
    private String userId;
    private Integer reason;
}
