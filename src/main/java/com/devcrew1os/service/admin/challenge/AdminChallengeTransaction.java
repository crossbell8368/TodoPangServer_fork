package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.AdminStatus;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeData;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeReq;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeTodo;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.entity.admin.AdminChallenge;
import com.devcrew1os.entity.admin.AdminTodo;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminChallengeRepository;
import com.devcrew1os.repository.admin.AdminTodoRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<GetAdminChallengeData> getChallengeProcess() {
        // 1. fetch data
        List<AdminChallenge> challengeList = challengeRepo.findAllByOrderByIdDesc();
        if(challengeList.isEmpty()){
            throw new RuntimeException("Admin challenge list not exist");
        }
        Map<String, String> adminMap = getAdminData(challengeList);

        // 2. set data
        List<GetAdminChallengeData> dataList = new ArrayList<>();
        for(AdminChallenge challenge : challengeList){
            dataList.add(
                    new GetAdminChallengeData(
                            challenge.getId(),
                            challenge.getTitle(),
                            challenge.getTerm(),
                            challenge.getDiff(),
                            challenge.getTodoCount(),
                            challenge.getStatus(),
                            challenge.getLastUpdatedAt(),
                            adminMap.get(challenge.getLastUpdatedBy())
                    )
            );
        }
        return dataList;
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
}
