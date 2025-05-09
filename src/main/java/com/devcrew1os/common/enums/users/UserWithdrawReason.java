package com.devcrew1os.common.enums.users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum UserWithdrawReason {
    REASON1(1),
    REASON2(2),
    REASON3(3),
    REASON4(4),
    REASON5(5),
    REASON6(6),
    REASON7(7),
    REASON8(8),
    REASON9(9),
    REASON10(10);

    private final int value;

    private static final Map<Integer, UserWithdrawReason> VALUE_MAP = Stream.of(
            values()).collect(Collectors.toMap(UserWithdrawReason::getValue, Function.identity())
    );

    public static boolean contains(int value) {
        return VALUE_MAP.containsKey(value);
    }

    public static UserWithdrawReason fromValue(int value) {
        return VALUE_MAP.get(value);
    }
}
