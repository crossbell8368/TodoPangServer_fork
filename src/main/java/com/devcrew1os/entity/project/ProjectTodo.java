package com.devcrew1os.entity.project;

import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.entity.challenge.Todo;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "project_todo")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_todo_id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge; // challenge_id 컬럼에 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo; // todo_id 컬럼에 매핑

    @Column(name = "project_todo_status")
    private int status;
}
