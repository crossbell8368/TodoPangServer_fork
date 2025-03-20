package com.devcrew1os.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetChallengesInfo {
    private int id;
    private int category;
    private String title;
    private int diff;
    private int popularity;
}
