package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.project.ProjectChallengeStatus;
import com.devcrew1os.common.enums.project.ProjectTodoStatus;
import com.devcrew1os.common.enums.project.ProjectUpdateType;
import com.devcrew1os.common.enums.project.ProjectLoggingAction;
import com.devcrew1os.dto.main.project.*;
import com.devcrew1os.entity.log.ProjectLog;
import com.devcrew1os.entity.project.ProjectChallenge;
import com.devcrew1os.entity.project.Project;
import com.devcrew1os.entity.project.ProjectTodo;
import com.devcrew1os.entity.user.UserStat;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.repository.main.project.ProjectLogRepository;
import com.devcrew1os.repository.main.project.ProjectChallengeRepository;
import com.devcrew1os.repository.main.project.ProjectRepository;
import com.devcrew1os.repository.main.project.ProjectTodoRepository;
import com.devcrew1os.repository.main.projection.ProjectChallengeProjection;
import com.devcrew1os.repository.main.projection.ProjectTodoProjection;
import com.devcrew1os.repository.main.users.UserStatRepository;
import com.devcrew1os.repository.main.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTransaction {

    private final UsersRepository usersRepo;
    private final UserStatRepository userStatRepo;

    private final ProjectRepository projectInfoRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;
    private final ProjectLogRepository projectLogRepo;

    private static final Logger logger = LoggerFactory.getLogger(ProjectTransaction.class);

    /*===========================
       전체 목표조회
    ===========================*/
    @Transactional(readOnly = true)
    public GetProjectData getProjectProcess(String userId) {
        // 1. fetch data: users
        Users users = usersRepo.findUsersAndProjectByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request User not found")
        );
        int projectId = users.getProject().getId();

        List<ProjectChallengeProjection> projectChallenges = projectChallengeRepo.findAllDataByProjectId(projectId, ProjectChallengeStatus.ONGOING.getValue());
        List<ProjectTodoProjection> projectTodos = projectTodoRepo.findAllDataByProjectId(projectId, ProjectTodoStatus.COMPLETE.getValue());

        // 3-3. grouping: projectTodo with challenge
        Map<Integer, List<ProjectTodoProjection>> projectTodoMap = projectTodos.stream()
                .collect(Collectors.groupingBy(ProjectTodoProjection::getChallengeId));

        // 4. struct data
        List<GetProjectChallenge> dtoList = projectChallenges.stream()
                .map(pc ->{
                    List<ProjectTodoProjection> relatedTodos = projectTodoMap.getOrDefault(pc.getChallenge().getId(), Collections.emptyList());
                    List<GetProjectTodo> getProjectTodoList = relatedTodos.stream()
                            .map(rtd -> new GetProjectTodo(
                                    pc.getChallenge().getId(),                  // projectTodo를 포함하는 ChallengeId
                                    rtd.getId(),                                // projectTodo 고유Id
                                    rtd.getTodo().getDesc(),
                                    rtd.getStatus()
                            )).collect(Collectors.toList());
                    return new GetProjectChallenge(
                            pc.getId(),                         // projectChallenge 고유Id
                            pc.getStatus(),
                            pc.getChallenge().getTitle(),
                            getProjectTodoList
                    );
                }).collect(Collectors.toList());

        return new GetProjectData(
                users.getUserName(),
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
        Project projectData = projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Request user project data not found")
        );

        if(type == ProjectUpdateType.TODO) {
            logger.info("[ProjectTrans][{}] Initiate update project todo", userId);
            updateTodos(userId, projectData.getId(),req , now);
        } else if(type == ProjectUpdateType.CHALLENGE) {
            logger.info("[ProjectTrans][{}] Initiate update project challenge", userId);
            updateChallenges(userId,projectData.getId(),req , now);
        }
    }

    private void updateChallenges(String userId, Integer projectId, UpdateProjectReq req, LocalDateTime now) {
        // 1. fetch data
        List<ProjectChallenge> challengeList = projectChallengeRepo.findAllByProjectIdWithChallenge(projectId);

        // 2. apply complete status
        boolean isAnyUpdateProceed = false;
        for(UpdateProjectChallenge dto : req.getChallengeList()) {
            ProjectChallenge entity = challengeList.stream()
                    .filter(pc -> pc.getId() == dto.getChallengeId()) // Challenge ID로 비교
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Update target challenge" + dto.getChallengeId() + " not found"));

            if(completeProjectChallenge(entity, dto)) {
                completeProjectTodo(projectId, entity, dto);
                recordProjectLog(userId, projectId, dto.getChallengeId(), now);
                isAnyUpdateProceed = true;
            }
        }
        if(isAnyUpdateProceed) {
            logger.info("[ProjectTrans][{}] Successfully update project challenge", userId);
        } else {
            logger.error("[ProjectTrans][{}] No actual changes applied to project challenges", userId);
        }
    }

    private boolean completeProjectChallenge(ProjectChallenge entity,
                                             UpdateProjectChallenge updated
    ) {
        boolean isUpdated = false;
        if(updated.getUpdatedStatus() != null){
            if(ProjectChallengeStatus.contains(updated.getUpdatedStatus()) &&
                    updated.getUpdatedStatus() == ProjectChallengeStatus.FINISHED.getValue()) {
                entity.setStatus(updated.getUpdatedStatus());
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    private void completeProjectTodo(int projectId,
                                     ProjectChallenge entity,
                                     UpdateProjectChallenge updated) {
        List<ProjectTodo> todoList = projectTodoRepo.findAllByProjectIdAndChallengeId(projectId, entity.getChallenge().getId());
        for(ProjectTodo todo : todoList) {
            todo.setStatus(ProjectTodoStatus.COMPLETE.getValue());
        }
    }

    private void recordProjectLog(String userId, Integer projectId, Integer challengeId, LocalDateTime now) {

        ProjectLog log = ProjectLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeId(challengeId)
                .userActionType(ProjectLoggingAction.COMPLETE_CHALLENGE.getValue())
                .userActionAt(now)
                .build();
        projectLogRepo.save(log);
        userStatRepo.updateCompleteChallenges(userId);
    }

    private void updateTodos(String userId, Integer projectId, UpdateProjectReq req, LocalDateTime now) {
        // 1. fetch data
        List<ProjectTodo> projectTodoList = projectTodoRepo.findAllByProjectIdWithStatus(projectId, ProjectTodoStatus.COMPLETE.getValue());

        // 2. assemble data
        Map<Integer, ProjectTodo> projectTodoMap = projectTodoList.stream()
                .collect(Collectors.toMap(ProjectTodo::getId, Function.identity()));
        List<UpdateProjectTodo> updatedTodoList = req.getChallengeList().stream()
                .filter(challenge -> challenge.getTodoList() != null)
                .flatMap(challenge -> challenge.getTodoList().stream())
                .collect(Collectors.toList());

        // 4. proceed update & logging
        boolean isAnyUpdateProceed = false;
        for(UpdateProjectTodo dto : updatedTodoList) {
            ProjectTodo entity = projectTodoMap.get(dto.getTodoId());
            if(entity == null) {
                throw new RuntimeException("Update target todos(" + dto.getTodoId() + ") not found");
            }
            if(applyProjectTodoUpdate(entity, dto)){
                recordProjectLog(userId, projectId, dto.getChallengeId(), dto, now);
                isAnyUpdateProceed = true;
            }
        }
        if(isAnyUpdateProceed) {
            logger.info("[ProjectTrans][{}] Successfully update project todos: {}", userId, updatedTodoList.size());
        } else {
            logger.error("[ProjectTrans][{}] No actual changes applied to project todos", userId);
        }
    }

    private boolean applyProjectTodoUpdate(ProjectTodo entity,
                                           UpdateProjectTodo updated
    ) {
        boolean isUpdated = false;
        if(updated.getUpdatedStatus() != null){
            entity.setStatus(updated.getUpdatedStatus());
            isUpdated = true;
        }
        return isUpdated;
    }

    private void recordProjectLog(String userId, Integer projectId, Integer challengeId, UpdateProjectTodo updated, LocalDateTime now) {
        Integer actionType = 0;
        if(updated.getUpdatedStatus() == 1){
            actionType = ProjectLoggingAction.COMPLETE_TODO.getValue();
        } else if(updated.getUpdatedStatus() == 2){
            actionType = ProjectLoggingAction.REVERT_TODO.getValue();
        }
        ProjectLog log = ProjectLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeId(challengeId)
                .userActionType(actionType)
                .userActionAt(now)
                .build();
        projectLogRepo.save(log);
    }
}
