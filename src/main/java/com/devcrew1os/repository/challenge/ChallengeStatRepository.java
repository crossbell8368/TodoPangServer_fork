package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.ChallengeStat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeStatRepository extends JpaRepository<ChallengeStat, Integer> {
    boolean existsById(Integer id);

    @Query("SELECT cs FROM ChallengeStat cs JOIN FETCH cs.challenge c JOIN FETCH c.category WHERE c.status =:status ORDER BY cs.participateUserCount DESC")
    List<ChallengeStat> findTop10ByPopularityWithDetail(@Param("status") Integer status, Pageable pageable);

    @Modifying
    @Query("UPDATE ChallengeStat cs SET cs.participateUserCount = cs.participateUserCount + 1 WHERE cs.id = :challengeId")
    void updatePopularity(@Param("challengeId") Integer challengeId);

    @Modifying
    @Query("UPDATE ChallengeStat cs SET cs.includeTodoCount = cs.includeTodoCount - 1 WHERE cs.id = :challengeId")
    void updateTotalRegisteredTodos(@Param("challengeId") Integer challengeId);

    @Modifying
    @Query("UPDATE ChallengeStat cs SET cs.averageSatisfactionRatio = " +
                    "(COALESCE(cs.averageSatisfactionRatio, 0.0) * cs.registeredReviewCount + :newScore) / (cs.registeredReviewCount + 1), " +
                    "cs.registeredReviewCount = cs.registeredReviewCount + 1 " +
                    "WHERE cs.id = :challengeId")
    void updateSatisfactionAndReviewCount(
            @Param("challengeId") int challengeId,
            @Param("newScore") float newScore
    );
}
