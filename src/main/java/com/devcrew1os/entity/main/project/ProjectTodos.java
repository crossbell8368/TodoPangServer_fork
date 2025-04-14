package com.devcrew1os.entity.main.project;

import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "project_todo")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectTodos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_todo_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private ProjectInfo project;

    @ManyToOne
    @JoinColumn(name = "challenge_todo_id", referencedColumnName = "challenge_todo_id")
    private ChallengeTodo challengeTodo;
}
