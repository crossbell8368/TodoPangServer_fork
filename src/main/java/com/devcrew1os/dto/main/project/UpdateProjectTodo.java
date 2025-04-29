package com.devcrew1os.dto.main.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectTodo {
    private Integer todoId;
    private Integer challengeId;
    private Integer updatedStatus;
}
