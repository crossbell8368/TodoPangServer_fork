package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum DataStatus {

    PREPARE(1),
    DEPLOYED(2),
    DELETE(3);

    private final int value;

    private static final Map<Integer, DataStatus> VALUE_MAP = Stream.of(
            DataStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type)
    );
    public static DataStatus fromValue(int value) {
        return VALUE_MAP.get(value);
    }

    public static boolean isValidValue(int value) {
        return VALUE_MAP.containsKey(value);
    }
}
