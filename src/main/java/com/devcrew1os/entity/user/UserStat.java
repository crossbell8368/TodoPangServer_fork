package com.devcrew1os.entity.user;

import lombok.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stat")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStat {

    /*===========================
       Parameter
    ===========================*/
    @Id
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "stat_term", nullable = false)
    private int serviceTerm;

    @Column(name = "stat_user_last_login_at", nullable = false)
    private LocalDateTime lastLoginAt;

    @Column(name = "stat_user_last_logout_at", nullable = false)
    private LocalDateTime lastLogoutAt;

    @Column(name = "stat_total_registered_challenges", nullable = false)
    private int totalRegisteredChallenges;

    @Column(name = "stat_total_finished_challenges", nullable = false)
    private int totalFinishedChallenges;

    @Column(name = "stat_total_suspend_challenges", nullable = false)
    private int totalSuspendChallenges;
}
