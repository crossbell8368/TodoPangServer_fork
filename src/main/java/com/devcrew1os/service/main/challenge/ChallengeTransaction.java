package com.devcrew1os.service.main.challenge;

import com.devcrew1os.dto.main.challenge.ChallengeDetailData;
import com.devcrew1os.dto.main.challenge.ChallengeReviewData;
import com.devcrew1os.dto.main.challenge.ChallengeTodoData;
import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import com.devcrew1os.entity.main.challenge.ChallengeStat;
import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.challenge.review.ChallengeReviewStat;
import com.devcrew1os.entity.main.project.ProjectChallenge;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.log.UserProjectActionLog;
import com.devcrew1os.entity.main.project.ProjectTodo;
import com.devcrew1os.repository.main.project.ProjectChallengeRepository;
import com.devcrew1os.repository.main.project.ProjectInfoRepository;
import com.devcrew1os.repository.main.challenge.*;
import com.devcrew1os.repository.main.project.ProjectTodoRepository;
import com.devcrew1os.repository.main.users.UserLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeTransaction {

    private final ChallengeCategoryRepository categoryRepo;
    private final ChallengeInfoRepository challengeInfoRepo;
    private final ChallengeStatRepository challengeStatRepo;
    private final ChallengeTodoRepository challengeTodoRepo;

    private final ProjectInfoRepository projectInfoRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;

    private final ChallengeReviewStatRepository reviewStatRepo;
    private final UserLogRepository userLogRepo;
    /*===========================
       도전과제 목록조회
    ===========================*/

    public List<ChallengeCategory> getCategoryList() {
        List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        } else {
            return categoryList;
        }
    }

    public List<ChallengeInfo> getInfoList() {
        List<ChallengeInfo> infoList = challengeInfoRepo.findAllByOrderByIdDesc();
        if(infoList.isEmpty()){
            throw new RuntimeException("Infos list is empty");
        } else {
            return infoList;
        }
    }

    /*===========================
       도전과제 상세조회
    ===========================*/

    public ChallengeDetailData getChallengeDetailData(int challengeId) {
        // 1. get Info
        ChallengeInfo info = getChallengeInfo(challengeId);

        // 2. get TodoList
        List<ChallengeTodoData> todos = getChallengeTodoList(challengeId).stream()
                .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                .collect(Collectors.toList());

        // 3. get ReviewList
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
        return challengeInfoRepo.findById(challengeId).orElseThrow(
                () -> new RuntimeException("ChallengeInfo not found: " + challengeId)
        );
    }

    public List<ChallengeTodo> getChallengeTodoList(int challengeId) {
        List<ChallengeTodo> todoList = challengeTodoRepo.findAllByChallengeInfoId(challengeId);
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

    /*===========================
       도전과제 등록
    ===========================*/
    public ProjectInfo getProject(String userId) {
        return projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Project not found with " + userId)
        );
    }

    public ChallengeStat getChallengeStat(int challengeId) {
        return challengeStatRepo.findById(challengeId).orElseThrow(
                () -> new RuntimeException("Challenge not found with " + challengeId)
        );
    }

    public void isChallengeExist(int challengeId, List<Integer> todoIds) {

        // 1. 요청 Challenge 객체 확인
        if(!challengeInfoRepo.existsChallengeInfoById(challengeId)) {
            throw new RuntimeException("ChallengeInfo not found: " + challengeId);
        }

        // 2. 요청 ChallengeTodo 객체 확인
        List<Integer> existingTodoIds = challengeTodoRepo.findIdsByChallengeInfoIdAndIdIn(challengeId, todoIds);
        List<Integer> missing = todoIds.stream()
                .filter(id -> !existingTodoIds.contains(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new RuntimeException("TodoInfo not found for challengeId=" + challengeId + ", missingIds=" + missing);
        }
    }

    @Transactional
    public void updateProjects(ChallengeStat challengeStat, ProjectChallenge projectChallenge, List<ProjectTodo> projectTodos) {
        try {
            projectChallengeRepo.save(projectChallenge);
            projectTodoRepo.saveAll(projectTodos);
            challengeStatRepo.save(challengeStat);
        } catch(Exception err) {
            throw new RuntimeException("ProjectData Transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }

    @Transactional
    public void updateProjectLog(List<UserProjectActionLog> logList) {
        try {
            userLogRepo.saveAll(logList);
        } catch(Exception err) {
            throw new RuntimeException("UserProjectLog Transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }
}
