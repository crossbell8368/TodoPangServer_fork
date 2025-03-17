package com.devcrew1os.entity.challenge.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_review_info")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeReviewInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_review_info_id")
    private int id;

    @Column(name = "challenge_review_info_desc", nullable = false, length = 255)
    private String desc;
}
