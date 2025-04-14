package com.devcrew1os.entity.main.project;

import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "project_challenge")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectChallenges {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_challenge_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private ProjectInfo project;

    @ManyToOne
    @JoinColumn(name = "challenge_info_id", referencedColumnName = "challenge_info_id")
    private ChallengeInfo challengeInfo;
}
