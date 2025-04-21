package com.devcrew1os.entity.main.project;

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
    @Column(name = "project_todo_id")
    private int id;

    @Column(name = "project_id")
    private int projectId;

    @Column(name = "challenge_info_id")
    private int projectChallengeId;

    @Column(name = "challenge_todo_id")
    private int projectTodoId;

    @Column(name = "project_todo_status")
    private int projectTodoStatus;
}
