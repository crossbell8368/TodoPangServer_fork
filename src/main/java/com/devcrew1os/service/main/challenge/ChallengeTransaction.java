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
import com.devcrew1os.repository.projection.ChallengeCardProjection;
import com.devcrew1os.repository.projection.ChallengeDetailProjection;
import com.devcrew1os.repository.review.ReviewChallengeRepository;
import com.devcrew1os.repository.users.UserStatRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    private final ObjectMapper objectMapper;
    private final ChallengeCacheManager cacheManager;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Integer> integerRedisTemplate;

    @Value("${devcrew1os.redis.key.challenge.category}")
    private String categoryKey;
    @Value("${devcrew1os.redis.key.challenge.data.id}")
    private String challengeIdKey;
    @Value("${devcrew1os.redis.key.challenge.data}")
    private String challengeCardKey;
    @Value("${devcrew1os.redis.key.challenge.detail}")
    private String challengeDetailKey;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeTransaction.class);

    /*===========================
       도전과제 목록조회
    ===========================*/
    @Transactional(readOnly = true)
    public ChallengeListData getChallengeProcess(String userId) {

        // 1. fetch category
        Map<Integer, String> categoryMap = getCategoryFromCache(userId);
        if(categoryMap.isEmpty()){
            categoryMap = getCategoryFromDB(userId);
            logger.info("[ChallengeTrans][{}] Fetch category data from DB (Cache miss)", userId);
        }
        // 2. fetch challenge
        List<ChallengeListInfo> challengeDtoList = getChallengeCardFromCache(userId);
        if(challengeDtoList.isEmpty()){
            challengeDtoList = getChallengeCardFromDB(userId);
            logger.info("[ChallengeTrans][{}] Fetch challenge card from DB (Cache miss)", userId);
        }
        // 3. return dto
        return new ChallengeListData(categoryMap, challengeDtoList);
    }

    /*===========================
       도전과제 상세조회
    ===========================*/
    @Transactional(readOnly = true)
    public ChallengeDetailData getChallengeDetailProcess(String userId, int challengeId) {

        // 1. fetch challenge detail
        ChallengeDetailData challengeDetailData = getChallengeDetailFromCache(userId, challengeId);
        if(challengeDetailData == null){
            challengeDetailData = getChallengeDetailFromDB(userId, challengeId);
            logger.info("[ChallengeTrans][{}] Fetch challenge detail from DB (Cache miss)", userId);
        }
        // 2. fetch review
        List<ChallengeReviewData> challengeReviewList = new ArrayList<>();
        try {
            Pageable top4 = PageRequest.of(0, 4);
            List<ReviewChallenge> reviewList = reviewStatRepo.findAllByChallengeIdAndStatus(challengeId, DataStatus.DEPLOYED.getValue(), top4);
            if(reviewList.isEmpty()) {
                reviewList = Collections.emptyList();
            }
            challengeReviewList = reviewList.stream()
                    .map(rev -> new ChallengeReviewData(
                            rev.getReview().getId(),
                            rev.getReview().getTitle(),
                            rev.getUserSelectedCount()
                    )).collect(Collectors.toList());
        } catch(Exception err) {
            logger.warn("[ChallengeTrans][{}] Error detected while fetch review data from db", userId);
        }
        challengeDetailData.setReviewList(challengeReviewList);

        // 2. return dto
        return challengeDetailData;
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

            // build entity
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
        Challenge challengeRef = challengeRepo.getReferenceById(req.getChallengeId());
        List<ProjectTodo> projectTodoList = dto.getUnregisteredTodoIds().stream()
                .map(todoId -> ProjectTodo.builder()
                        .project(projectData)
                        .challenge(challengeRef)
                        .todo(todoRepo.getReferenceById(todoId))
                        .status(ProjectTodoStatus.UNCHECK.getValue())
                        .build())
                .collect(Collectors.toList());

        // 5. update date
        projectTodoRepo.saveAll(projectTodoList);

        // 6. record log
        recordUserLog(userId, projectData.getId(), now, req);
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
            return new ChallengeRegisterDTO(false, Collections.emptySet());
        } else {
            return new ChallengeRegisterDTO(true, unregisteredTodos);
        }
    }

    public boolean isExistChallenge(String userId, int challengeId, List<Integer> todoIds) {

        // 1. 요청 Challenge 객체 확인
        if(!challengeRepo.existsChallengeByIdAndStatus(challengeId, DataStatus.DEPLOYED.getValue())) {
            logger.warn("[ChallengeTrans][{}] Request Challenge({}) not found", userId, challengeId);
            return false;
        }

        // 2. 요청 ChallengeTodo 객체 확인
        List<Integer> existingTodoIds = todoRepo.findAllIdsByChallengeIdAndTodoIds(challengeId, todoIds);
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
                               ChallengeRegisterReq req
    ) {
        ProjectLog challengeLog = ProjectLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeId(req.getChallengeId())
                .userActionType(ProjectLoggingAction.ADD_CHALLENGE.getValue())
                .userActionAt(now)
                .build();
        projectLogRepo.save(challengeLog);
    }

    /*===========================
       유틸리티: category
    ===========================*/
    private Map<Integer, String> getCategoryFromCache(String userId) {
        try {
            // 1. get data from cache
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            String jsonCategory = valueOps.get(categoryKey);
            if (jsonCategory == null || jsonCategory.trim().isEmpty()) {
                logger.warn("[ChallengeTrans][{}] Category cache is empty or not found for key: {}", userId, categoryKey);
                return Collections.emptyMap();
            }
            // 2, convert json to dto
            return objectMapper.readValue(jsonCategory, new TypeReference<Map<Integer, String>>() {});
        } catch (JsonProcessingException err) {
            logger.error("[ChallengeTrans][{}] Failed to parse JSON category data from cache.", userId, err);
            return Collections.emptyMap();
        } catch (Exception err) {
            logger.error("[ChallengeTrans][{}] An unexpected error occurred while getting category from cache.", userId, err);
            return Collections.emptyMap();
        }
    }

    private Map<Integer, String> getCategoryFromDB(String userId) {
        try {
            // 1. fetch category from db
            List<Category> categoryList = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
            Map<Integer, String> categoryMap = new HashMap<>();

            // 2. struct dto & update cache
            if(categoryList != null && !categoryList.isEmpty()) {
                categoryMap = categoryList.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getTitle));
            }
            cacheManager.refreshCategory(categoryMap);
            return categoryMap;

        } catch(Exception err) {
            logger.error("[ChallengeCache][{}] Failed to fetch category from db", userId, err);
            return Collections.emptyMap();
        }
    }

    /*===========================
       유틸리티: ChallengeCard
    ===========================*/
    private List<ChallengeListInfo> getChallengeCardFromCache(String userId) {
        try {
            // 1. get challengeIds from cache
            ListOperations<String, Integer> listOps = integerRedisTemplate.opsForList();
            List<Integer> challengeIds = listOps.range(challengeIdKey, 0, -1);
            if(challengeIds == null || challengeIds.isEmpty()) {
                logger.warn("[ChallengeTrans][{}] ChallengeIds cache is empty or not found for key", userId);
                return Collections.emptyList();
            }
            // 2. struct key list
            List<String> cardKey = challengeIds.stream()
                    .map(id -> challengeCardKey + id)
                    .collect(Collectors.toList());

            // 3. get jsonChallengeCard from cache
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            List<String> jsonChallengeCardList = valueOps.multiGet(cardKey);
            if(jsonChallengeCardList == null || jsonChallengeCardList.isEmpty()) {
                logger.warn("[ChallengeTrans][{}] jsonChallengeCard list is empty or not found for key", userId);
                return Collections.emptyList();
            }

            // 4. convert json to dto
            List<ChallengeListInfo> resultList = jsonChallengeCardList.stream()
                    .filter(Objects::nonNull)
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, ChallengeListInfo.class);
                        } catch (JsonProcessingException err) {
                            logger.error("[ChallengeTrans][{}] Failed to parse JSON challenge card from cache: {}", userId, json, err);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return resultList;

        }  catch (Exception err) {
            logger.error("[ChallengeTrans][{}] An unexpected error occurred while getting challengeCard from cache.", userId, err);
            return Collections.emptyList();
        }
    }

    private List<ChallengeListInfo> getChallengeCardFromDB(String userId) {
        try {
            // 1. fetch challenge from db
            List<ChallengeCardProjection> challengeList = challengeRepo.findAllCardProjByStatus(DataStatus.DEPLOYED.getValue());
            List<ChallengeListInfo> dtoList = new ArrayList<>();

            // 2. struct dto & update cache
            if(challengeList != null && !challengeList.isEmpty()) {
                dtoList = challengeList.stream()
                        .map(ch -> new ChallengeListInfo(
                                ch.getId(),
                                ch.getCategoryId(),
                                ch.getTitle(),
                                ch.getDiff(),
                                ch.getPopularity()
                        )).collect(Collectors.toList());
            }
            cacheManager.refreshChallengeCard(dtoList);
            return dtoList;

        } catch(Exception err) {
            logger.error("[ChallengeTrans][{}] Failed to fetch ChallengeCard from db", userId, err);
            return Collections.emptyList();
        }
    }

    /*===========================
       유틸리티: ChallengeDetail
    ===========================*/
    private ChallengeDetailData getChallengeDetailFromCache(String userId, Integer ChallengeId) {
        try {
            // 1. get jsonChallengeDetail from cache
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            String jsonChallengeDetail = valueOps.get(challengeDetailKey + ChallengeId);
            if(jsonChallengeDetail == null || jsonChallengeDetail.trim().isEmpty()) {
                logger.warn("[ChallengeTrans][{}] Failed to retrieve jsonChallengeDetail from cache", userId);
                return null;
            }
            // 2. convert json to dto
            return objectMapper.readValue(jsonChallengeDetail, ChallengeDetailData.class);

        } catch(JsonProcessingException err) {
            logger.error("[ChallengeCache][{}] Failed to parse JSON challenge detail from cache", userId, err);
            return null;
        } catch(Exception err) {
            logger.error("[ChallengeTrans][{}] An unexpected error occurred while getting challengeDetail from cache.", userId, err);
            return null;
        }
    }


    private ChallengeDetailData getChallengeDetailFromDB(String userId, Integer challengeId) {
        try {
            // 1. fetch data from db
            // challenges
            ChallengeDetailProjection challengeProj = challengeRepo.findAllDetailProjByStatus(challengeId, DataStatus.DEPLOYED.getValue());

            // todos
            List<Todo> todoList = todoRepo.findAllByChallengeIdAndStatus(challengeId, DataStatus.DEPLOYED.getValue());
            List<ChallengeTodoData> todos = new ArrayList<>();
            if(!todoList.isEmpty()) {
                todos = todoList.stream()
                        .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                        .collect(Collectors.toList());
            }
            // reviews
            Pageable top4 = PageRequest.of(0, 4);
            List<ReviewChallenge> reviewList = reviewStatRepo.findAllByChallengeIdAndStatus(DataStatus.DEPLOYED.getValue(), challengeId, top4);
            if(reviewList.isEmpty()){
                throw new RuntimeException("Review's empty");
            }

            // 2. struct dto & update cache
            ChallengeDetailData dto = new ChallengeDetailData(
                    challengeProj.getId(),
                    challengeProj.getTitle(),
                    challengeProj.getPopularity(),
                    challengeProj.getCategoryId(),
                    challengeProj.getTerm(),
                    challengeProj.getDiff(),
                    todos,
                    new ArrayList<>()
            );
            cacheManager.refreshChallengeDetails(dto);

            // 3. complete dto & return
            List<ChallengeReviewData> reviews = reviewList.stream()
                    .map(review -> new ChallengeReviewData(
                            review.getReview().getId(),
                            review.getReview().getTitle(),
                            review.getUserSelectedCount()))
                    .collect(Collectors.toList());
            dto.setReviewList(reviews);
            return dto;

        } catch(Exception err) {
            logger.error("[ChallengeTrans][{}] Failed to fetch ChallengeDetail from db", userId, err);
            return null;
        }
    }
}
