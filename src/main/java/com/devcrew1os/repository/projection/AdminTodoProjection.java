package com.devcrew1os.repository.projection;

import java.time.LocalDateTime;

public interface AdminTodoProjection {
    Integer getId();
    Integer getChallengeId();
    Integer getTodoOrder();
    String getDesc();
    String getUpdatedBy();
    LocalDateTime getUpdatedAt();
}
