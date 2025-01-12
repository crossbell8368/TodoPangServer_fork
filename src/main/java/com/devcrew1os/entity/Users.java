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

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // 필드 이름을 카멜 케이스로 변경

    @Column(name = "user_email", nullable = false, unique = true)
    private String userEmail; // 카멜 케이스로 변경

    @Column(name = "user_name", nullable = false)
    private String userName; // 카멜 케이스로 변경

    @Column(name = "user_social_type")
    @Convert(converter = UserSocialTypeConverter.class)
    private UserSocialType userSocialType; // 카멜 케이스로 변경

    @Column(name = "user_status")
    @Convert(converter = UserStatusConverter.class)
    private UserStatus userStatus; // 카멜 케이스로 변경

    @Column(name = "user_created_at", nullable = false, updatable = false)
    private LocalDateTime userCreatedAt; // 카멜 케이스로 변경

    @Column(name = "user_updated_at")
    private LocalDateTime userUpdatedAt; // 카멜 케이스로 변경

    @Column(name = "user_deleted_at")
    private LocalDateTime userDeletedAt; // 카멜 케이스로 변경

    @Column(name = "user_last_login_at")
    private LocalDateTime userLastLoginAt; // 카멜 케이스로 변경

    @Column(name = "user_last_logout_at")
    private LocalDateTime userLastLogoutAt; // 카멜 케이스로 변경
}
