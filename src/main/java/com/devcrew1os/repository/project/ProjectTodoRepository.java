package com.devcrew1os.repository.project;

import com.devcrew1os.entity.project.ProjectTodo;
import com.devcrew1os.repository.projection.ProjectTodoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectTodoRepository extends JpaRepository<ProjectTodo, Integer> {

    @Modifying
    @Query("UPDATE ProjectTodo pt " +
            "SET pt.status = :status " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.id = :projectTodoId")
    void updateTodoStatusByProjectIdAndChallengeId(
            @Param("status") int status,
            @Param("projectId") int projectId,
            @Param("projectTodoId") int projectTodoId
    );

    @Modifying
    @Query("UPDATE ProjectTodo pt " +
            "SET pt.status = :status " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.challenge.id = :challengeId")
    void updateAllTodosByProjectIdAndChallengeId(
            @Param("status") int status,
            @Param("projectId") int projectId,
            @Param("challengeId") int challengeId
    );

    @Query("SELECT COUNT(pt) FROM ProjectTodo pt " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.challenge.id = :challengeId " +
            "AND (pt.status = :firstStatus OR pt.status = :secondStatus) ")
    int countActiveTodoCount(
            @Param("projectId") int projectId,
            @Param("challengeId") int challengeId,
            @Param("firstStatus") int firstStatus,
            @Param("secondStatus") int secondStatus
    );

    @Query("SELECT pt.id as id, pt.status as status, pt.challenge.id as challengeId, t.desc as todoDesc " +
            "FROM ProjectTodo pt " +
            "JOIN pt.todo t " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.status <> :firstStatus " +
            "AND pt.status <> :secondStatus")
    List<ProjectTodoProjection> findAllDataByProjectId(
            @Param("projectId") int projectId,
            @Param("firstStatus") int firstStatus,
            @Param("secondStatus") int secondStatus
    );

    @Query("SELECT pt FROM ProjectTodo pt " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.todo.id IN :todoIds " +
            "AND pt.status <> :status")
    List<ProjectTodo> findAllByProjectAndTodoIdInWithStatus(
            @Param("projectId") int projectId,
            @Param("todoIds") Set<Integer> todoIds,
            @Param("status") int status
    );
}
