package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.AdminStatus;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeReq;
import com.devcrew1os.dto.admin.challenge.SetAdminChallengeTodo;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.entity.admin.AdminChallenge;
import com.devcrew1os.entity.admin.AdminTodo;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminChallengeRepository;
import com.devcrew1os.repository.admin.AdminTodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminChallengeTransaction {

    private final AdminChallengeRepository challengeRepo;
    private final AdminCategoryRepository categoryRepo;
    private final AdminTodoRepository todoRepo;

    /*===========================
       도전과제 목록
    ===========================*/
    public List<AdminChallenge> getAdminChallengeList() {
        List<AdminChallenge> challengeList = challengeRepo.findAllByOrderByIdDesc();
        if(challengeList.isEmpty()){
            throw new RuntimeException("Admin challenge list is empty");
        } else {
            return challengeList;
        }
    }

    /*===========================
       도전과제 등록
    ===========================*/
    @Transactional
    public void setAdminChallengeProcess(String adminId, SetAdminChallengeReq req) {
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
