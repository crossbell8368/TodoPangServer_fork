package com.devcrew1os.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetAdminReviewReq {
    private String title;
    private Integer emoji;
}
