package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.project.ProjectChallengeStatus;
import com.devcrew1os.common.enums.project.ProjectTodoStatus;
import com.devcrew1os.common.enums.project.ProjectLoggingAction;
import com.devcrew1os.dto.main.challenge.*;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.entity.review.ReviewChallenge;
import com.devcrew1os.entity.project.ProjectChallenge;
import com.devcrew1os.entity.project.Project;
import com.devcrew1os.entity.log.ProjectLog;
import com.devcrew1os.entity.project.ProjectTodo;
import com.devcrew1os.repository.challenge.CategoryRepository;
import com.devcrew1os.repository.challenge.ChallengeRepository;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.challenge.TodoRepository;
import com.devcrew1os.repository.project.ProjectChallengeRepository;
import com.devcrew1os.repository.project.ProjectRepository;
import com.devcrew1os.repository.project.ProjectTodoRepository;
import com.devcrew1os.repository.project.ProjectLogRepository;
import com.devcrew1os.repository.review.ReviewChallengeRepository;
import com.devcrew1os.repository.users.UserStatRepository;
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

    private final CategoryRepository categoryRepo;
    private final ChallengeRepository challengeRepo;
    private final ChallengeStatRepository challengeStatRepo;
    private final TodoRepository todoRepo;

    private final ProjectRepository projectRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;
    private final ProjectLogRepository projectLogRepo;

    private final UserStatRepository userStatRepo;
    private final ReviewChallengeRepository reviewStatRepo;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeTransaction.class);
    /*===========================
       도전과제 목록조회
    ===========================*/
    @Transactional(readOnly = true)
    public ChallengeListData getChallengeProcess(String userId) {
        // 1. fetch data: category
        List<Category> categoryList = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
        if(categoryList.isEmpty()){
            throw new RuntimeException("Category list is empty");
        }
        Map<Integer, String> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        // 2. fetch data: challenge
        List<Challenge> challengeList = challengeRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
        if(challengeList.isEmpty()){
            throw new RuntimeException("Challenge list is empty");
        }
        List<ChallengeListInfo> dtoList = challengeList.stream()
                .map(ch -> new ChallengeListInfo(
                        ch.getId(),
                        ch.getCategory().getId(),
                        ch.getTitle(),
                        ch.getDiff(),
                        ch.getStat().getParticipateUserCount()
                )).collect(Collectors.toList());

        // 3. return dto
        return new ChallengeListData(categoryMap, dtoList);
    }

    /*===========================
       도전과제 상세조회
    ===========================*/
    @Transactional(readOnly = true)
    public ChallengeDetailData getChallengeDetailProcess(int challengeId) {
        // 1. fetch data: challenge
        Challenge challenge = challengeRepo.findChallengeByIdWithCategory(challengeId, DataStatus.DEPLOYED.getValue()).orElseThrow(
                () -> new RuntimeException("Challenge not found: " + challengeId)
        );

        // 2. fetch data: todos
        List<Todo> todoList = todoRepo.findAllByChallengeIdAndStatus(challengeId, DataStatus.DEPLOYED.getValue());
        if(todoList.isEmpty()) {
            throw new RuntimeException("TodoList is empty");
        }
        List<ChallengeTodoData> todos = todoList.stream()
                .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                .collect(Collectors.toList());

        // 2. fetch data: reviews
        List<ReviewChallenge> reviewList = reviewStatRepo.findAllByChallengeIdAndStatus(DataStatus.DEPLOYED.getValue(), challengeId);
        if(reviewList.isEmpty()){
            throw new RuntimeException("Review's empty");
        }
        List<ChallengeReviewData> reviews = reviewList.stream()
                .map(review -> new ChallengeReviewData(
                        review.getReview().getId(),
                        review.getReview().getTitle(),
                        review.getUserSelectedCount()))
                .collect(Collectors.toList());

        return new ChallengeDetailData(
                challengeId,
                challenge.getTitle(),
                challenge.getStat().getParticipateUserCount(),
                challenge.getCategory().getId(),
                challenge.getTerm(),
                challenge.getDiff(),
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

        // 1. Fetch data: Project
        Project projectData = projectRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request user project data not found")
        );

        // 2. check data: unregistered challenge & todo
        ChallengeRegisterDTO dto = getUnregisteredChallengeData(userId, projectData.getId(), req);
        if(!dto.isChallengeRegisterNeed() && dto.getUnregisteredTodoIds().isEmpty()){
            return false;
        }

        // 3-1. challenge register process
        if(dto.isChallengeRegisterNeed()){
            if(!projectChallengeRepo.checkProjectChallengeLimit(projectData.getId())){
                return false;
            }
            if(!isExistChallenge(userId, req.getChallengeId(), req.getTodoIds())){
                throw new RuntimeException("Request challenge(Info) data not found at server");
            }
            if(!challengeStatRepo.existsById(req.getChallengeId())){
                throw new RuntimeException("Challenge not found with " + req.getChallengeId());
            }

            ProjectChallenge projectChallenge = ProjectChallenge.builder()
                    .project(projectData)
                    .challenge(Challenge.builder()
                            .id(req.getChallengeId())
                            .build())
                    .status(ProjectChallengeStatus.ONGOING.getValue())
                    .build();

            // update field
            // project | stat(도전중인 사람) | users(등록한 위시)
            projectChallengeRepo.save(projectChallenge);
            challengeStatRepo.updatePopularity(req.getChallengeId());
            userStatRepo.updateRegisterChallenges(userId);
        }

        // 4-2. challenge todo register process
        List<ProjectTodo> projectTodoList = dto.getUnregisteredTodoIds().stream()
                .map(todoId -> ProjectTodo.builder()
                        .project(projectData)
                        .challenge(challengeRepo.getReferenceById(req.getChallengeId()))
                        .todo(todoRepo.getReferenceById(todoId))
                        .status(ProjectTodoStatus.UNCHECK.getValue())
                        .build())
                .collect(Collectors.toList());

        // 5. update date
        projectTodoRepo.saveAll(projectTodoList);

        // 6. record log
        recordUserLog(userId, projectData.getId(), now, req, dto);
        return true;
    }

    private ChallengeRegisterDTO getUnregisteredChallengeData(String userId, Integer projectId, ChallengeRegisterReq req) {

        // 1. check challenge
        if(!projectChallengeRepo.existsByByAllCriteria(
                projectId,
                req.getChallengeId(),
                ProjectChallengeStatus.ONGOING.getValue())
        ) return new ChallengeRegisterDTO(true, new HashSet<>(req.getTodoIds()));

        // 2. check todos
        Set<Integer> requestTodos = new HashSet<>(req.getTodoIds());
        Set<Integer> registeredTodos = projectTodoRepo.findAllByProjectAndTodoIdInWithStatus(projectId, requestTodos, ProjectTodoStatus.COMPLETE.getValue()).stream()
                .map(pt -> pt.getTodo().getId())
                .collect(Collectors.toSet());
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
        if(!challengeRepo.existsChallengeByIdAndStatus(challengeId, DataStatus.DEPLOYED.getValue())) {
            logger.warn("[ChallengeTrans][{}] Request Challenge({}) not found", userId, challengeId);
            return false;
        }

        // 2. 요청 ChallengeTodo 객체 확인
        List<Integer> existingTodoIds = todoRepo.findIdsByChallengeIdAndIdIn(challengeId, todoIds);
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
        List<ProjectLog> logList = new ArrayList<>();

        if(dto.isChallengeRegisterNeed()){
            ProjectLog challengeLog = ProjectLog.builder()
                    .userId(userId)
                    .projectId(projectId)
                    .challengeId(req.getChallengeId())
                    .userActionType(ProjectLoggingAction.ADD_CHALLENGE.getValue())
                    .userActionAt(now)
                    .build();
            logList.add(challengeLog);
        }
        List<ProjectLog> todoLog = dto.getUnregisteredTodoIds().stream()
                .map(todoId -> ProjectLog.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .challengeId(req.getChallengeId())
                        .todoId(todoId)
                        .userActionType(ProjectLoggingAction.ADD_TODO.getValue())
                        .userActionAt(now)
                        .build())
                .collect(Collectors.toList());
        logList.addAll(todoLog);
        projectLogRepo.saveAll(logList);
    }
}
