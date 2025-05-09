package com.devcrew1os.repository.main.projection;

import java.time.LocalDateTime;

public interface CategoryProjection {

    int getId();
    String getTitle();
    int getStatus();
    LocalDateTime getUpdatedAt();
    String getUpdatedBy();
    long getChallengeCount();
}
