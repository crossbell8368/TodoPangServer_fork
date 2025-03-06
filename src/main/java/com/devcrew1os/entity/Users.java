package com.devcrew1os.entity;

import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.common.util.converter.UserSocialTypeConverter;
import com.devcrew1os.common.util.converter.UserStatusConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_account")
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
}
