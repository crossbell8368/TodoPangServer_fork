package com.devcrew1os.service.main.review;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.project.ProjectChallengeStatus;
import com.devcrew1os.dto.main.review.GetReviewData;
import com.devcrew1os.dto.main.review.UpdateReviewReq;
import com.devcrew1os.entity.review.Review;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.project.ProjectChallengeRepository;
import com.devcrew1os.repository.review.ReviewChallengeRepository;
import com.devcrew1os.repository.review.ReviewRepository;
import com.devcrew1os.repository.users.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewTransaction {

    private final ReviewRepository reviewRepo;
    private final UserStatRepository userStatRepo;
    private final ReviewChallengeRepository reviewChallengeRepo;
    private final ChallengeStatRepository challengeStatRepo;
    private final ProjectChallengeRepository projectChallengeRepo;

    /*===========================
       리뷰 조회
    ===========================*/
    @Transactional(readOnly = true)
    public List<GetReviewData> getReviewProcess() {

        List<Review> reviewList = reviewRepo.findAllByStatus(DataStatus.DEPLOYED.getValue());
        if(reviewList.isEmpty()){
            throw new RuntimeException("Review not found");
        }

        return reviewList.stream()
                .map(rv -> new GetReviewData(
                        rv.getId(),
                        rv.getEmoji(),
                        rv.getTitle()))
                .collect(Collectors.toList());
    }

    /*===========================
       리뷰 업데이트
    ===========================*/
    @Transactional
    public void updateReview(String userId, UpdateReviewReq req) {

        if(!projectChallengeRepo.existsByUserIdAndChallengeIdAndStatus(
                userId, req.getOriginChallengeId(), ProjectChallengeStatus.FINISHED.getValue())){
            throw new RuntimeException("Registered Challenge not found");
        }

        // challengeReview 업데이트
        reviewChallengeRepo.updateReviewChallengeSelected(req.getSelectedReviewId(), req.getOriginChallengeId());

        // ChallengeStat 평균값 업데이트
        challengeStatRepo.updateSatisfactionAndReviewCount(req.getOriginChallengeId(), req.getSatisfiedRating());

        // UserStat 업데이트
        userStatRepo.updateRegisteredReview(userId);
    }
}
