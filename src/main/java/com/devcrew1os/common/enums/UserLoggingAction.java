package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserLoggingAction {

    ADD_CHALLENGE(1),
    REMOVE_CHALLENGE(2),
    COMPLETE_CHALLENGE(3),
    ADD_TODO(4),
    REMOVE_TODO(5),
    COMPLETE_TODO(6),
    REVERT_TODO(7);

    private final int value;

    private static final Map<Integer, UserLoggingAction> VALUE_MAP =
            Stream.of(UserLoggingAction.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static UserLoggingAction fromValue(int value) {
        UserLoggingAction userLoggingAction = VALUE_MAP.get(value);
        if (userLoggingAction == null) {
            throw new IllegalArgumentException("Invalid user challenge action value: " + value);
        }
        return userLoggingAction;
    }
}
