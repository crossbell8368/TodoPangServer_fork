package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.repository.projection.AdminTodoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {

    @Modifying
    @Query("UPDATE Todo t SET t.status = :status, t.updatedAt = :now, t.updatedBy = :adminId " +
            "WHERE t.challenge.id IN :challengeIds")
    void updateStatusByChallengeIds(
            @Param("status") int status,
            @Param("adminId") String adminId,
            @Param("now") LocalDateTime now,
            @Param("challengeIds") List<Integer> challengeIds);

    @Query("SELECT t FROM Todo t " +
            "WHERE t.challenge.id = :challengeId")
    List<Todo> findAllByChallengeId(@Param("challengeId") int challengeId);

    @Query("SELECT t FROM Todo t " +
            "WHERE t.id IN :todoIds")
    List<Todo> findAllByTodoIds(
            @Param("todoIds") Set<Integer> todoIds
    );

    @Query("SELECT t FROM Todo t " +
            "WHERE t.challenge.id =:challengeId AND t.status =:status")
    List<Todo> findAllByChallengeIdAndStatus(
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query("SELECT t.id FROM Todo t " +
            "WHERE t.challenge.id = :challengeId AND t.id IN :todoIds")
    List<Integer> findAllIdsByChallengeIdAndTodoIds(
            @Param("challengeId") int challengeId,
            @Param("todoIds") List<Integer> todoIds
    );

    @Query("SELECT t FROM Todo t " +
            " WHERE t.challenge.id IN :challengeIds AND t.status = :status")
    List<Todo> findAllByChallengeIdsAndStatus(
            @Param("challengeIds") List<Integer> challengeIds,
            @Param("status") int status
    );

    @Query("SELECT t.id as id, t.challenge.id as challengeId, t.order as todoOrder, " +
            "t.desc as desc, u.userName as updatedBy, t.updatedAt as updatedAt " +
            "FROM Todo t " +
            "LEFT JOIN Users u ON t.updatedBy = u.userId " +
            "WHERE t.challenge.id IN :challengeIds AND t.status <> :status")
    List<AdminTodoProjection> findAllProjByChallengeIdsAndStatus(
            @Param("challengeIds") Set<Integer> challengeIds,
            @Param("status") int status
    );
}
