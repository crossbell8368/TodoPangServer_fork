package com.devcrew1os.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Location {

    MAIN_AUTH("mainAuth"),
    MAIN_HOME("mainHome"),
    MAIN_CHALLENGE("mainChallenge"),
    MAIN_CHALLENGE_DETAIL("mainChallengeDetail"),

    ADMIN_AUTH("adminAuth"),
    ADMIN_CATEGORY("adminCategory"),
    ADMIN_CHALLENGE("adminChallenge");

    private final String val;
}
