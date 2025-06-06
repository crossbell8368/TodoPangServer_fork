package com.devcrew1os.service.admin.review;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.admin.review.*;
import com.devcrew1os.entity.review.Review;
import com.devcrew1os.repository.projection.AdminReviewProjection;
import com.devcrew1os.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReviewTransaction {

    private final ReviewRepository reviewRepo;

    private final static Logger logger = LoggerFactory.getLogger(AdminReviewTransaction.class);

    /*===========================
       리뷰 조회
    ===========================*/
    @Transactional(readOnly = true)
    public Page<GetAdminReviewData> getAdminReviewProcess(Pageable pageable) {
        // 1. fetch data: Review
        Page<AdminReviewProjection> reviewProjections = reviewRepo.findAllWithUserName(pageable);

        List<GetAdminReviewData> dtoList = reviewProjections.getContent().stream()
                .map(proj -> new GetAdminReviewData(
                        proj.getReviewId(),
                        proj.getEmoji(),
                        proj.getStatus(),
                        proj.getTitle(),
                        proj.getUpdatedBy(),
                        proj.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, reviewProjections.getTotalElements());
    }

    /*===========================
       리뷰 추가
    ===========================*/
    @Transactional
    public void setReviewProcess(String adminId, SetAdminReviewReq req) {
        LocalDateTime now = LocalDateTime.now();

        Review newReview = Review.builder().
                title(req.getTitle()).
                emoji(req.getEmoji()).
                status(DataStatus.PREPARE.getValue()).
                updatedAt(now).
                updatedBy(adminId).
                build();
        reviewRepo.save(newReview);
    }

    /*===========================
       리뷰 업데이트: single
    ===========================*/
    @Transactional
    public void updateReviewProcess(String adminId, UpdateAdminReviewReq req) {
        // 1. fetch data: review
        Optional<Review> reviewOpt = reviewRepo.findById(req.getReviewId());
        if(reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }
        // 2. update data
        Review entity = reviewOpt.get();
        boolean isUpdated = false;

        if(entity.getStatus() != DataStatus.PREPARE.getValue()) {
            throw new RuntimeException("Review status is not PREPARE");
        }
        if(req.getNewTitle() != null) {
            entity.setTitle(req.getNewTitle());
            isUpdated = true;
        }
        if(req.getNewEmoji() != null) {
            entity.setEmoji(req.getNewEmoji());
            isUpdated = true;
        }
        if(isUpdated){
            entity.setUpdatedBy(adminId);
            entity.setUpdatedAt(LocalDateTime.now());
            reviewRepo.save(entity);
        } else {
            logger.warn("Review not updated");
        }
    }

    /*===========================
       리뷰 배포(update status): multi
    ===========================*/
    @Transactional
    public void deployReviewProcess(String adminId, DeployAdminReviewReq req, DeployAdminReviewRes res) {
        // 1. fetch data: reviewList
        Set<Integer> targetReviewIdSet = req.getData().stream()
                .map(DeployAdminReviewData::getReviewId)
                .collect(Collectors.toSet());
        List<Review> targetReviewList = reviewRepo.findAllByIdIn(targetReviewIdSet);

        if(targetReviewIdSet.size() != targetReviewList.size()) {
            throw new RuntimeException("Review not matched");
        }
        // 2. assemble data: toMap
        Map<Integer, Review> targetReviewMap = targetReviewList.stream()
                .collect(Collectors.toMap(
                        Review::getId,
                        review -> review
                ));
        LocalDateTime now = LocalDateTime.now();

        // 3. loop data
        for(DeployAdminReviewData newData : req.getData()) {
            Review target = targetReviewMap.get(newData.getReviewId());
            if (target == null) {
                res.addMessage("ReviewId(" + newData.getReviewId() + ") not found in database");
                logger.warn("ReviewId {} requested for deployment but not found.", newData.getReviewId());
                continue;
            }

            int currentStatus = target.getStatus();
            int newStatus = newData.getNewStatus();
            boolean isUpdated = false;

            if (currentStatus == newStatus) {
                res.addMessage("ReviewId(" + target.getId() + ") is already in the requested status: " + DataStatus.fromValue(newStatus).name());
                continue;
            }

            switch(DataStatus.fromValue(newStatus)) {
                case DEPLOYED:
                    if (currentStatus == DataStatus.PREPARE.getValue()) {
                        if(!reviewRepo.existsReviewByTitleAndStatus(target.getTitle(), DataStatus.DEPLOYED.getValue())){
                            target.setStatus(newStatus);
                            res.addMessage("ReviewId(" + target.getId() + ") turned into DEPLOYED");
                            isUpdated = true;
                        } else {
                            res.addMessage("ReviewId(" + target.getId() + ") title already DEPLOYED");
                        }
                    } else {
                        res.addMessage("ReviewId(" + target.getId() + ") cannot be DEPLOYED from status " + DataStatus.fromValue(currentStatus).name());
                    }
                    break;
                case PREPARE:
                    if (currentStatus == DataStatus.DEPLOYED.getValue() || currentStatus == DataStatus.DELETE.getValue()) {
                        target.setStatus(newStatus);
                        res.addMessage("ReviewId(" + target.getId() + ") turned into PREPARED");
                        isUpdated = true;
                    } else {
                        res.addMessage("ReviewId(" + target.getId() + ") is already PREPARED");
                    }
                    break;
                case DELETE:
                    if (currentStatus == DataStatus.PREPARE.getValue()) {
                        target.setStatus(newStatus);
                        res.addMessage("ReviewId(" + target.getId() + ") turned into DELETED");
                        isUpdated = true;
                    } else {
                        res.addMessage("ReviewId(" + target.getId() + ") cannot be DELETED from status " + DataStatus.fromValue(currentStatus).name());
                    }
                    break;
            }
            if(isUpdated){
                target.setUpdatedBy(adminId);
                target.setUpdatedAt(now);
            }
        }
        reviewRepo.saveAll(targetReviewMap.values());
    }
}
