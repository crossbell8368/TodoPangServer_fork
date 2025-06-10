package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface AdminChallengeProjection {
    Integer getId();
    Integer getCategoryId();
    String getTitle();
    Integer getTerm();
    Integer getDiff();
    Integer getStatus();
    Integer getIncludeTodoCount();
    String getUpdatedBy();
    LocalDateTime getUpdatedAt();
}
