package com.devcrew1os.dto.main.project;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectData {
    private String userName;
    private int registeredChallenges;
    private List<GetProjectChallenge> challenges;
}
