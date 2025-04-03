package com.devcrew1os.dto.main.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenVerifyReq {
    private String token;
    private String userId;
    private String service;
}
