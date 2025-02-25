package com.devcrew1os.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "stat")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stat {

    /*===========================
       Parameter
    ===========================*/
    @Id
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "stat_term", nullable = false)
    private int statTerm;

    @Column(name = "stat_user_status", nullable = false)
    private int statUserStatus;

    @Column(name = "stat_user_created_at", nullable = false)
    private LocalDateTime statUserCreatedAt;

    @Column(name = "stat_user_updated_at", nullable = false)
    private LocalDateTime statUserUpdatedAt;

    @Column(name = "stat_user_deleted_at", nullable = false)
    private LocalDateTime statUserDeletedAt;

    @Column(name = "stat_user_last_login_at", nullable = false)
    private LocalDateTime statUserLastLoginAt;

    @Column(name = "stat_user_last_logout_at", nullable = false)
    private LocalDateTime statUserLastLogoutAt;

    @Column(name = "stat_registed_projects", nullable = false)
    private int statRegistedProjects;

    @Column(name = "stat_finished_projects", nullable = false)
    private int statFinishedProjects;

    @Column(name = "stat_failed_projects", nullable = false)
    private int statFailedProjects;

    @Column(name = "stat_registed_todos", nullable = false)
    private int statRegistedTodos;

    @Column(name = "stat_finished_todos", nullable = false)
    private int statFinishedTodos;

    /*===========================
       Function
    ===========================*/
}
