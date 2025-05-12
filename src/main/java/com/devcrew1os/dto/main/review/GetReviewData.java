package com.devcrew1os.dto.main.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetReviewData {
    private int reviewId;
    private int emoji;
    private String title;
}
