package com.devcrew1os.entity.project;

import com.devcrew1os.entity.user.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Users users;

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectChallenge> challengeList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectTodo> todoList = new ArrayList<>();
}
