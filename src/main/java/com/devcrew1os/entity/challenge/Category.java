package com.devcrew1os.entity.challenge;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int id;

    @Column(name = "category_title", nullable = false, length = 255)
    private String title;

    @Column(name = "category_status", nullable = false)
    private int status;

    @Column(name = "category_last_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "category_last_updated_by")
    private String updatedBy;
}
