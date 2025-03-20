package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.ChallengeStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeStatRepository extends JpaRepository<ChallengeStat, Integer> {
    List<ChallengeStat> findTop10ByOrderByPopularityDesc();
}
