package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSocialType {

    GOOGLE(1),
    APPLE(2);

    private final int value;

    public static UserSocialType fromValue(int value) {
        for (UserSocialType userSocialType : UserSocialType.values()) {
            if (userSocialType.value == value) {
                return userSocialType;
            }
        }
        //TODO: add exception
        return null;
    }
}
