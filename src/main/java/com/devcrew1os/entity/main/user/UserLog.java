package com.devcrew1os.entity.main.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_log_id")
    private int id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "project_id")
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
