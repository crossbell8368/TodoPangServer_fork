package com.devcrew1os.repository.projection;

public interface ProjectTodoProjection {
    int getId();
    int getStatus();
    int getChallengeId();
    Todo getTodo();

    interface Todo {
        int getId();
        String getDesc();
    }
}
