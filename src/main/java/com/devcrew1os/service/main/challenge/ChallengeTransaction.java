package com.devcrew1os.service.main.challenge;

import com.devcrew1os.dto.main.challenge.ChallengeDetailData;
import com.devcrew1os.dto.main.challenge.ChallengeReviewData;
import com.devcrew1os.dto.main.challenge.ChallengeTodoData;
import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.challenge.review.ChallengeReviewStat;
import com.devcrew1os.repository.main.challenge.ChallengeCategoryRepository;
import com.devcrew1os.repository.main.challenge.ChallengeInfoRepository;
import com.devcrew1os.repository.main.challenge.ChallengeReviewStatRepository;
import com.devcrew1os.repository.main.challenge.ChallengeTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeTransaction {

    private final ChallengeCategoryRepository categoryRepo;
    private final ChallengeInfoRepository infoRepo;
    private final ChallengeTodoRepository todoRepo;
    private final ChallengeReviewStatRepository reviewStatRepo;

    public List<ChallengeCategory> getCategoryList() {
        List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        } else {
            return categoryList;
        }
    }

    public List<ChallengeInfo> getInfoList() {
        List<ChallengeInfo> infoList = infoRepo.findAllByOrderByIdDesc();
        if(infoList.isEmpty()){
            throw new RuntimeException("Infos list is empty");
        } else {
            return infoList;
        }
    }

    public ChallengeDetailData getChallengeDetailData(int challengeId) {
        ChallengeInfo info = getChallengeInfo(challengeId);

        List<ChallengeTodoData> todos = getChallengeTodoList(challengeId).stream()
                .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                .collect(Collectors.toList());

        List<ChallengeReviewData> reviews = getChallengeReview(challengeId).stream()
                .map(review -> new ChallengeReviewData(
                        review.getId(),
                        review.getReviewInfo().getDesc(),
                        review.getCount()))
                .collect(Collectors.toList());
        return new ChallengeDetailData(
                challengeId,
                info.getTitle(),
                info.getStat().getPopularity(),
                info.getCategory().getId(),
                info.getTerm(),
                info.getDiff(),
                todos,
                reviews
        );
    }

    public ChallengeInfo getChallengeInfo(int challengeId) {
        return infoRepo.findById(challengeId).orElseThrow(
                () -> new RuntimeException("ChallengeInfo not found: " + challengeId)
        );
    }

    public List<ChallengeTodo> getChallengeTodoList(int challengeId) {
        List<ChallengeTodo> todoList = todoRepo.findAllByChallengeInfoId(challengeId);
        if(todoList.isEmpty()) {
            throw new RuntimeException("Challenge Todo List is empty");
        } else {
            return todoList;
        }
    }

    public List<ChallengeReviewStat> getChallengeReview(int challengeId) {
        List<ChallengeReviewStat> reviewList = reviewStatRepo.findAllByInfoId(challengeId);
        if(reviewList.isEmpty()){
            throw new RuntimeException("ChallengeReview list is empty");
        } else {
            return reviewList;
        }
    }
}
