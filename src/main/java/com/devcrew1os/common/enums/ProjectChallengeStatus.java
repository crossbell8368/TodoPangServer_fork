package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ProjectChallengeStatus {

    ONGOING(1),
    FINISHED(2);

    private final int value;

    private static final Map<Integer, ProjectChallengeStatus> VALUE_MAP =
            Stream.of(values()).collect(Collectors.toMap(ProjectChallengeStatus::getValue, Function.identity()));

    public static boolean contains(int value) {
        return VALUE_MAP.containsKey(value);
    }

    public static ProjectChallengeStatus fromValue(int value) {
        ProjectChallengeStatus status = VALUE_MAP.get(value);
        if(status == null){
            throw new IllegalArgumentException("Unknown projectChallengeStatus: " + value + " detected");
        }
        return status;
    }
}
