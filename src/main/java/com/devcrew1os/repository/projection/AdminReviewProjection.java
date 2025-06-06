package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface AdminReviewProjection {
    int getReviewId();
    int getEmoji();
    int getStatus();
    String getTitle();
    String getUpdatedBy(); // 사용자 이름이 여기에 매핑됩니다.
    LocalDateTime getUpdatedAt();
}
