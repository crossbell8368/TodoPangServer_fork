package com.devcrew1os.repository.main.project;

import com.devcrew1os.entity.project.ProjectChallenge;
import com.devcrew1os.repository.main.projection.ProjectChallengeProjection;
import com.devcrew1os.repository.main.projection.ProjectTodoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectChallengeRepository extends JpaRepository<ProjectChallenge, Integer> {

    @Query("SELECT count(pc) > 0 " +
            "FROM ProjectChallenge pc " +
            "WHERE pc.project.id = :projectId " +
            "AND pc.challenge.id = :challengeId " +
            "AND pc.status = :status")
    boolean existsByByAllCriteria(
            @Param("projectId") int projectId,
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query("SELECT count(pc) < 10 " +
            "FROM ProjectChallenge pc " +
            "WHERE pc.project.id = :projectId")
    boolean checkProjectChallengeLimit(@Param("projectId") int projectId);

    @Query("SELECT pc FROM ProjectChallenge pc JOIN FETCH pc.challenge WHERE pc.project.id = :projectId")
    List<ProjectChallenge> findAllByProjectIdWithChallenge(@Param("projectId") int projectId);

    @Query("SELECT pc.id as id, pc.status as status, pc.challenge as challenge " +
            "FROM ProjectChallenge pc JOIN pc.challenge ch " +
            "WHERE pc.project.id = :projectId " +
            "AND pc.status = :status"
    )
    List<ProjectChallengeProjection> findAllDataByProjectId(@Param("projectId") int projectId, @Param("status") int status);
}
