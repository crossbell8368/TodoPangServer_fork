package com.devcrew1os.repository.review;

import com.devcrew1os.entity.review.ReviewChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewChallengeRepository extends JpaRepository<ReviewChallenge, Integer> {

    @Query("SELECT revc FROM ReviewChallenge revc JOIN FETCH revc.review rev WHERE rev.status = :status AND revc.challenge.id = :challengeId")
    List<ReviewChallenge> findAllByChallengeIdAndStatus(@Param("status") int status, @Param("challengeId") int challengeId);

    @Modifying
    @Query("UPDATE ReviewChallenge rvch SET rvch.userSelectedCount = rvch.userSelectedCount + 1 WHERE rvch.review.id = :reviewId AND rvch.challenge.id = :challengeId")
    void updateReviewChallengeSelected(@Param("reviewId") int reviewId, @Param("challengeId") int challengeId);
}

