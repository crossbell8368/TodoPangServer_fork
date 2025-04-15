package com.devcrew1os.entity.main.challenge;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "challenge_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_info_id")
    private int id;

    @ManyToOne()
    @JoinColumn(name = "challenge_category_info_id", referencedColumnName = "challenge_category_info_id")
    private ChallengeCategory category;

    @OneToOne(mappedBy = "challengeInfo")
    private ChallengeStat stat;

    @Column(name = "challenge_title", nullable = false)
    private String title;

    @Column(name = "challenge_desc", nullable = false, columnDefinition = "TEXT")
    private String desc;

    @Column(name = "challenge_term", nullable = false)
    private int term;

    @Column(name = "challenge_diff", nullable = false)
    private int diff;

    @Column(name = "challenge_todo_count", nullable = false)
    private int todoCount;
}
