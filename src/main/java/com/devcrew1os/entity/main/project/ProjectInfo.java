package com.devcrew1os.entity.main.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_info")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private int id;

    @Column(name = "user_id")
    private String userId;

    @OneToMany(mappedBy = "project")
    private List<ProjectChallenges> challengesList = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    private List<ProjectTodos> todoList = new ArrayList<>();
}
