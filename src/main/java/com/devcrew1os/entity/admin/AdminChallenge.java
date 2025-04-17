package com.devcrew1os.entity.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_info", schema = "admin")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_info_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_info_id", referencedColumnName = "category_info_id")
    private AdminCategory category;

    @Column(name = "challenge_title", nullable = false)
    private String title;

    @Column(name = "challenge_term", nullable = false)
    private int term;

    @Column(name = "challenge_diff", nullable = false)
    private int diff;

    @Column(name = "challenge_todo_count", nullable = false)
    private int todoCount;

    @Column(name = "challenge_status", nullable = false)
    private int status;

    @Column(name = "challenge_last_updated", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "challenge_last_updated_admin", nullable = false)
    private String lastUpdatedBy;
}
