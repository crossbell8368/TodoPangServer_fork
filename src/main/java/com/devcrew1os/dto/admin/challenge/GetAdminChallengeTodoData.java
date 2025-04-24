package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminChallengeTodoData {
    private Integer todoId;
    private Integer todoOrder;
    private String todoTitle;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedAt;
}
