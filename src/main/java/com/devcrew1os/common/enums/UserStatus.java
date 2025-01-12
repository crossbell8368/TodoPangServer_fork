package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE(1),
    INACTIVE(2),
    DELETED(3);

    private final int value;

    public static UserStatus fromValue(int value) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.value == value) {
                return userStatus;
            }
        }
        //TODO: add exception
        return null;
    }
}
