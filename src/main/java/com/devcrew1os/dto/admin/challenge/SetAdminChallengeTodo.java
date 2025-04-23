package com.devcrew1os.dto.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetAdminChallengeTodo {
    private int order;
    private String title;
}
