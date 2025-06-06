package com.devcrew1os.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminReviewData {
    private int reviewId;
    private int emoji;
    private int status;
    private String title;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedAt;
}
