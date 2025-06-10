package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminChallengeReq {
    private Integer challengeId;
    private Integer newCategoryId;
    private String newChallengeTitle;
    private Integer newChallengeTerm;
    private Integer newChallengeDiff;

    private List<UpdateAdminChallengeTodo> todosToAdd = new ArrayList<>();
    private List<UpdateAdminChallengeTodo> todosToUpdate = new ArrayList<>();
    private List<UpdateAdminChallengeTodo> todosToDelete = new ArrayList<>();
}
