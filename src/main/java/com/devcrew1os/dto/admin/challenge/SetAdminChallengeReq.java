package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetAdminChallengeReq {
    private String title;
    private Integer categoryId;
    private Integer diff;
    private Integer term;
    private List<SetAdminChallengeTodo> todoList;
}
