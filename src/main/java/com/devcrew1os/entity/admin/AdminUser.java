package com.devcrew1os.entity.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_info", schema = "admin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String id;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_status", nullable = false)
    private int status;

    @Column(name = "user_desc", columnDefinition = "TEXT")
    private String desc;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_withdraw_at")
    private LocalDateTime deletedAt;
}
