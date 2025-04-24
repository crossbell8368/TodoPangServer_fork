package com.devcrew1os.dto.admin.challenge;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminChallengeData {
    private Integer challengeId;
    private Integer categoryId;
    private String challengeName;
    private Integer challengeTerm;
    private Integer challengeDiff;
    private Integer challengeTodoCount;
    private Integer challengeStatus;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedBy;
    private List<GetAdminChallengeTodoData> todoList;
}
