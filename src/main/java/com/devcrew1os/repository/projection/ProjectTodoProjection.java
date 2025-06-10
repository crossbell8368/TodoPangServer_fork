package com.devcrew1os.repository.projection;

public interface ProjectTodoProjection {
    Integer getId();
    Integer getStatus();
    Integer getChallengeId();
    Todo getTodo();

    interface Todo {
        Integer getId();
        String getDesc();
    }
}
