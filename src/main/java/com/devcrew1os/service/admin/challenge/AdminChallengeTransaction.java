package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.DataResult;
import com.devcrew1os.common.enums.admin.AdminChallengeUpdateType;
import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.admin.challenge.*;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.repository.admin.AdminUserRepository;
import com.devcrew1os.repository.main.challenge.CategoryRepository;
import com.devcrew1os.repository.main.challenge.ChallengeRepository;
import com.devcrew1os.repository.main.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.main.challenge.TodoRepository;
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

    private final AdminUserRepository adminRepo;

    private final CategoryRepository categoryRepo;
    private final ChallengeRepository challengeRepo;
    private final ChallengeStatRepository challengeStatRepo;
    private final TodoRepository todoRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminChallengeTransaction.class);

    /*===========================
       도전과제 목록
    ===========================*/
    @Transactional(readOnly = true)
    public Page<GetAdminChallengeData> getChallengeProcess(Pageable pageable) {
        // 1. fetch data
        Page<Challenge> challengePage = challengeRepo.findAllBy(pageable);
        List<Challenge> challengeList = challengePage.getContent();

        Map<String, String> adminMap = getChallengeUpdateBy(challengeList);
        Map<Integer, List<GetAdminChallengeTodoData>> todoMap = getTodoData(challengeList, adminMap);

        // 2. set data
        List<GetAdminChallengeData> dataList = new ArrayList<>();
        for(Challenge challenge : challengeList){
            int categoryId;
            if(challenge.getCategory() == null){
                categoryId = 0;
            } else {
                categoryId = challenge.getCategory().getId();
            }
            dataList.add(
                    new GetAdminChallengeData(
                            challenge.getId(),
                            categoryId,
                            challenge.getTitle(),
                            challenge.getTerm(),
                            challenge.getDiff(),
                            todoMap.get(challenge.getId()).size(),
                            challenge.getStatus(),
                            challenge.getUpdatedAt(),
                            adminMap.get(challenge.getUpdatedBy()),
                            todoMap.get(challenge.getId())
                    )
            );
        }
        return new PageImpl<>(dataList, pageable, challengePage.getTotalElements());
    }

    private Map<String, String> getChallengeUpdateBy(List<Challenge> challengeList) {
        List<String> challengeUpdateByList = challengeList.stream()
                .map(Challenge::getUpdatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<AdminUser> adminList = adminRepo.findAllByUserIdIn(challengeUpdateByList);

        if (adminList.size() != challengeUpdateByList.size()) {
            Set<String> foundAdminIds = adminList.stream().map(AdminUser::getUserId).collect(Collectors.toSet());
            List<String> missingAdminIds = challengeUpdateByList.stream().filter(id -> !foundAdminIds.contains(id)).collect(Collectors.toList());
            logger.warn("[AdminChallenge] Unidentified adminId detected: {}", missingAdminIds);
        }
        return adminList.stream()
                .collect(Collectors.toMap(AdminUser::getUserId, AdminUser::getName));
    }

    private Map<Integer, List<GetAdminChallengeTodoData>> getTodoData(List<Challenge> challengeList, Map<String, String> updateByMap) {
        // 1. fetch total data
        Set<Integer> challengeIdSet = challengeList.stream()
                .map(Challenge::getId)
                .collect(Collectors.toSet());
        List<Todo> allTodoList = todoRepo.findAllWithChallengeIds(challengeIdSet);
        Map<Integer, List<Todo>> todosGroupByChallenge = allTodoList.stream()
                .collect(Collectors.groupingBy(todo -> todo.getChallenge().getId()));

        // 2. assemble dto
        Map<Integer, List<GetAdminChallengeTodoData>> dataMap = new HashMap<>();
        for(Challenge challenge : challengeList){
            List<Todo> relatedTodos = todosGroupByChallenge.getOrDefault(challenge.getId(), Collections.emptyList());
            List<GetAdminChallengeTodoData> dtoTodoList = relatedTodos.stream()
                    .map(todo -> new GetAdminChallengeTodoData(
                            todo.getId(),
                            todo.getOrder(),
                            todo.getDesc(),
                            updateByMap.get(todo.getUpdatedBy()),
                            todo.getUpdatedAt()
                    )).collect(Collectors.toList());
            if(challenge.getStat() != null){
                if(challenge.getStat().getIncludeTodoCount() != relatedTodos.size()){
                    logger.warn("[AdminChallenge] Total include todo count's are mismatched, at challenge({}) record({}) exists({})", challenge.getId(), challenge.getStat().getIncludeTodoCount(), relatedTodos.size());
                }
            }
            dataMap.put(challenge.getId(), dtoTodoList);
        }
        return dataMap;
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
    public void updateChallengeProcess(String adminId, AdminChallengeUpdateType type, UpdateAdminChallengeReq req) {
        LocalDateTime now = LocalDateTime.now();

        if(type == AdminChallengeUpdateType.CHALLENGE) {
            updateChallenges(adminId, req, now);
        } else if(type == AdminChallengeUpdateType.TODO){
            updateTodos(adminId, req,now);
        } else if(type == AdminChallengeUpdateType.ALL) {
            updateChallenges(adminId, req, now);
            updateTodos(adminId, req,now);
        }
    }

    private void updateChallenges(String adminId, UpdateAdminChallengeReq req, LocalDateTime now) {
        // 1. fetch data
        Set<Integer> challengeIdSet = req.getUpdatedChallenges().stream()
                .map(UpdateAdminChallengeData::getChallengeId)
                .collect(Collectors.toSet());
        Set<Integer> catgoryIdSet = req.getUpdatedChallenges().stream()
                .map(UpdateAdminChallengeData::getCategoryId)
                .collect(Collectors.toSet());
        List<Challenge> challengeList = challengeRepo.findAllByChallengeIdIn(challengeIdSet);
        List<Category> categoryList = categoryRepo.findAllByIdIn(catgoryIdSet);

        // 2. validate data
        if(challengeList.size() != challengeIdSet.size()){
            Set<Integer> foundIds = challengeList.stream().map(Challenge::getId).collect(Collectors.toSet());
            List<Integer> missingIds = challengeIdSet.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("[AdminChallenge] Not all requested challenges were found. Missing IDs: " + missingIds);
        }
        // 3. assemble data
        Map<Integer, UpdateAdminChallengeData> challengeDtoMap = req.getUpdatedChallenges().stream()
                .collect(Collectors.toMap(UpdateAdminChallengeData::getChallengeId, Function.identity()
                ));
        Map<Integer, Category> categoryMap = categoryList.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        // 4. proceed update
        boolean isAnyUpdateProceed = false;
        for(Challenge entity : challengeList){
            UpdateAdminChallengeData dto = challengeDtoMap.get(entity.getId());
            if(applyChallengeUpdate(adminId, entity, dto, categoryMap, now)){
                isAnyUpdateProceed = true;
            }
        }

        if(isAnyUpdateProceed){
            logger.info("[AdminChallenge][{}] Successfully proceed challenge update", adminId);
        } else {
            logger.info("[AdminChallenge][{}] No actual changes applied to challenges.", adminId);
        }
    }

    private boolean applyChallengeUpdate(String adminId,
                                         Challenge entity,
                                         UpdateAdminChallengeData dto,
                                         Map<Integer, Category> categoryMap,
                                         LocalDateTime now) {
        boolean isUpdated = false;
        if(dto.getNewChallengeTitle() != null){
            entity.setTitle(dto.getNewChallengeTitle());
            isUpdated = true;
        }
        if(dto.getNewChallengeTerm() != null) {
            entity.setTerm(dto.getNewChallengeTerm());
            isUpdated = true;
        }
        if(dto.getNewChallengeDiff() != null) {
            entity.setDiff(dto.getNewChallengeDiff());
            isUpdated = true;
        }
        if(dto.getNewChallengeStatus() != null) {
            DataResult res = updateChallengeStatus(entity, dto);
            if(res.isSuccess()){
                entity.setStatus(dto.getNewChallengeStatus());
                updateTodoStatus(adminId, entity.getId(), dto.getNewChallengeStatus(), now);
                isUpdated = true;
            } else {
                throw new RuntimeException("Challenge status update failed: " + res.getDesc() + " at challenge: " + entity.getId());
            }
        }
        if(entity.getStatus() == DataStatus.PREPARE.getValue() &&
                !dto.getCategoryId().equals(entity.getCategory().getId())
        ) {
            Category newCategory = categoryMap.get(dto.getCategoryId());
            entity.setCategory(newCategory);
            isUpdated = true;
        } else if(!dto.getCategoryId().equals(entity.getCategory().getId())){
            throw new RuntimeException("Challenge category update failed, because of challenge status: " + entity.getStatus() + "or same category request: " + entity.getCategory().getId() + " / " + dto.getCategoryId());
        }
        if(isUpdated){
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(adminId);
        }
        return isUpdated;
    }

    private DataResult updateChallengeStatus(Challenge challenge, UpdateAdminChallengeData updated) {
        if(updated.getNewChallengeStatus() == DataStatus.DEPLOYED.getValue()){
            if(challenge.getStatus() != DataStatus.PREPARE.getValue()) {
                return DataResult.NOT_PREPARED;
            }
            // Other elements are 'nullable = false' type
            if(challenge.getCategory() == null ||
                    challenge.getStat() == null) {
                return DataResult.INCOMPLETE_DATA;
            }
            if(!categoryRepo.existsByIdAndStatus(updated.getCategoryId(), DataStatus.DEPLOYED.getValue())) {
                return DataResult.CATEGORY_NOT_DEPLOYED;
            }
            if(challengeRepo.existsByTitleAndStatus(updated.getNewChallengeTitle(), DataStatus.DEPLOYED.getValue())) {
                return DataResult.ALREADY_DEPLOYED;
            }
            return DataResult.SUCCESSFULLY_DEPLOY;

        } else if(updated.getNewChallengeStatus() == DataStatus.PREPARE.getValue()) {
            if(challenge.getStatus() == DataStatus.PREPARE.getValue()){
                return DataResult.ALREADY_NEUTRALIZED;
            }
            return DataResult.SUCCESSFULLY_NEUTRALIZED;

        } else if(updated.getNewChallengeStatus() == DataStatus.DELETE.getValue()) {
            if(challenge.getStatus() != DataStatus.PREPARE.getValue()){
                return DataResult.NOT_PREPARED;
            }
            return DataResult.SUCCESSFULLY_DELETED;

        } else {
            return DataResult.INVALID_STATUS;
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

    // update only title & order
    // status depend on include challenge
    private void updateTodos(String adminId, UpdateAdminChallengeReq req, LocalDateTime now) {
        // 1. prepare data
        Map<Integer, UpdateAdminChallengeTodoData> todoDtoMap = req.getUpdatedChallenges().stream()
                .filter(challenge -> challenge.getNewTodoList() != null)
                .flatMap(challenge -> challenge.getNewTodoList().stream())
                .collect(Collectors.toMap(UpdateAdminChallengeTodoData::getTodoId, Function.identity()
                ));
        if (todoDtoMap.isEmpty()) {
            return;
        }
        Set<Integer> uniqueTodoIds = todoDtoMap.keySet();
        List<Todo> todoList = todoRepo.findAllById(uniqueTodoIds);

        // 2. validate data
        if (todoList.size() != uniqueTodoIds.size()) {
            Set<Integer> foundIds = todoList.stream().map(Todo::getId).collect(Collectors.toSet());
            List<Integer> missingIds = uniqueTodoIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("[AdminChallenge] Todo count mismatch: Requested " + uniqueTodoIds.size() +
                    " unique IDs, but found " + todoList.size() + " entities. Missing IDs: " + missingIds);
        }

        // 4. proceed update
        boolean isAnyUpdateProceed = false;
        for(Todo entity : todoList) {
            UpdateAdminChallengeTodoData dto = todoDtoMap.get(entity.getId());
            if(applyTodoUpdate(adminId, entity, dto, now)){
                isAnyUpdateProceed = true;
            }
        }
        if(isAnyUpdateProceed){
            logger.info("[AdminChallenge][{}] Successfully proceed todo update", adminId);
        } else {
            logger.info("[AdminChallenge][{}] No actual changes applied to todo.", adminId);
        }
    }

    private boolean applyTodoUpdate(String adminId,
                                    Todo entity,
                                    UpdateAdminChallengeTodoData dto,
                                    LocalDateTime now) {
        boolean isUpdated = false;
        if(dto.getNewTodoOrder() != null) {
            entity.setOrder(dto.getNewTodoOrder());
            isUpdated = true;
        }
        if(dto.getNewTodoTitle() != null) {
            entity.setDesc(dto.getNewTodoTitle());
            isUpdated = true;
        }
        if(isUpdated){
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(adminId);
        }
        return isUpdated;
    }

    /*===========================
       도전과제 업로드
    ===========================*/
}
