package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.AdminStatus;
import com.devcrew1os.dto.admin.challenge.*;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.entity.admin.AdminChallenge;
import com.devcrew1os.entity.admin.AdminTodo;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminChallengeRepository;
import com.devcrew1os.repository.admin.AdminTodoRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
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

    private final AdminChallengeRepository challengeRepo;
    private final AdminCategoryRepository categoryRepo;
    private final AdminTodoRepository todoRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminChallengeTransaction.class);

    /*===========================
       도전과제 목록
    ===========================*/
    @Transactional(readOnly = true)
    public Page<GetAdminChallengeData> getChallengeProcess(Pageable pageable) {
        // 1. fetch data
        Page<AdminChallenge> challengePage = challengeRepo.findAllBy(pageable);
        List<AdminChallenge> challengeList = challengePage.getContent();
        if(challengeList.isEmpty()){
            throw new RuntimeException("Admin challenge list not exist");
        }
        Map<String, String> adminMap = getAdminData(challengeList);
        Map<Integer, List<GetAdminChallengeTodoData>> todoMap = getTodoData(challengeList, adminMap);

        // 2. set data
        List<GetAdminChallengeData> dataList = new ArrayList<>();
        for(AdminChallenge challenge : challengeList){
            dataList.add(
                    new GetAdminChallengeData(
                            challenge.getId(),
                            challenge.getCategory().getId(),
                            challenge.getTitle(),
                            challenge.getTerm(),
                            challenge.getDiff(),
                            challenge.getTodoCount(),
                            challenge.getStatus(),
                            challenge.getLastUpdatedAt(),
                            adminMap.get(challenge.getLastUpdatedBy()),
                            todoMap.get(challenge.getId())
                    )
            );
        }
        return new PageImpl<>(dataList, pageable, challengePage.getTotalElements());
    }

    private Map<String, String> getAdminData(List<AdminChallenge> challengeList) {
        List<String> adminIdList = challengeList.stream()
                .map(AdminChallenge::getLastUpdatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<AdminUser> adminList = adminRepo.findAllById(adminIdList);

        if (adminList.size() != adminIdList.size()) {
            Set<String> foundAdminIds = adminList.stream().map(AdminUser::getId).collect(Collectors.toSet());
            List<String> missingAdminIds = adminIdList.stream().filter(id -> !foundAdminIds.contains(id)).collect(Collectors.toList());
            logger.warn("[AdminChallenge] Unidentified adminId detected: {}", missingAdminIds);
        }
        return adminList.stream()
                .collect(Collectors.toMap(AdminUser::getId, AdminUser::getName));
    }

    private Map<Integer, List<GetAdminChallengeTodoData>> getTodoData(List<AdminChallenge> challengeList, Map<String, String> adminMap) {
        // 1. fetch total data
        List<Integer> challengeIdList = challengeList.stream()
                .map(AdminChallenge::getId)
                .collect(Collectors.toList());
        List<AdminTodo> allTodoList = todoRepo.findAllByChallenge_IdIn(challengeIdList);
        Map<Integer, List<AdminTodo>> todosGroupByChallenge = allTodoList.stream()
                .collect(Collectors.groupingBy(todo -> todo.getChallenge().getId()));

        // 2. assemble dto
        Map<Integer, List<GetAdminChallengeTodoData>> dataMap = new HashMap<>();
        for(AdminChallenge challenge : challengeList){
            List<AdminTodo> relatedTodo = todosGroupByChallenge.getOrDefault(challenge.getId(), Collections.emptyList());
            List<GetAdminChallengeTodoData> dtoTodoList = relatedTodo.stream()
                    .map(todo -> new GetAdminChallengeTodoData(
                            todo.getId(),
                            todo.getOrder(),
                            todo.getDesc(),
                            adminMap.get(todo.getLastUpdatedBy()),
                            todo.getLastUpdatedAt()))
                    .collect(Collectors.toList());

            if(challenge.getTodoCount() != dtoTodoList.size()){
                logger.warn("[AdminChallenge] Todo count mismatch for challenge {}: DB count {}, Fetched count {}",
                        challenge.getId(), challenge.getTodoCount(), dtoTodoList.size());
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
        AdminCategory category = categoryRepo.findById(req.getCategoryId()).orElseThrow(
                () -> new RuntimeException("Category not found: " + req.getCategoryId())
        );
        // 2. set challenge
        AdminChallenge challenge = structChallengeEntity(adminId, req, category, now);
        AdminChallenge managedChallenge = challengeRepo.save(challenge);

        // 3. set todoList
        if(!req.getTodoList().isEmpty()) {
            todoRepo.saveAll(
                    structTodoEntity(adminId, managedChallenge, req.getTodoList(), now)
            );
        }

        // 4. update todoList
        category.setChallengesCount(category.getChallengesCount() + 1);
        categoryRepo.save(category);
    }

    private AdminChallenge structChallengeEntity(String adminId, SetAdminChallengeReq req, AdminCategory category, LocalDateTime now) {
        return AdminChallenge.builder()
                .category(category)
                .title(req.getTitle())
                .term(req.getTerm())
                .diff(req.getDiff())
                .todoCount(req.getTodoList().size())
                .status(AdminStatus.PREPARE.getValue())
                .lastUpdatedAt(LocalDateTime.now())
                .lastUpdatedBy(adminId)
                .build();
    }

    private List<AdminTodo> structTodoEntity(String adminId, AdminChallenge challenge, List<SetAdminChallengeTodo> todoList, LocalDateTime now) {
        List<AdminTodo> entityList = new ArrayList<>();
        for(SetAdminChallengeTodo todo : todoList){
            entityList.add(AdminTodo.builder()
                    .challenge(challenge)
                    .order(todo.getOrder())
                    .desc(todo.getTitle())
                    .lastUpdatedAt(now)
                    .lastUpdatedBy(adminId)
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
        LocalDateTime now = LocalDateTime.now();

        // 1. challenge update
        updateChallenges(adminId, req, now);

        // 2. todo update
        updateTodos(adminId, req,now);
    }

    private void updateChallenges(String adminId, UpdateAdminChallengeReq req, LocalDateTime now) {
        // 1. fetch data
        Set<Integer> challengeIdSet = req.getUpdatedChallenges().stream()
                .map(UpdateAdminChallengeData::getChallengeId)
                .collect(Collectors.toSet());
        List<AdminChallenge> challengeList = challengeRepo.findAllByIdIn(challengeIdSet);

        // 2. validate data
        if(challengeList.size() != challengeIdSet.size()){
            Set<Integer> foundIds = challengeList.stream().map(AdminChallenge::getId).collect(Collectors.toSet());
            List<Integer> missingIds = challengeIdSet.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("[AdminChallenge] Not all requested challenges were found. Missing IDs: " + missingIds);
        }
        // 3. assemble data
        Map<Integer, UpdateAdminChallengeData> challengeDtoMap = req.getUpdatedChallenges().stream()
                .collect(Collectors.toMap(UpdateAdminChallengeData::getChallengeId, Function.identity()
                ));
        Map<Integer, AdminCategory> categoryMap = fetchCategory(challengeList, challengeDtoMap);

        // 4. proceed update
        boolean isAnyUpdateProceed = false;
        for(AdminChallenge entity : challengeList){
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

    private Map<Integer, AdminCategory> fetchCategory(List<AdminChallenge> challengeList,
                                                      Map<Integer, UpdateAdminChallengeData> challengeDtoMap) {
        // 1. prepare data
        Set<Integer> categoryIdsForFetch = challengeList.stream()
                .map(entity -> challengeDtoMap.get(entity.getId()))
                .map(UpdateAdminChallengeData::getCategoryId)
                .collect(Collectors.toSet());
        List<AdminCategory> fetchedCategory = categoryRepo.findAllByIdIn(categoryIdsForFetch);

        // 2. validate data
        if (fetchedCategory.size() != categoryIdsForFetch.size()) {
            Set<Integer> foundCategoryIds = fetchedCategory.stream()
                    .map(AdminCategory::getId)
                    .collect(Collectors.toSet());
            categoryIdsForFetch.removeAll(foundCategoryIds);
            throw new RuntimeException("Referenced Categories not found: " + categoryIdsForFetch);
        }

        return fetchedCategory.stream()
                .collect(Collectors.toMap(AdminCategory::getId, Function.identity()));
    }

    private boolean applyChallengeUpdate(String adminId,
                                         AdminChallenge entity,
                                         UpdateAdminChallengeData dto,
                                         Map<Integer, AdminCategory> categoryMap,
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
            entity.setStatus(dto.getNewChallengeStatus());
            isUpdated = true;
        }
        if(!dto.getCategoryId().equals(entity.getCategory().getId())) {
            AdminCategory newCategory = categoryMap.get(dto.getCategoryId());
            entity.setCategory(newCategory);
            isUpdated = true;
        }
        if(isUpdated){
            entity.setLastUpdatedAt(now);
            entity.setLastUpdatedBy(adminId);
        }
        return isUpdated;
    }

    private void updateTodos(String adminId, UpdateAdminChallengeReq req, LocalDateTime now) {
        // 1. prepare data
        Map<Integer, UpdateAdminChallengeTodoData> todoDtoMap = req.getUpdatedChallenges().stream()
                .filter(challenge -> challenge.getNewTodoList() != null)
                .flatMap(challenge -> challenge.getNewTodoList().stream())
                .collect(Collectors.toMap(
                        UpdateAdminChallengeTodoData::getTodoId,
                        Function.identity()
                ));
        if (todoDtoMap.isEmpty()) {
            logger.info("[AdminChallenge] No todo updates requested.");
            return;
        }
        Set<Integer> uniqueTodoIds = todoDtoMap.keySet();
        List<AdminTodo> todoList = todoRepo.findAllById(uniqueTodoIds);

        // 2. validate data
        if (todoList.size() != uniqueTodoIds.size()) {
            Set<Integer> foundIds = todoList.stream().map(AdminTodo::getId).collect(Collectors.toSet());
            List<Integer> missingIds = uniqueTodoIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("[AdminChallenge] Todo count mismatch: Requested " + uniqueTodoIds.size() +
                    " unique IDs, but found " + todoList.size() + " entities. Missing IDs: " + missingIds);
        }

        // 4. proceed update
        boolean isAnyUpdateProceed = false;
        for(AdminTodo entity : todoList) {
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
                                    AdminTodo entity,
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
            entity.setLastUpdatedAt(now);
            entity.setLastUpdatedBy(adminId);
        }
        return isUpdated;
    }
}
