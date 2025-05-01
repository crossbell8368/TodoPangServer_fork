package com.devcrew1os.dto.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class ChallengeRegisterDTO {

    private final boolean isChallengeRegisterNeed;
    private final Set<Integer> unregisteredTodoIds;
}
