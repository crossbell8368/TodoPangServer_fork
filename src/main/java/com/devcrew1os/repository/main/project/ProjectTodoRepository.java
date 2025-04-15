package com.devcrew1os.repository.main.project;

import com.devcrew1os.entity.main.project.ProjectTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTodoRepository extends JpaRepository<ProjectTodo, Integer> {
    List<ProjectTodo> findAllByProjectId(int projectId);
}
