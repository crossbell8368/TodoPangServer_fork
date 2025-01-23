package com.devcrew1os.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthDTO {
    private String userId;
    private String userName;
    private String userEmail;
}
