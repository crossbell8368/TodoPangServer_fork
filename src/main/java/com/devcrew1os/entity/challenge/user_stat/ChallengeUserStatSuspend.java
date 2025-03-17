package com.devcrew1os.entity.challenge.user_stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_user_stat_suspend")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeUserStatSuspend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_user_stat_suspend_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "challenge_info_id", nullable = false)
    private int infoId;

    @Column(name = "challenge_user_stat_suspend_count", nullable = false)
    private int count;

    @Column(name = "challenge_user_stat_suspend_at", nullable = false)
    private LocalDateTime suspendAt;
}
