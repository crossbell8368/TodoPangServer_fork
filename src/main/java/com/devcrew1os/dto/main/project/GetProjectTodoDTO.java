package com.devcrew1os.dto.main.project;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GetProjectTodoDTO {
    private int projectTodoId;
    private int projectTodoStatus;
    private int challengeId;
    private int todoId;
    private String todoDesc;
}
