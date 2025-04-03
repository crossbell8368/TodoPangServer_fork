package com.devcrew1os.dto.util;

import com.devcrew1os.entity.main.user.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ValidationResult {
    private boolean isValid;
    private UserInfo userInfo;
}
