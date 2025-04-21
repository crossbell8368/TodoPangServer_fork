package com.devcrew1os.repository.main.project;

import com.devcrew1os.entity.main.project.ProjectTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProjectTodoRepository extends JpaRepository<ProjectTodo, Integer> {
    List<ProjectTodo> findAllByProjectId(int projectId);
    List<ProjectTodo> findByProjectIdAndProjectTodoIdIn(int projectId, List<Integer> projectTodoIds);

    @Query("SELECT pt.projectTodoId FROM ProjectTodo pt WHERE pt.projectId = :projectId AND pt.projectTodoId IN :projectTodoIds")
    Set<Integer> findIdsByProjectIdAndProjectTodoIdIn(@Param("projectId") Integer projectId, @Param("projectTodoIds") List<Integer> projectTodoIds);
}
