package com.devcrew1os.entity;

import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.common.util.converter.UserSocialTypeConverter;
import com.devcrew1os.common.util.converter.UserStatusConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Users {

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

    @Column(name = "user_social_type")
    @Convert(converter = UserSocialTypeConverter.class)
    private UserSocialType userSocialType;

    @Column(name = "user_status")
    @Convert(converter = UserStatusConverter.class)
    private UserStatus userStatus;

    @Column(name = "user_created_at", nullable = false, updatable = false)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at")
    private LocalDateTime userUpdatedAt;

    @Column(name = "user_deleted_at")
    private LocalDateTime userDeletedAt;

    @Column(name = "user_last_login_at")
    private LocalDateTime userLastLoginAt;

    @Column(name = "user_last_logout_at")
    private LocalDateTime userLastLogoutAt;

    /*===========================
       Function
    ===========================*/
    public void updateUserStatus(UserStatus userStatus) {

        // shared
        this.userStatus = userStatus;
        this.userUpdatedAt = LocalDateTime.now();

        // withdraw
        if (userStatus == UserStatus.DELETED) {
            this.userDeletedAt = LocalDateTime.now();
            this.userLastLoginAt = LocalDateTime.now();
        }
    }
}
