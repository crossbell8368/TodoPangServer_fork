package com.devcrew1os.dto.main.project;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProjectTodoDTO {
    private int todoId;
    private int status;
}
