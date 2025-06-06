package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Integer> {
    @Query("SELECT t FROM Todo t " +
            "JOIN FETCH t.challenge ch " +
            "WHERE t.id IN :todoList " +
            "AND t.status <> :status")
    List<Todo> findAllByIdIn(@Param("todoList") Set<Integer> ids, @Param("status") int status);

    @Query("SELECT t FROM Todo t WHERE t.challenge.id = :challengeId")
    List<Todo> findAllByChallengeId(@Param("challengeId") int challengeId);

    @Query("SELECT t FROM Todo t JOIN FETCH t.challenge ch " +
            "WHERE ch.id IN :challengeIds " +
            "AND t.status <> :status")
    List<Todo> findAllWithChallengeIds(
            @Param("challengeIds") Set<Integer> challengeIds,
            @Param("status") int status
    );

    @Query("SELECT t FROM Todo t WHERE t.challenge.id =:challengeId AND t.status =:status")
    List<Todo> findAllByChallengeIdAndStatus(@Param("challengeId") int challengeId, @Param("status") int status);

    @Query("SELECT t.id FROM Todo t WHERE t.challenge.id = :challengeId AND t.id IN :todoIds")
    List<Integer> findIdsByChallengeIdAndIdIn(@Param("challengeId") int challengeId, @Param("todoIds") List<Integer> todoIds);
}
