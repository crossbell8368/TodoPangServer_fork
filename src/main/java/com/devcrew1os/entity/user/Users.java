package com.devcrew1os.entity.user;

import com.devcrew1os.entity.project.Project;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_role", nullable = false)
    private int role;

    @Column(name = "user_status", nullable = false)
    private int status;

    @Column(name = "user_social_type", nullable = false)
    private int socialType;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_withdraw_reason", nullable = false)
    private int withdrawReason;

    @Column(name = "user_withdraw_at")
    private LocalDateTime withdrawAt;

    @OneToOne(mappedBy = "users")
    private UserStat stat;

    @OneToOne(mappedBy = "users")
    private Project project;
}
