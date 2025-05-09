package com.devcrew1os.entity.challenge;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "challenge_stat")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeStat {

    @Id
    @Column(name = "challenge_id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @Column(name = "challenge_include_todos_count", nullable = false)
    private int includeTodoCount;

    @Column(name = "challenge_participate_user_count", nullable = false)
    private int participateUserCount;

    @Column(name = "challenge_completed_user_count", nullable = false)
    private int completedUserCount;

    @Column(name = "challenge_registered_review_count", nullable = false)
    private int registeredReviewCount;

    @Column(name = "challenge_avg_satisfaction_ratio")
    private Float averageSatisfactionRatio;
}
