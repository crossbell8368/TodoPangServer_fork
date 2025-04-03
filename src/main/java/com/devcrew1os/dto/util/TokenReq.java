package com.devcrew1os.dto.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenReq {
    private String token;
    private String userId;
    private String service;
}
