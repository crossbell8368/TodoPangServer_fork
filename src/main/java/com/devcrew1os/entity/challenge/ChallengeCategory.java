package com.devcrew1os.entity.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_category_info")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_category_info_id")
    private int id;

    @Column(name = "challenge_category_title", nullable = false, length = 255)
    private String title;
}
