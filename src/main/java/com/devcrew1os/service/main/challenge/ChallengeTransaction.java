package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.ProjectChallengeStatus;
import com.devcrew1os.common.enums.ProjectTodoStatus;
import com.devcrew1os.common.enums.UserLoggingAction;
import com.devcrew1os.dto.main.challenge.*;
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
import com.devcrew1os.repository.log.UserActionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private final UserActionRepository userLogRepo;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeTransaction.class);
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
    @Transactional(readOnly = true)
    public ChallengeDetailData getChallengeDetailData(int challengeId) {
        // 1. get Info
        ChallengeInfo info = challengeInfoRepo.findById(challengeId).orElseThrow(
                () -> new RuntimeException("ChallengeInfo not found: " + challengeId)
        );

        // 2. get TodoList
        List<ChallengeTodo> todoList = challengeTodoRepo.findAllByChallengeInfoId(challengeId);
        if(todoList.isEmpty()) {
            throw new RuntimeException("Challenge Todo List is empty");
        }
        List<ChallengeTodoData> todos = todoList.stream()
                .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                .collect(Collectors.toList());

        // 3. get ReviewList
        List<ChallengeReviewStat> reviewList = reviewStatRepo.findAllByInfoId(challengeId);
        if(reviewList.isEmpty()){
            throw new RuntimeException("ChallengeReview list is empty");
        }
        List<ChallengeReviewData> reviews = reviewList.stream()
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

    /*===========================
       도전과제 등록
    ===========================*/
    @Transactional
    public boolean registerChallenge(String userId, ChallengeRegisterReq req) {

        LocalDateTime now = LocalDateTime.now();
        ProjectInfo projectData = projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request user project data not found")
        );

        // 1. Check challenge & todo
        ChallengeRegisterDTO dto = getUnregisteredChallengeData(userId, projectData.getId(), req);
        if(!dto.isChallengeRegisterNeed() && dto.getUnregisteredTodoIds().isEmpty()){
            return false;
        }
        // 2-1. challenge register process
        if(dto.isChallengeRegisterNeed()){
            if(!isExistChallenge(userId, req.getChallengeId(), req.getTodoIds())){
                throw new RuntimeException("Request challenge(Info) data not found at server");
            }
            if(!challengeStatRepo.existsById(req.getChallengeId())){
                throw new RuntimeException("Challenge not found with " + req.getChallengeId());
            }

            ProjectChallenge projectChallenge = ProjectChallenge.builder()
                    .projectId(projectData.getId())
                    .challengeInfoId(req.getChallengeId())
                    .challengeStatus(ProjectChallengeStatus.ONGOING.getValue())
                    .build();

            projectChallengeRepo.save(projectChallenge);
            challengeStatRepo.updatePopularity(req.getChallengeId());
        }

        // 2-2. challenge todo register process
        List<ProjectTodo> projectTodoList = dto.getUnregisteredTodoIds().stream()
                .map(todoId -> ProjectTodo.builder()
                        .projectId(projectData.getId())
                        .projectChallengeId(req.getChallengeId())
                        .projectTodoId(todoId)
                        .projectTodoStatus(ProjectTodoStatus.UNCHECK.getValue())
                        .build())
                .collect(Collectors.toList());

        // 3. update date
        projectTodoRepo.saveAll(projectTodoList);

        // 4. record log
        recordUserLog(userId, projectData.getId(), now, req, dto);
        return true;
    }

    private ChallengeRegisterDTO getUnregisteredChallengeData(String userId, Integer projectId, ChallengeRegisterReq req) {

        // check challenge
        if(!projectChallengeRepo.existsByProjectIdAndChallengeInfoId(projectId, req.getChallengeId())){
            return new ChallengeRegisterDTO(true, new HashSet<>(req.getTodoIds()));
        }
        // check todos
        Set<Integer> requestTodos = new HashSet<>(req.getTodoIds());
        Set<Integer> registeredTodos = projectTodoRepo.findAllByProjectIdAndProjectTodoIdIn(projectId, requestTodos).stream()
                .map(ProjectTodo::getProjectTodoId).collect(Collectors.toSet());
        Set<Integer> unregisteredTodos = requestTodos.stream()
                .filter(reqId -> !registeredTodos.contains(reqId))
                .collect(Collectors.toSet());

        if(unregisteredTodos.isEmpty()){
            logger.info("[ChallengeTrans][{}] Request Challenge({}) and all requested Todos({}) exist at project", userId, req.getChallengeId(), requestTodos);
        } else {
            logger.info("[ChallengeTrans][{}] ProjectChallenge {} exists. New todos to add: {}. Existing: {}. Requested: {}",
                    userId, req.getChallengeId(), unregisteredTodos, registeredTodos, requestTodos);
        }
        return new ChallengeRegisterDTO(false, unregisteredTodos);
    }

    public boolean isExistChallenge(String userId, int challengeId, List<Integer> todoIds) {

        // 1. 요청 Challenge 객체 확인
        if(!challengeInfoRepo.existsChallengeInfoById(challengeId)) {
            logger.warn("[ChallengeTrans][{}] Request Challenge({}) not found", userId, challengeId);
            return false;
        }

        // 2. 요청 ChallengeTodo 객체 확인
        List<Integer> existingTodoIds = challengeTodoRepo.findIdsByChallengeInfoIdAndIdIn(challengeId, todoIds);
        List<Integer> missing = todoIds.stream()
                .filter(id -> !existingTodoIds.contains(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            logger.warn("[ChallengeTrans][{}] Request Challenge({}) Todos not found: {}", userId, challengeId, missing);
            return false;
        } else {
            return true;
        }
    }

    private void recordUserLog(String userId,
                               Integer projectId,
                               LocalDateTime now,
                               ChallengeRegisterReq req,
                               ChallengeRegisterDTO dto
    ) {
        List<UserProjectActionLog> logList = new ArrayList<>();

        if(dto.isChallengeRegisterNeed()){
            UserProjectActionLog challengeLog = UserProjectActionLog.builder()
                    .userId(userId)
                    .projectId(projectId)
                    .challengeInfoId(req.getChallengeId())
                    .userActionType(UserLoggingAction.ADD_CHALLENGE.getValue())
                    .userActionAt(now)
                    .build();
            logList.add(challengeLog);
        }
        List<UserProjectActionLog> todoLog = dto.getUnregisteredTodoIds().stream()
                .map(todoId -> UserProjectActionLog.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .challengeInfoId(req.getChallengeId())
                        .challengeTodoId(todoId)
                        .userActionType(UserLoggingAction.ADD_TODO.getValue())
                        .userActionAt(now)
                        .build())
                .collect(Collectors.toList());
        logList.addAll(todoLog);
        userLogRepo.saveAll(logList);
    }
}
