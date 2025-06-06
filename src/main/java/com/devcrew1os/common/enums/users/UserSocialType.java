package com.devcrew1os.common.enums.users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserSocialType {

    GOOGLE(1),
    APPLE(2);

    private final int value;

    private static final Map<Integer, UserSocialType> VALUE_MAP = Stream.of(
            UserSocialType.values()).collect(Collectors.toMap(type -> type.value, type -> type)
    );

    public static boolean contains(int value) {
        return VALUE_MAP.containsKey(value);
    }
}
