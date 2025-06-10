package com.devcrew1os.dto.admin.challenge;

import com.devcrew1os.dto.PageResponse;
import lombok.Getter;

import java.util.Map;

@Getter
public class GetAdminChallengeData {

    private final PageResponse<GetAdminChallenge> challengeData;
    private final Map<Integer, String> categories;

    public GetAdminChallengeData(PageResponse<GetAdminChallenge> challengePage, Map<Integer, String> categoryMap) {
        this.challengeData = challengePage;
        this.categories = categoryMap;
    }
}
