package com.devcrew1os.repository.main.review;

import com.devcrew1os.entity.review.ReviewChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewChallengeRepository extends JpaRepository<ReviewChallenge, Integer> {
    @Query("SELECT revc FROM ReviewChallenge revc JOIN FETCH revc.review rev WHERE rev.status = :status AND revc.challenge.id = :challengeId")
    List<ReviewChallenge> findAllByChallengeIdAndStatus(@Param("status") int status, @Param("challengeId") int challengeId);
}

