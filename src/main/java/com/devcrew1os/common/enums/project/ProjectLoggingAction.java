package com.devcrew1os.common.enums.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ProjectLoggingAction {

    ADD_CHALLENGE(1),
    ADD_TODO(2),
    REMOVE_CHALLENGE(3),
    REMOVE_TODO(4),
    COMPLETE_CHALLENGE(5),
    COMPLETE_TODO(6),
    REGISTER_REVIEW(7),
    REVERT_TODO(8);

    private final int value;

    private static final Map<Integer, ProjectLoggingAction> VALUE_MAP = Stream.of(
            ProjectLoggingAction.values()).collect(Collectors.toMap(type -> type.value, type -> type)
    );

    public static boolean contains(int value) {
        return VALUE_MAP.containsKey(value);
    }

    public static ProjectLoggingAction fromValue(int value) {
        return VALUE_MAP.get(value);
    }
}
