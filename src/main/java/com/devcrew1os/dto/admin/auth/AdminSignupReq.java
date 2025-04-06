package com.devcrew1os.dto.admin.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminSignupReq {
    private String name;
    private String email;
    private Integer socialType;
}
