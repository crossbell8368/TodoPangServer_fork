package com.devcrew1os.dto.admin.challenge;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminChallengeData {
    private int challengeId;
    private String categoryName;
    private int challengeTerm;
    private int challengeDiff;
    private int challengeTodoCount;
    private int challengeStatus;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedBy;
}
