package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminChallengeTodo {
    private Integer todoId;
    private Integer newTodoOrder;
    private Integer newTodoStatus;
    private String newTodoTitle;
}
