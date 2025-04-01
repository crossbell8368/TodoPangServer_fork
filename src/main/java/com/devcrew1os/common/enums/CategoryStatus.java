package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum CategoryStatus {

    PREPARE(1),
    LAUNCH(2),
    DELETE(3);

    private final int value;

    private static final Map<Integer, CategoryStatus> VALUE_MAP =
            Stream.of(CategoryStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static CategoryStatus fromValue(int value) {
        CategoryStatus categoryStatus = VALUE_MAP.get(value);
        if (categoryStatus == null) {
            throw new IllegalArgumentException("Invalid category status: " + value);
        }
        return categoryStatus;
    }
    public static boolean isValidValue(int value) {
        return VALUE_MAP.containsKey(value);
    }
}
