package com.devcrew1os.entity.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_info", schema = "admin")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReview {

    @Id
    @Column(name = "review_info_id", nullable = false, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_info_id", referencedColumnName = "challenge_info_id")
    private AdminChallenge challenge;

    @Column(name = "review_desc", nullable = false)
    private String desc;

    @Column(name = "review_status", nullable = false)
    private int status;

    @Column(name = "review_last_updated", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "review_last_updated_admin", nullable = false)
    private String lastUpdatedBy;
}
