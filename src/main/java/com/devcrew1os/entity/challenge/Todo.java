package com.devcrew1os.entity.challenge;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "todo_order")
    private int order;

    @Column(name = "todo_desc", nullable = false, columnDefinition = "TEXT")
    private String desc;

    @Column(name = "todo_status", nullable = false)
    private int status;

    @Column(name = "todo_last_updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "todo_last_updated_by")
    private String updatedBy;
}
