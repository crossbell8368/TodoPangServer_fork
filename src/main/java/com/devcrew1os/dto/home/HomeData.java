package com.devcrew1os.dto.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class HomeData {
    private String userName;
    private int finishedProjects;
    private int registeredProjects;
    private Map<Integer, String> categories;
    private List<HomeChallengeCard> popularChallenges;
}
