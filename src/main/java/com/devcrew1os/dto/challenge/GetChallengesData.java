package com.devcrew1os.dto.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GetChallengesData {
    private Map<Integer, String> categories;
    private List<GetChallengesInfo> infoData;
}
