package com.devcrew1os.entity.admin.challenge;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_category_info", schema = "admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_category_info_id")
    private int id;

    @OneToOne(mappedBy = "categoryInfo")
    private AdminCategoryStat stat;

    @Column(name = "challenge_category_status", nullable = false)
    private int status;

    @Column(name = "challenge_category_title", nullable = false, length = 255)
    private String title;

    @Column(name = "challenge_category_last_updated", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "challenge_category_last_updated_admin", nullable = false)
    private String lastUpdatedBy;

}
