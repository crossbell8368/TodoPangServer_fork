package com.devcrew1os.entity.review;

import com.devcrew1os.entity.challenge.Challenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "review_challenges")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_challenges_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @Column(name = "user_selected_count", nullable = false)
    private int userSelectedCount;
}
