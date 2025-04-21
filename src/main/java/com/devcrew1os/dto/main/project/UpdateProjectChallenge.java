package com.devcrew1os.dto.main.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectChallenge {
    private int challengeId;
    private List<UpdateProjectTodo> todoList;
}
