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
    private HomeUserData userData;
    private Map<Integer, String> categories;
    private List<HomeChallengeData> popularChallenges;
}
