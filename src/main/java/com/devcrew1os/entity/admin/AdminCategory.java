package com.devcrew1os.entity.admin;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category_info", schema = "admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_info_id")
    private int id;

    @Column(name = "category_title", nullable = false, length = 255)
    private String title;

    @Column(name = "category_status", nullable = false)
    private int status;

    @Column(name = "category_challenges_count", nullable = false)
    private int challengesCount;

    @Column(name = "category_last_updated", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "category_last_updated_admin", nullable = false)
    private String lastUpdatedBy;
}
