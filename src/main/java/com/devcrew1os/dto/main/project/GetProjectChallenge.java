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
public class GetProjectChallenge {
    private int challengeId;
    private int originChallengeId;
    private int challengeStatus;
    private String challengeName;
    private List<GetProjectTodo> todoList;
}
