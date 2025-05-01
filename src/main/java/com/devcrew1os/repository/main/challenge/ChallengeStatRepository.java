package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeStatRepository extends JpaRepository<ChallengeStat, Integer> {
    boolean existsById(Integer id);

    List<ChallengeStat> findTop10ByOrderByPopularityDesc();

    @Modifying
    @Query("UPDATE ChallengeStat cs SET cs.popularity = cs.popularity + 1 WHERE cs.id = :challengeId")
    int updatePopularity(@Param("challengeId") Integer challengeId);
}
