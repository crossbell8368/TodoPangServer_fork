package com.devcrew1os.dto.main.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectTodo {
    private int challengeId;
    private int todoId;
    private String title;
    private int status;
}
