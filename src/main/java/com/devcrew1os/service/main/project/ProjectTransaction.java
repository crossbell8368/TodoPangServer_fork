package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.ProjectUpdateType;
import com.devcrew1os.common.enums.UserLoggingAction;
import com.devcrew1os.dto.main.project.*;
import com.devcrew1os.entity.log.UserProjectActionLog;
import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.project.ProjectChallenge;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.project.ProjectTodo;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.log.UserActionRepository;
import com.devcrew1os.repository.main.challenge.ChallengeInfoProjection;
import com.devcrew1os.repository.main.challenge.ChallengeInfoRepository;
import com.devcrew1os.repository.main.challenge.ChallengeTodoRepository;
import com.devcrew1os.repository.main.project.ProjectChallengeRepository;
import com.devcrew1os.repository.main.project.ProjectInfoRepository;
import com.devcrew1os.repository.main.project.ProjectTodoRepository;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTransaction {

    private final UserInfoRepository userInfoRepo;
    private final UserActionRepository userLogRepo;

    private final ChallengeInfoRepository challengeInfoRepo;
    private final ChallengeTodoRepository challengeTodoRepo;

    private final ProjectInfoRepository projectInfoRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;

    private static final Logger logger = LoggerFactory.getLogger(ProjectTransaction.class);

    /*===========================
       전체 목표조회
    ===========================*/
    @Transactional(readOnly = true)
    public GetProjectData getProjectProcess(String userId) {
        // 1. fetch ID data
        UserInfo userData = userInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request User not found")
        );
        ProjectInfo projectData = projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request Project not found")
        );
        List<ProjectChallenge> projectChallenges = projectChallengeRepo.findAllByProjectId(projectData.getId());
        List<ProjectTodo> projectTodos = projectTodoRepo.findAllByProjectId(projectData.getId());

        // 2-1. fetch ChallengeInfo for title
        Set<Integer> projectChallengeIdSet = projectChallenges.stream()
                .map(ProjectChallenge::getChallengeInfoId)
                .collect(Collectors.toSet());
        List<ChallengeInfoProjection> challengeInfoList = challengeInfoRepo.findAllByIdIn(projectChallengeIdSet);

        // 2-1. validate ChallengeInfo
        if(challengeInfoList.size() != projectChallengeIdSet.size()) {
            Set<Integer> foundIds = challengeInfoList.stream().map(ChallengeInfoProjection::getId).collect(Collectors.toSet());
            List<Integer> missingIds = projectChallengeIdSet.stream().filter(i -> !foundIds.contains(i)).collect(Collectors.toList());
            throw new RuntimeException("Not all requested project challenges were found. Missing challenges IDs: " + missingIds);
        }

        // 2-2. fetch challengeTodo for data
        Set<Integer> todoIdSet = projectTodos.stream()
                .map(ProjectTodo::getProjectTodoId)
                .collect(Collectors.toSet());
        List<ChallengeTodo> challengeTodoList = challengeTodoRepo.findAllByIdIn(todoIdSet);

        // 2-2. validate challengeTodo
        if(challengeTodoList.size() != todoIdSet.size()) {
            Set<Integer> foundIds = challengeTodoList.stream().map(ChallengeTodo::getId).collect(Collectors.toSet());
            List<Integer> missingIds = todoIdSet.stream().filter(i -> !foundIds.contains(i)).collect(Collectors.toList());
            throw new RuntimeException("Not all requested project todos were found. Missing challenges todo IDs: " + missingIds);
        }

        // 3-1. indexing challengeInfo
        Map<Integer, ChallengeInfoProjection> challengeInfoMap = challengeInfoList.stream()
                .collect(Collectors.toMap(ChallengeInfoProjection::getId, Function.identity()));

        // 3-2. indexing challengeTodo
        Map<Integer, ChallengeTodo> challengeTodoMap = challengeTodoList.stream()
                .collect(Collectors.toMap(ChallengeTodo::getId, Function.identity()));

        // 3-3. grouping projectTodo
        Map<Integer, List<ProjectTodo>> projectTodoMap = projectTodos.stream()
                .collect(Collectors.groupingBy(ProjectTodo::getProjectChallengeId));

        // 4. struct data
        List<GetProjectChallenge> dtoList = new ArrayList<>();
        for(ProjectChallenge pc : projectChallenges) {
            ChallengeInfoProjection challengeData = challengeInfoMap.get(pc.getChallengeInfoId());
            List<ProjectTodo> todoDataList = projectTodoMap.getOrDefault(pc.getChallengeInfoId(), Collections.emptyList());

            List<GetProjectTodo> projectTodoList = new ArrayList<>();
            for(ProjectTodo pt : todoDataList) {
                ChallengeTodo todo = challengeTodoMap.get(pt.getProjectTodoId());
                String title = (todo != null) ? todo.getDesc() : "Unknown Todo";
                projectTodoList.add(
                        new GetProjectTodo(
                                pc.getId(),
                                pt.getId(),
                                title,
                                pt.getProjectTodoStatus()
                        )
                );
            }
            dtoList.add(new GetProjectChallenge(
                    pc.getId(),
                    pc.getChallengeStatus(),
                    challengeData != null ? challengeData.getTitle() : "Unknown Challenge",
                    projectTodoList
            ));
        }
        return new GetProjectData(
                userData.getUserName(),
                dtoList.size(),
                dtoList
        );
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    @Transactional
    public void updateProjectProcess(String userId, UpdateProjectReq req, ProjectUpdateType type) {
        LocalDateTime now = LocalDateTime.now();
        ProjectInfo projectData = projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request user project data not found")
        );

        if(type == ProjectUpdateType.TODO) {
            updateTodos(userId, projectData.getId(),req , now);
        } else if(type == ProjectUpdateType.CHALLENGE) {
            updateChallenges(userId,projectData.getId(),req , now);
        } else {
            return;
        }
    }

    private void updateChallenges(String userId, Integer projectId, UpdateProjectReq req, LocalDateTime now) {
        // 1. fetch data
        Set<Integer> challengeIdSet = req.getChallengeList().stream()
                .map(UpdateProjectChallenge::getChallengeId)
                .collect(Collectors.toSet());
        List<ProjectChallenge> projectChallenges = projectChallengeRepo.findAllByIdIn(challengeIdSet);

        // 2. validate data
        if(projectChallenges.size() != challengeIdSet.size()) {
            Set<Integer> foundIds = projectChallenges.stream().map(ProjectChallenge::getChallengeInfoId).collect(Collectors.toSet());
            List<Integer> missingIds = challengeIdSet.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("Not all requested project challenges were found. Missing challenges IDs: " + missingIds);
        }

        // 3. assemble data
        Map<Integer, UpdateProjectChallenge> challengeUpdatedMap = req.getChallengeList().stream()
                .collect(Collectors.toMap(UpdateProjectChallenge::getChallengeId, Function.identity()
                ));

        // 4. proceed update & logging
        boolean isAnyUpdateProceed = false;
        for(ProjectChallenge entity : projectChallenges) {
            UpdateProjectChallenge updated = challengeUpdatedMap.get(entity.getId());
            if(applyProjectChallengeUpdate(userId, entity, updated)) {
                recordProjectLog(userId, projectId, entity.getChallengeInfoId(), now);
                isAnyUpdateProceed = true;
            }
        }
        if(isAnyUpdateProceed) {
            logger.info("[ProjectTrans][{}] Successfully update project challenge", userId);
        } else {
            logger.error("[ProjectTrans][{}] No actual changes applied to project challenges", userId);
        }
    }

    private boolean applyProjectChallengeUpdate(String userId,
                                                ProjectChallenge entity,
                                                UpdateProjectChallenge updated
    ) {
        boolean isUpdated = false;
        if(updated.getUpdatedStatus() != null){
            entity.setChallengeStatus(updated.getUpdatedStatus());
            isUpdated = true;
        }
        return isUpdated;
    }

    private void recordProjectLog(String userId, Integer projectId, Integer challengeId, LocalDateTime now) {
        UserProjectActionLog log = UserProjectActionLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeInfoId(challengeId)
                .userActionType(UserLoggingAction.COMPLETE_CHALLENGE.getValue())
                .userActionAt(now)
                .build();
        userLogRepo.save(log);
    }

    private void updateTodos(String userId, Integer projectId, UpdateProjectReq req, LocalDateTime now) {
        // 1. fetch data
        Set<Integer> todoIdSet = req.getChallengeList().stream()
                .filter(challenge -> challenge.getTodoList() != null)
                .flatMap(challenge -> challenge.getTodoList().stream())
                .map(UpdateProjectTodo::getTodoId)
                .collect(Collectors.toSet());
        List<ProjectTodo> projectTodos = projectTodoRepo.findAllByProjectIdAndIdIn(projectId, todoIdSet);

        // 2. validate data
        if(projectTodos.size() != todoIdSet.size()) {
            Set<Integer> foundIds = projectTodos.stream().map(ProjectTodo::getId).collect(Collectors.toSet());
            List<Integer> missingIds = todoIdSet.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("Not all requested project todos were found. Missing challenges IDs: " + missingIds);
        }

        // 3. assemble data
        Map<Integer, UpdateProjectTodo> todoUpdatedMap = req.getChallengeList().stream()
                .filter(challenge -> challenge.getTodoList() != null)
                .flatMap(challenge -> challenge.getTodoList().stream())
                .collect(Collectors.toMap(UpdateProjectTodo::getTodoId, Function.identity()
                ));

        // 4. proceed update & logging
        boolean isAnyUpdateProceed = false;
        for(ProjectTodo entity : projectTodos) {
            UpdateProjectTodo updated = todoUpdatedMap.get(entity.getId());
            if(applyProjectTodoUpdate(userId, entity,updated)){
                recordProjectLog(userId, projectId, entity.getProjectChallengeId(), updated, now);
                isAnyUpdateProceed = true;
            }
        }
        if(isAnyUpdateProceed) {
            logger.info("[ProjectTrans][{}] Successfully update project todos", userId);
        } else {
            logger.error("[ProjectTrans][{}] No actual changes applied to project todos", userId);
        }
    }

    private boolean applyProjectTodoUpdate(String userId,
                                           ProjectTodo entity,
                                           UpdateProjectTodo updated
    ) {
        boolean isUpdated = false;
        if(updated.getUpdatedStatus() != null){
            entity.setProjectTodoStatus(updated.getUpdatedStatus());
            isUpdated = true;
        }
        return isUpdated;
    }

    private void recordProjectLog(String userId, Integer projectId, Integer challengeId, UpdateProjectTodo updated, LocalDateTime now) {
        Integer actionType = 0;
        if(updated.getUpdatedStatus() == 1){
            actionType = UserLoggingAction.COMPLETE_TODO.getValue();
        } else if(updated.getUpdatedStatus() == 2){
            actionType = UserLoggingAction.REVERT_TODO.getValue();
        }
        UserProjectActionLog log = UserProjectActionLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeInfoId(challengeId)
                .userActionType(actionType)
                .userActionAt(now)
                .build();
        userLogRepo.save(log);
    }
}
