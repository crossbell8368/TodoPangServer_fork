package com.devcrew1os.entity.challenge;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(mappedBy = "challenge")
    private ChallengeStat stat;

    @Column(name = "challenge_title", nullable = false)
    private String title;

    @Column(name = "challenge_desc", nullable = false, columnDefinition = "TEXT")
    private String desc;

    @Column(name = "challenge_term", nullable = false)
    private int term;

    @Column(name = "challenge_diff", nullable = false)
    private int diff;

    @Column(name = "challenge_status", nullable = false)
    private int status;

    @Column(name = "challenge_last_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "challenge_last_updated_by")
    private String updatedBy;
}
