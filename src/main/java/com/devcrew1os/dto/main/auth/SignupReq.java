package com.devcrew1os.dto.main.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupReq {
    private String name;
    private String email;
    private Integer socialType;
}
