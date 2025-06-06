package com.devcrew1os.repository.project;

import com.devcrew1os.entity.project.ProjectTodo;
import com.devcrew1os.repository.projection.ProjectTodoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectTodoRepository extends JpaRepository<ProjectTodo, Integer> {

    @Query("SELECT COUNT(pt) FROM ProjectTodo pt " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.challenge.id = :challengeId " +
            "AND (pt.status = :firstStatus OR pt.status = :secondStatus) " +
            "AND pt.status <> :finalStatus")
    int countActiveTodoCount(
            @Param("projectId") int projectId,
            @Param("challengeId") int challengeId,
            @Param("firstStatus") int firstStatus,
            @Param("secondStatus") int secondStatus,
            @Param("finalStatus") int finalStatus);

    @Query("SELECT pt FROM ProjectTodo pt " +
            "JOIN FETCh pt.challenge ch " +
            "JOIN FETCh pt.todo t " +
            "WHERE pt.project.id = :projectId " +
            "AND (pt.status = :firstStatus OR pt.status = :secondStatus)")
    List<ProjectTodo> findAllByProjectIdWithStatus(
            @Param("projectId") int projectId,
            @Param("firstStatus") int firstStatus,
            @Param("secondStatus") int secondStatus);

    @Query("SELECT pt FROM ProjectTodo pt " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.challenge.id = :challengeId")
    List<ProjectTodo> findAllByProjectIdAndChallengeId(
            @Param("projectId") int projectId,
            @Param("challengeId") int challengeId);

    @Query("SELECT pt.id as id, pt.status as status, pt.challenge.id as challengeId, pt.todo as todo " +
            "FROM ProjectTodo pt JOIN pt.todo t " + // 일반 JOIN 사용 가능
            "WHERE pt.project.id = :projectId " +
            "AND pt.status <> :firstStatus " +
            "AND pt.status <> :secondStatus")
    List<ProjectTodoProjection> findAllDataByProjectId(
            @Param("projectId") int projectId,
            @Param("firstStatus") int firstStatus,
            @Param("secondStatus") int secondStatus);

    @Query("SELECT pt FROM ProjectTodo pt " +
            "WHERE pt.project.id = :projectId " +
            "AND pt.todo.id IN :todoIds " +
            "AND pt.status <> :status")
    List<ProjectTodo> findAllByProjectAndTodoIdInWithStatus(
            @Param("projectId") int projectId,
            @Param("todoIds") Set<Integer> todoIds,
            @Param("status") int status);
}
