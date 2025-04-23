package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum AdminStatus {

    PREPARE(1),
    LAUNCH(2),
    DELETE(3);

    private final int value;

    private static final Map<Integer, AdminStatus> VALUE_MAP =
            Stream.of(AdminStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static AdminStatus fromValue(int value) {
        AdminStatus adminStatus = VALUE_MAP.get(value);
        if (adminStatus == null) {
            throw new IllegalArgumentException("Invalid category status: " + value);
        }
        return adminStatus;
    }
    public static boolean isValidValue(int value) {
        return VALUE_MAP.containsKey(value);
    }
}
