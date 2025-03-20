package com.devcrew1os.entity.user;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    /*===========================
       Parameter
    ===========================*/
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_social_type", nullable = false)
    private int socialType;

    @Column(name = "user_status", nullable = false)
    private int status;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "userInfo")
    private UserStat stat;
}
