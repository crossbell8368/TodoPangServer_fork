package com.devcrew1os.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_log_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "project_id", nullable = false)
    private int projectId;

    @Column(name = "challenge_id")
    private Integer challengeId;

    @Column(name = "todo_id")
    private Integer todoId;

    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "user_action_type")
    private int userActionType;

    @Column(name = "user_action_at")
    private LocalDateTime userActionAt;
}
