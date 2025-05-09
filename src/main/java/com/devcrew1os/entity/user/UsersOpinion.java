package com.devcrew1os.entity.user;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "users_opinion")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsersOpinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_opinion_id")
    private int id;

    @Column(name = "user_satisfaction_ratio")
    private float satisfactionRatio;

    @Column(name = "user_opinion_desc", columnDefinition = "TEXT")
    private String opinionDesc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
}
