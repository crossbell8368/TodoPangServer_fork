package com.devcrew1os.dto.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ChallengeListData {
    private Map<Integer, String> categories;
    private List<ChallengeListInfo> infoData;
}
