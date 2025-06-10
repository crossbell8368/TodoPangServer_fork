package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface AdminReviewProjection {
    Integer getReviewId();
    Integer getEmoji();
    Integer getStatus();
    String getTitle();
    String getUpdatedBy(); // 사용자 이름이 여기에 매핑됩니다.
    LocalDateTime getUpdatedAt();
}
