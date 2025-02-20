package com.devcrew1os.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginReq {
    private String userId;
    private String idToken;
}
