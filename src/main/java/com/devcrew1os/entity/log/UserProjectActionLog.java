package com.devcrew1os.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_project_action_log", schema = "log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProjectActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_project_action_log_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "project_id", nullable = false)
    private int projectId;

    @Column(name = "challenge_info_id")
    private int challengeInfoId;

    @Column(name = "challenge_todo_id")
    private int challengeTodoId;

    @Column(name = "user_action_type")
    private int userActionType;

    @Column(name = "user_action_desc")
    private String userActionDesc;

    @Column(name = "user_action_at")
    private LocalDateTime userActionAt;
}
