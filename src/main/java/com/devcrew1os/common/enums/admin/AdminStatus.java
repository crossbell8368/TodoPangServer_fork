package com.devcrew1os.common.enums.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum AdminStatus {
    UNREGISTERED(1),
    REGISTERED(2);

    private final int value;

    private static final Map<Integer, AdminStatus> VALUE_MAP = Stream.of(
            AdminStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type)
    );

    public static AdminStatus fromValue(int value) {
        return VALUE_MAP.get(value);
    }
}
