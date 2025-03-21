package com.devcrew1os.entity.main.challenge.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_review_stat")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeReviewStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_review_stat_id")
    private int id;

    @Column(name = "challenge_info_id")
    private int infoId;

    @Column(name = "challenge_review_info_id")
    private int reviewInfoId;

    @Column(name = "challenge_review_count", nullable = false)
    private int count;
}
