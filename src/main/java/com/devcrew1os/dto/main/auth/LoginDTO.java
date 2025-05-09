package com.devcrew1os.dto.main.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDTO {
    private String uid;
    private String email;
}
