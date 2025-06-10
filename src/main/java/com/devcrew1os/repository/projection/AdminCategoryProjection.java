package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface AdminCategoryProjection {

    Integer getId();
    String getTitle();
    Integer getStatus();
    LocalDateTime getUpdatedAt();
    String getUpdatedBy();
    Integer getChallengeCount();
}
