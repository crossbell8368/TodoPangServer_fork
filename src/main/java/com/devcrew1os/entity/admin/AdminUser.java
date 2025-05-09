package com.devcrew1os.entity.admin;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_user", schema = "admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_user_id")
    private int id;

    @Column(name = "user_id", unique = true)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_status", nullable = false)
    private int status;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_withdraw_at")
    private LocalDateTime deletedAt;
}
