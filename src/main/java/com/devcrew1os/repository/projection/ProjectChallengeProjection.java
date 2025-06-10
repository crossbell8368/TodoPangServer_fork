package com.devcrew1os.repository.projection;

public interface ProjectChallengeProjection {
    Integer getId();
    Integer getStatus();
    Challenge getChallenge();

    // Challenge 정보를 위한 중첩 인터페이스
    interface Challenge {
        Integer getId();
        String getTitle();
    }
}
