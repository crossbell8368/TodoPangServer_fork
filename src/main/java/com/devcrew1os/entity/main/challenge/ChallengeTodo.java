package com.devcrew1os.entity.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_todo_info")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_todo_id")
    private int id;

    @Column(name = "challenge_info_id")
    private int challengeInfoId;

    @Column(name = "challenge_todo_desc", nullable = false, columnDefinition = "TEXT")
    private String desc;
}
