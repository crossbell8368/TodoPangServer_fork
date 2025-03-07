package com.devcrew1os.dto.home;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeChallengeCard {
    private String title;
    private int category;
    private int diff;
    private int popularity;
}
