package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminChallengeData {
    private Integer challengeId;
    private Integer categoryId;
    private String newChallengeTitle;
    private Integer newChallengeTerm;
    private Integer newChallengeDiff;
    private Integer newChallengeStatus;
    private List<UpdateAdminChallengeTodoData> newTodoList = new ArrayList<>();
}
