package com.devcrew1os.entity.admin;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_info", schema = "admin")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_info_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_info_id", referencedColumnName = "challenge_info_id")
    private AdminChallenge challenge;

    @Column(name = "todo_order", nullable = false)
    private int order;

    @Column(name = "todo_desc", columnDefinition = "TEXT")
    private String desc;

    @Column(name = "todo_last_updated", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "todo_last_updated_admin")
    private String lastUpdatedBy;
}
