package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.repository.projection.AdminChallengeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    @Query("SELECT count(ch) > 0 FROM Challenge ch " +
            "WHERE ch.id = :challengeId " +
            "AND ch.status = :status")
    boolean existsChallengeByIdAndStatus(
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query("SELECT count(ch) > 0 FROM Challenge ch " +
            "WHERE ch.title = :title " +
            "AND ch.status = :status")
    boolean existsChallengeByTitleAndStatus(
            @Param("title") String title,
            @Param("status") int status
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "WHERE ch.id = :challengeId")
    Optional<Challenge> findChallengeByIdWithStat(
            @Param("challengeId") int challengeId
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "JOIN FETCH ch.category cat " +
            "WHERE ch.id = :challengeId " +
            "AND ch.status = :status")
    Optional<Challenge> findChallengeByIdWithCategory(
            @Param("challengeId") int challengeId,
            @Param("status") int status)
            ;

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "WHERE ch.id IN :challengeIds")
    List<Challenge> findAllChallengesWithIds(
            @Param("challengeIds") Set<Integer> challengeIds
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.category cat " +
            "JOIN FETCH ch.stat st " +
            "WHERE cat.status = :dataStatus " +
            "AND ch.status = :dataStatus ORDER BY ch.id DESC")
    List<Challenge> findAllByStatusDesc(@Param("dataStatus") int dataStatus);

    @Query(value = "SELECT " +
            "ch.id as id, cat.id as categoryId, ch.title as title, ch.term as term, " +
            "ch.diff as diff, ch.status as status, ch.updatedAt as updatedAt, " +
            "u.userName as updatedBy, st.includeTodoCount as includeTodoCount " +
            "FROM Challenge ch " +
            "LEFT JOIN ch.category cat " +
            "LEFT JOIN ch.stat st " +
            "LEFT JOIN Users u ON ch.updatedBy = u.userId",
            countQuery = "SELECT count(ch) FROM Challenge ch")
    Page<AdminChallengeProjection> findAllChallengeProjection(Pageable pageable);
}
