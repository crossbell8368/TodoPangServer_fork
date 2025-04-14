package com.devcrew1os.dto.main.home;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeChallengeData {
    private int challengeId;
    private String title;
    private int category;
    private int diff;
    private int popularity;
}
