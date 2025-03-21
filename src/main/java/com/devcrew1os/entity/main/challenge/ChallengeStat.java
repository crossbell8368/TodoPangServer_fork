package com.devcrew1os.entity.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_stat")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeStat {

    @Id
    @Column(name = "challenge_info_id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "challenge_info_id")
    private ChallengeInfo challengeInfo;

    @Column(name = "challenge_popularity", nullable = false)
    private int popularity;

    @Column(name = "challenge_completed_user_count", nullable = false)
    private int completedCount;

    @Column(name = "challenge_avg_user_progress", nullable = false)
    private float averageProgress;
}
