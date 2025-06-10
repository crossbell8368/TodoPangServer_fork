package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.repository.projection.AdminTodoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {

    @Query("SELECT t FROM Todo t WHERE t.challenge.id = :challengeId")
    List<Todo> findAllByChallengeId(@Param("challengeId") int challengeId);

    @Query("SELECT t FROM Todo t " +
            "WHERE t.id IN :todoIds")
    List<Todo> findAllWithTodoIds(
            @Param("todoIds") Set<Integer> todoIds
    );

    @Query("SELECT t FROM Todo t WHERE t.challenge.id =:challengeId AND t.status =:status")
    List<Todo> findAllByChallengeIdAndStatus(
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query("SELECT t.id FROM Todo t WHERE t.challenge.id = :challengeId AND t.id IN :todoIds")
    List<Integer> findIdsByChallengeIdAndIdIn(
            @Param("challengeId") int challengeId,
            @Param("todoIds") List<Integer> todoIds
    );

    @Query("SELECT t.id as id, t.challenge.id as challengeId, t.order as todoOrder, " +
            "t.desc as desc, u.userName as updatedBy, t.updatedAt as updatedAt " +
            "FROM Todo t " +
            "LEFT JOIN Users u ON t.updatedBy = u.userId " +
            "WHERE t.challenge.id IN :challengeIds AND t.status <> :status")
    List<AdminTodoProjection> findAllTodoProjections(
            @Param("challengeIds") Set<Integer> challengeIds,
            @Param("status") int status
    );
}
