package com.devcrew1os.dto.main.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewReq {
    private int originChallengeId;
    private int selectedReviewId;
    private float satisfiedRating;
}
