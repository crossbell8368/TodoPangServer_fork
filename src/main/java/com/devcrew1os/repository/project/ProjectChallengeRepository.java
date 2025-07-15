package com.devcrew1os.repository.project;

import com.devcrew1os.entity.project.ProjectChallenge;
import com.devcrew1os.repository.projection.ProjectChallengeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectChallengeRepository extends JpaRepository<ProjectChallenge, Integer> {

    @Modifying
    @Query("UPDATE ProjectChallenge pc " +
            "SET pc.status = :status " +
            "WHERE pc.project.id = :projectId " +
            "AND pc.id = :projectChallengeId")
    void updateProjectChallengeStatus(
            @Param("status") int status,
            @Param("projectId") int projectId,
            @Param("projectChallengeId") int projectChallengeId
    );

    @Query("SELECT count(pc) > 0 " +
            "FROM ProjectChallenge pc " +
            "JOIN pc.project p " +
            "WHERE p.users.userId = :userId " +
            "AND pc.challenge.id = :challengeId " +
            "AND pc.status = :status")
    boolean existsByUserIdAndChallengeIdAndStatus(
            @Param("userId") String userId,
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

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
    boolean checkProjectChallengeLimit(
            @Param("projectId") int projectId
    );

    @Query("SELECT pc.challenge.id " +
            "FROM ProjectChallenge pc " +
            "WHERE pc.id = :projectChallengeId " +
            "AND pc.project.id = :projectId")
    Integer getOriginalChallengeId(
            @Param("projectId") int projectId,
            @Param("projectChallengeId") int projectChallengeId
    );

    @Query("SELECT pc.id as projectChallengeId, pc.status as status," +
            "ch.id as challengeId, ch.title as challengeTitle " +
            "FROM ProjectChallenge pc " +
            "JOIN pc.challenge ch " +
            "WHERE pc.project.id = :projectId " +
            "AND pc.status = :status")
    List<ProjectChallengeProjection> findAllDataByProjectId(
            @Param("projectId") int projectId,
            @Param("status") int status
    );
}
