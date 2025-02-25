package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE(1),
    INACTIVE(2),
    DELETED(3);

    private final int value;

    private static final Map<Integer, UserStatus> VALUE_MAP =
            Stream.of(UserStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static UserStatus fromValue(int value) {
        UserStatus userStatus = VALUE_MAP.get(value);
        if (userStatus == null) {
            throw new IllegalArgumentException("Invalid user status: " + value);
        }
        return userStatus;
    }
}
