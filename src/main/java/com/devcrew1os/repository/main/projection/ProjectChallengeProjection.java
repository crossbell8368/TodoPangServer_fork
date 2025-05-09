package com.devcrew1os.repository.main.projection;

public interface ProjectChallengeProjection {
    int getId();
    int getStatus();
    Challenge getChallenge();

    // Challenge 정보를 위한 중첩 인터페이스
    interface Challenge {
        int getId();
        String getTitle();
    }
}
