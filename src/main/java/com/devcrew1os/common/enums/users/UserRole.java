package com.devcrew1os.common.enums.users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER(1),
    ADMIN(2);

    private final int value;

    private static final Map<Integer, UserRole> VALUE_MAP = Stream.of(
            UserRole.values()).collect(Collectors.toMap(UserRole::getValue, Function.identity())
    );

    public static boolean isValid(int val){
        return VALUE_MAP.containsKey(val);
    }
}
