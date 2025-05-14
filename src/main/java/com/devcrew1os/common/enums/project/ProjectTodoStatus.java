package com.devcrew1os.common.enums.project;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ProjectTodoStatus {

    CHECK(1),
    UNCHECK(2),
    COMPLETE(3),
    REMOVED(4);

    private final int value;

    private static final Map<Integer, ProjectTodoStatus> VALUE_MAP =
            Stream.of(ProjectTodoStatus.values()).collect(Collectors.toMap(type -> type.value, type -> type));

    public static boolean contains(int value) {
        return VALUE_MAP.containsKey(value);
    }

    public static ProjectTodoStatus fromValue(int value) {
        ProjectTodoStatus projectTodoStatus = VALUE_MAP.get(value);
        if (projectTodoStatus == null) {
            throw new IllegalArgumentException("Invalid projectTodoStatus value: " + value);
        }
        return projectTodoStatus;
    }
}
