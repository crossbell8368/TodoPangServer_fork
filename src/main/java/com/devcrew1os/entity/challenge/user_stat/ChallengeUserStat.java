package com.devcrew1os.entity.challenge.user_stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_user_stat")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeUserStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_user_stat_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "challenge_info_id")
    private int infoId;

    @Column(name = "challenge_status")
    private int status;

    @Column(name = "challenge_progress")
    private int progress;

    @Column(name = "challenge_selected_review")
    private int selectedReviewId;

    @Column(name = "challenge_last_updated_at")
    private LocalDateTime lastUpdatedAt;
}
