package com.devcrew1os.entity.main.project;

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
public class ProjectChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_challenge_id")
    private int id;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "challenge_info_id")
    private int challengeInfoId;
}
