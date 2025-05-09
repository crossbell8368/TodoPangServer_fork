package com.devcrew1os.entity.project;

import com.devcrew1os.entity.challenge.Challenge;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "project_challenge")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_challenge_id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "project_challenge_status", nullable = false)
    private int status;
}
