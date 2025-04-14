package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserChallengeAction {

    ADD_CHALLENGE(1),
    REMOVE_CHALLENGE(2),
    COMPLETE_CHALLENGE(3),
    ADD_TODO(4),
    REMOVE_TODO(5),
    COMPLETE_TODO(6),
    REVERT_TODO(7);

    private final int value;

    private static final Map<Integer, UserChallengeAction> VALUE_MAP =
            Stream.of(UserChallengeAction.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static UserChallengeAction fromValue(int value) {
        UserChallengeAction userChallengeAction = VALUE_MAP.get(value);
        if (userChallengeAction == null) {
            throw new IllegalArgumentException("Invalid user challenge action value: " + value);
        }
        return userChallengeAction;
    }
}
