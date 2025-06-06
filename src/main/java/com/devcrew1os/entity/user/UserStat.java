package com.devcrew1os.entity.user;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users_stat")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStat {

    @Id
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "user_service_used_days", nullable = false)
    private int serviceTerm;

    @Column(name = "last_login_at", nullable = false)
    private LocalDateTime lastLoginAt;

    @Column(name = "last_logout_at", nullable = false)
    private LocalDateTime lastLogoutAt;

    @Column(name = "total_registered_challenges_count", nullable = false)
    private int registeredChallenges;

    @Column(name = "total_completed_challenges_count", nullable = false)
    private int completedChallenges;

    @Column(name = "total_registered_reviews_count", nullable = false)
    private int registeredReviews;
}
