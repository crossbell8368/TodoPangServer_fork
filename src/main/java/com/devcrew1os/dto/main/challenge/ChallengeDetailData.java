package com.devcrew1os.dto.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDetailData {
    private int challengeId;
    private String title;
    private int popularity;
    private int category;
    private int term;
    private int diff;
    private List<ChallengeTodoData> todoList;
    private List<ChallengeReviewData> reviewList;
}

