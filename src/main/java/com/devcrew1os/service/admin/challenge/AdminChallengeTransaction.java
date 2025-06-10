package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.PageResponse;
import com.devcrew1os.dto.admin.challenge.*;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.entity.review.Review;
import com.devcrew1os.entity.review.ReviewChallenge;
import com.devcrew1os.repository.challenge.CategoryRepository;
import com.devcrew1os.repository.challenge.ChallengeRepository;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.challenge.TodoRepository;
import com.devcrew1os.repository.projection.AdminChallengeProjection;
import com.devcrew1os.repository.projection.AdminTodoProjection;
import com.devcrew1os.repository.review.ReviewChallengeRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChallengeTransaction {

    private final ReviewRepository reviewRepo;
    private final CategoryRepository categoryRepo;
    private final ChallengeRepository challengeRepo;
    private final ChallengeStatRepository challengeStatRepo;
    private final ReviewChallengeRepository reviewChallengeRepo;
    private final TodoRepository todoRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminChallengeTransaction.class);

    /*===========================
       도전과제 목록
    ===========================*/
    @Transactional(readOnly = true)
    public GetAdminChallengeData getChallengeProcess(Pageable pageable) {
        // 1-1. fetch data: category
        List<Category> categories = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
        Map<Integer, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getTitle));

        // 1-1. fetch data: Challenge
        Page<AdminChallengeProjection> challengePages = challengeRepo.findAllChallengeProjection(pageable);
        List<AdminChallengeProjection> challengeList = challengePages.getContent();

        // 1-2. fetch data: Todo
        Set<Integer> challengeIdSet = challengeList.stream()
                .map(AdminChallengeProjection::getId)
                .collect(Collectors.toSet());
        List<AdminTodoProjection> todoProjections = todoRepo.findAllTodoProjections(challengeIdSet, DataStatus.DELETE.getValue());

        // 2. data grouping
        Map<Integer, List<AdminTodoProjection>> todosMap = todoProjections.stream()
                .collect(Collectors.groupingBy(AdminTodoProjection::getChallengeId));

        // 4. assemble data
        List<GetAdminChallenge> dataList = challengeList.stream().map(ch -> {
            List<AdminTodoProjection> relatedTodoProj = todosMap.getOrDefault(ch.getId(), Collections.emptyList());
            List<GetAdminChallengeTodo> relatedTodoDto = relatedTodoProj.stream()
                    .map(t -> new GetAdminChallengeTodo(
                            t.getId(),
                            t.getTodoOrder(),
                            t.getDesc(),
                            t.getUpdatedBy(),
                            t.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());
            return new GetAdminChallenge(
                    ch.getId(),
                    ch.getCategoryId(),
                    ch.getTitle(),
                    ch.getTerm(),
                    ch.getDiff(),
                    ch.getIncludeTodoCount(),
                    ch.getStatus(),
                    ch.getUpdatedAt(),
                    ch.getUpdatedBy(),
                    relatedTodoDto
            );
        }).collect(Collectors.toList());

        // 5. struct response
        Page<GetAdminChallenge> challengePageDto = new PageImpl<>(dataList, pageable, challengePages.getTotalElements());
        PageResponse<GetAdminChallenge> pageResponse = new PageResponse<>(challengePageDto);
        return new GetAdminChallengeData(pageResponse, categoryMap);
    }

    /*===========================
       도전과제 등록
    ===========================*/
    @Transactional
    public void setChallengeProcess(String adminId, SetAdminChallengeReq req) {
        LocalDateTime now = LocalDateTime.now();

        // 1. get category
        Category category = categoryRepo.findById(req.getCategoryId()).orElseThrow(
                () -> new RuntimeException("Category not found: " + req.getCategoryId())
        );
        // 2. set challenge
        Challenge challenge = structChallenge(adminId, req, category, now);
        Challenge managedChallenge = challengeRepo.save(challenge);

        ChallengeStat stat = structChallengeStat(req, managedChallenge, now);
        challengeStatRepo.save(stat);

        // 3. set review
        List<Review> reviewList = reviewRepo.findAllByStatus(DataStatus.DEPLOYED.getValue());
        List<ReviewChallenge> reviewChallengeList = reviewList.stream()
                .map(rv -> ReviewChallenge.builder()
                        .review(rv)
                        .challenge(managedChallenge)
                        .userSelectedCount(0)
                        .build())
                .collect(Collectors.toList());
        reviewChallengeRepo.saveAll(reviewChallengeList);

        // 3. set todoList
        if(!req.getTodoList().isEmpty()) {
            todoRepo.saveAll(
                    structTodoEntity(adminId, managedChallenge, req.getTodoList(), now)
            );
        }
    }

    private Challenge structChallenge(String adminId, SetAdminChallengeReq req, Category category, LocalDateTime now) {
        return Challenge.builder()
                .category(category)
                .title(req.getTitle())
                .desc(req.getTitle() + " Description")
                .term(req.getTerm())
                .diff(req.getDiff())
                .status(DataStatus.PREPARE.getValue())
                .updatedAt(now)
                .updatedBy(adminId)
                .build();
    }

    private ChallengeStat structChallengeStat(SetAdminChallengeReq req, Challenge challenge, LocalDateTime now) {
        return ChallengeStat.builder()
                .challenge(challenge)
                .includeTodoCount(req.getTodoList().size())
                .participateUserCount(0)
                .completedUserCount(0)
                .registeredReviewCount(0)
                .build();
    }

    private List<Todo> structTodoEntity(String adminId, Challenge challenge, List<SetAdminChallengeTodo> todoList, LocalDateTime now) {
        List<Todo> entityList = new ArrayList<>();
        for(SetAdminChallengeTodo todo : todoList){
            entityList.add(Todo.builder()
                    .challenge(challenge)
                    .order(todo.getOrder())
                    .desc(todo.getTitle())
                    .status(DataStatus.PREPARE.getValue())
                    .updatedAt(now)
                    .updatedBy(adminId)
                    .build()
            );
        }
        return entityList;
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    @Transactional
    public void updateChallengeProcess(String adminId, UpdateAdminChallengeReq req) {

        Challenge entity = challengeRepo.findChallengeByIdWithStat(req.getChallengeId()) // Fetch Join 포함된 메소드 가정
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + req.getChallengeId()));
        LocalDateTime now = LocalDateTime.now();

        boolean isChallengeUpdated = updateChallenge(adminId, req, entity, now);
        boolean isTodosUpdated = updateChallengeTodo(adminId, req, entity, now);

        if(isChallengeUpdated || isTodosUpdated){
            entity.setUpdatedBy(adminId);
            entity.setUpdatedAt(now);
        } else {
            logger.info("[AdminChallenge][{}] No actual changes applied to challenge [ID: {}]", adminId, entity.getId());
        }
    }

    private boolean updateChallenge(String adminId, UpdateAdminChallengeReq req, Challenge entity, LocalDateTime now) {
        boolean isUpdated = false;

        if(entity.getStatus() != DataStatus.PREPARE.getValue()){
            throw new RuntimeException("Challenge status is not PREPARE");
        }
        if (req.getNewCategoryId() != null) {
            Category category = categoryRepo.findByIdWithStatus(req.getNewCategoryId(), DataStatus.DELETE.getValue())
                    .orElseThrow(() -> new RuntimeException("Deployable category not found or invalid: " + req.getNewCategoryId()));
            entity.setCategory(category);
            isUpdated = true;
        }
        if(req.getNewChallengeTitle() != null){
            entity.setTitle(req.getNewChallengeTitle());
            isUpdated = true;
        }
        if(req.getNewChallengeTerm() != null){
            entity.setTerm(req.getNewChallengeTerm());
            isUpdated = true;
        }
        if(req.getNewChallengeDiff() != null){
            entity.setDiff(req.getNewChallengeDiff());
            isUpdated = true;
        }
        if(isUpdated){
        } else {
            logger.warn("Challenge not updated: " + req.getChallengeId());
        }
        return isUpdated;
    }

    private boolean updateChallengeTodo(String adminId, UpdateAdminChallengeReq req, Challenge entity, LocalDateTime now) {
        boolean isUpdated = false;

        // check add
        if (req.getTodosToAdd() != null && !req.getTodosToAdd().isEmpty()) {
            List<Todo> newTodoList = req.getTodosToAdd().stream()
                    .map(todo -> Todo.builder()
                            .challenge(entity)
                            .order(todo.getNewTodoOrder())
                            .desc(todo.getNewTodoTitle())
                            .status(entity.getStatus())
                            .updatedAt(now)
                            .updatedBy(adminId)
                            .build()
                    ).collect(Collectors.toList());
            todoRepo.saveAll(newTodoList);

            // 1-2. update challenge stat
            challengeStatRepo.addTotalRegisteredTodos(entity.getId(), newTodoList.size());
            isUpdated = true;
        }

        // check update & delete Ids
        Set<Integer> updateIds = req.getTodosToUpdate() != null ? req.getTodosToUpdate().stream().map(UpdateAdminChallengeTodo::getTodoId).collect(Collectors.toSet()) : Collections.emptySet();
        Set<Integer> deleteIds = req.getTodosToDelete() != null ? req.getTodosToDelete().stream().map(UpdateAdminChallengeTodo::getTodoId).collect(Collectors.toSet()) : Collections.emptySet();

        // update
        if(req.getTodosToUpdate() != null){
            // prepare update data
            List<Todo> updateTodoList = todoRepo.findAllWithTodoIds(updateIds);
            if(updateIds.size() != updateTodoList.size()){
                throw new RuntimeException("Some todos for update not found.");
            }
            Map<Integer, Todo> updateTodoMap = updateTodoList.stream().collect(Collectors.toMap(Todo::getId, Function.identity()));
            for(UpdateAdminChallengeTodo dto : req.getTodosToUpdate()) {
                Todo target = updateTodoMap.get(dto.getTodoId());
                if(dto.getNewTodoOrder() != null){
                    target.setOrder(dto.getNewTodoOrder());
                    isUpdated = true;
                }
                if(dto.getNewTodoTitle() != null){
                    target.setDesc(dto.getNewTodoTitle());
                    isUpdated = true;
                }
            }
        }

        // delete
        if(req.getTodosToDelete() != null) {
            // prepare update data
            List<Todo> deleteTodoList = todoRepo.findAllWithTodoIds(deleteIds);
            if(deleteIds.size() != deleteTodoList.size()){
                throw new RuntimeException("Some todos for delete not found.");
            }
            Map<Integer, Todo> deleteTodoMap = deleteTodoList.stream().collect(Collectors.toMap(Todo::getId, Function.identity()));
            for(UpdateAdminChallengeTodo dto : req.getTodosToDelete()) {
                Todo target = deleteTodoMap.get(dto.getTodoId());
                target.setStatus(DataStatus.DELETE.getValue());
            }
            challengeStatRepo.removeTotalRegisteredTodos(entity.getId(), deleteIds.size());
            isUpdated = true;
        }
        return isUpdated;
    }

    /*===========================
       도전과제 배포
    ===========================*/
    @Transactional
    public void deployChallengeProcess(String adminId, DeployAdminChallengeReq req, DeployAdminChallengeRes res) {
        // 1. fetch data: challenges
        Set<Integer> targetChallengeIdSet = req.getData().stream()
                .map(DeployAdminChallengeData::getChallengeId)
                .collect(Collectors.toSet());
        List<Challenge> targetChallengeList = challengeRepo.findAllChallengesWithIds(targetChallengeIdSet);

        if(targetChallengeIdSet.size() != targetChallengeList.size()){
            throw new RuntimeException("Some challenges for deployment not found.");
        }

        // 2. assemble data
        Map<Integer, Challenge> targetChallengeMap = targetChallengeList.stream()
                .collect(Collectors.toMap(Challenge::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();

        // 3. update status
        for(DeployAdminChallengeData dto : req.getData()) {
            Challenge target = targetChallengeMap.get(dto.getChallengeId());
            if(target == null){
                logger.error("Target challenge({}) for deployment not found", dto.getChallengeId());
                throw new RuntimeException("Target challenge(" + dto.getChallengeId() + ") for deployment not found");
            }
            int currentStatus = target.getStatus();
            int newStatus = dto.getNewStatus();
            boolean isUpdated = false;
            if(currentStatus == newStatus) continue;

            switch(DataStatus.fromValue(newStatus)) {
                case DEPLOYED:
                    if(currentStatus == DataStatus.PREPARE.getValue()){
                        if(!challengeRepo.existsChallengeByTitleAndStatus(target.getTitle(), DataStatus.DEPLOYED.getValue()) && target.getStat().getIncludeTodoCount() != 0){
                            target.setStatus(newStatus);
                            updateTodoStatus(adminId, target.getId(), newStatus, now);
                            res.addMessage("Challenge(" + dto.getChallengeId() + ") turned into DEPLOYED");
                            isUpdated = true;
                        } else {
                            res.addMessage("Challenge(" + target.getId() + ") title already DEPLOYED");
                            logger.warn("Target challenge({}) for deployment already exist: {}", dto.getChallengeId(), target.getTitle());
                        }
                    } else {
                        res.addMessage("Challenge(" + target.getId() + ") cannot be DEPLOYED from status " + DataStatus.fromValue(currentStatus).name());
                        logger.warn("Target challenge({}) for deployment cannot be DEPLOYED from status {}", target.getId(), DataStatus.fromValue(currentStatus).name());
                    }
                    break;

                case PREPARE:
                    if(currentStatus == DataStatus.DEPLOYED.getValue() || currentStatus == DataStatus.DELETE.getValue()){
                        target.setStatus(newStatus);
                        updateTodoStatus(adminId, target.getId(), newStatus, now);
                        res.addMessage("Challenge(" + dto.getChallengeId() + ") turned into PREPARE");
                        isUpdated = true;
                    } else {
                        res.addMessage("Challenge(" + target.getId() + ") title already PREPARE");
                    }
                    break;

                case DELETE:
                    if(currentStatus == DataStatus.PREPARE.getValue()){
                        target.setStatus(newStatus);
                        updateTodoStatus(adminId, target.getId(), newStatus, now);
                        res.addMessage("Challenge(" + dto.getChallengeId() + ") turned into DELETED");
                        isUpdated = true;
                    } else {
                        res.addMessage("Challenge(" + target.getId() + ") cannot be DELETED from status " + DataStatus.fromValue(currentStatus).name());
                        logger.warn("Target challenge({}) for deployment cannot be DELETED from status {}", target.getId(), DataStatus.fromValue(currentStatus).name());
                    }
                    break;
            }
            if(isUpdated){
                target.setUpdatedBy(adminId);
                target.setUpdatedAt(now);
            }
        }
    }

    private void updateTodoStatus(String adminId, int challengeId, Integer updated, LocalDateTime now){
        List<Todo> todoList = todoRepo.findAllByChallengeId(challengeId);
        for(Todo entity : todoList){
            entity.setStatus(updated);
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(adminId);
        }
        todoRepo.saveAll(todoList);
    }
}
