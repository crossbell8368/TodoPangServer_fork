package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.review.ChallengeReviewStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeReviewStatRepository extends JpaRepository<ChallengeReviewStat, Integer> {
    List<ChallengeReviewStat> findAllByInfoId(Integer infoId);
}

