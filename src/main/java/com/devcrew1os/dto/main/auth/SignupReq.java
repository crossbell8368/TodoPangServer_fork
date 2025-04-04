package com.devcrew1os.dto.main.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignupReq {
    private String name;
    private String email;
    private Integer socialType;
}
