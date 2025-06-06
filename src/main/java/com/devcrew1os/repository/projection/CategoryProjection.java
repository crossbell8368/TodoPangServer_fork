package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface CategoryProjection {

    int getId();
    String getTitle();
    int getStatus();
    LocalDateTime getUpdatedAt();
    String getUpdatedBy();
    long getChallengeCount();
}
