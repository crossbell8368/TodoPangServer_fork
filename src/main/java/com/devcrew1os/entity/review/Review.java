package com.devcrew1os.entity.review;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int id;

    @Column(name = "review_title", nullable = false, length = 255)
    private String title;

    @Column(name = "review_emoji", nullable = false)
    private int emoji;

    @Column(name = "review_status", nullable = false)
    private int status;

    @Column(name = "review_last_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "review_last_updated_by")
    private String updatedBy;
}
