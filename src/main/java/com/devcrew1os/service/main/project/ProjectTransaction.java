package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.project.ProjectChallengeStatus;
import com.devcrew1os.common.enums.project.ProjectTodoStatus;
import com.devcrew1os.common.enums.project.ProjectLoggingAction;
import com.devcrew1os.dto.main.project.*;
import com.devcrew1os.entity.log.ProjectLog;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.repository.project.ProjectLogRepository;
import com.devcrew1os.repository.project.ProjectChallengeRepository;
import com.devcrew1os.repository.project.ProjectTodoRepository;
import com.devcrew1os.repository.projection.ProjectChallengeProjection;
import com.devcrew1os.repository.projection.ProjectTodoProjection;
import com.devcrew1os.repository.users.UserStatRepository;
import com.devcrew1os.repository.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTransaction {

    private final UsersRepository usersRepo;
    private final UserStatRepository userStatRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;
    private final ProjectLogRepository projectLogRepo;

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
        List<ProjectTodoProjection> projectTodos = projectTodoRepo.findAllDataByProjectId(projectId, ProjectTodoStatus.COMPLETE.getValue(), ProjectTodoStatus.REMOVED.getValue());

        // 3-3. grouping: projectTodo with challenge
        Map<Integer, List<ProjectTodoProjection>> projectTodoMap = projectTodos.stream()
                .collect(Collectors.groupingBy(ProjectTodoProjection::getChallengeId));

        // 4. struct data
        List<GetProjectChallenge> dtoList = projectChallenges.stream()
                .map(pc ->{
                    List<ProjectTodoProjection> relatedTodos = projectTodoMap.getOrDefault(pc.getChallengeId(), Collections.emptyList());
                    List<GetProjectTodo> getProjectTodoList = relatedTodos.stream()
                            .map(rtd -> new GetProjectTodo(
                                    rtd.getId(),
                                    rtd.getTodoDesc(),
                                    rtd.getStatus()
                            )).collect(Collectors.toList());
                    return new GetProjectChallenge(
                            pc.getProjectChallengeId(),
                            pc.getChallengeId(),
                            pc.getStatus(),
                            pc.getChallengeTitle(),
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
    public void updateProjectProcess(String userId, UpdateProjectReq req) {
        Integer projectId = usersRepo.findProjectIdByUserId(userId);
        if (projectId == null) {
            throw new RuntimeException("Request users project data not found");
        }
        LocalDateTime now = LocalDateTime.now();
        List<ProjectLog> logs = new ArrayList<>();
        Map<Integer, Integer> challengesToCheck = new HashMap<>();

        for (UpdateProjectChallenge challengeDto : req.getChallengeList()) {
            Integer originalChallengeId = projectChallengeRepo.getOriginalChallengeId(projectId, challengeDto.getChallengeId());
            if (originalChallengeId == null) {
                throw new RuntimeException("Request challenge data not found");
            }
            updateChallengeProcess(userId, projectId, originalChallengeId, challengeDto, now, logs);

            updateTodoProcess(userId, projectId, challengeDto.getChallengeId(), originalChallengeId, challengeDto.getTodoList(), now, logs, challengesToCheck);
        }
        if(!challengesToCheck.isEmpty()) {
            checkChallengeRemoval(userId, projectId, challengesToCheck, now, logs);
        }
        if (!logs.isEmpty()) {
            projectLogRepo.saveAll(logs);
        }
    }

    private void updateChallengeProcess(
            String userId,
            int projectId,
            int originalChallengeId,
            UpdateProjectChallenge challengeDto,
            LocalDateTime now,
            List<ProjectLog> logs
    ) {
        if (challengeDto.getUpdatedStatus() == null) return;

        if (ProjectChallengeStatus.fromValue(challengeDto.getUpdatedStatus()) == ProjectChallengeStatus.FINISHED) {
            projectChallengeRepo.updateProjectChallengeStatus(ProjectChallengeStatus.FINISHED.getValue(), projectId, challengeDto.getChallengeId());
            projectTodoRepo.updateAllTodosByProjectIdAndChallengeId(ProjectTodoStatus.COMPLETE.getValue(), projectId, challengeDto.getChallengeId());
            userStatRepo.updateCompleteChallenges(userId);

            logs.add(createLog(userId, projectId, originalChallengeId, null, now, ProjectLoggingAction.COMPLETE_CHALLENGE));
        }
    }

    private void updateTodoProcess(
            String userId,
            int projectId,
            int projectChallengeId,
            int originalChallengeId,
            List<UpdateProjectTodo> todoList,
            LocalDateTime now,
            List<ProjectLog> logs,
            Map<Integer, Integer> challengesToCheck
    ){
        for (UpdateProjectTodo todo : todoList) {
            projectTodoRepo.updateTodoStatusByProjectIdAndChallengeId(todo.getUpdatedStatus(), projectId, todo.getTodoId());

            ProjectLoggingAction actionType = null;
            switch (ProjectTodoStatus.fromValue(todo.getUpdatedStatus())) {
                case CHECK:
                    actionType = ProjectLoggingAction.COMPLETE_TODO;
                    break;
                case UNCHECK:
                    actionType = ProjectLoggingAction.REVERT_TODO;
                    break;
                case REMOVED:
                    actionType = ProjectLoggingAction.REMOVE_TODO;
                    challengesToCheck.put(projectChallengeId, originalChallengeId);
                    break;
            }
            if (actionType != null) {
                logs.add(createLog(userId, projectId, originalChallengeId, todo.getTodoId(), now, actionType));
            }
        }
    }

    private void checkChallengeRemoval(
            String userId,
            int projectId,
            Map<Integer, Integer> challengesToCheck,
            LocalDateTime now,
            List<ProjectLog> logs
    ) {
        for (Integer projectChallengeId : challengesToCheck.keySet()) {
            int activeTodoCount = projectTodoRepo.countActiveTodoCount(
                    projectId,
                    challengesToCheck.get(projectChallengeId),
                    ProjectTodoStatus.CHECK.getValue(),
                    ProjectTodoStatus.UNCHECK.getValue()
            );
            if (activeTodoCount == 0) {
                projectChallengeRepo.updateProjectChallengeStatus(ProjectChallengeStatus.REMOVED.getValue(), projectId, projectChallengeId);
                logs.add(createLog(userId, projectId, challengesToCheck.get(projectChallengeId), null, now, ProjectLoggingAction.REMOVE_CHALLENGE));
            }
        }
    }

    private ProjectLog createLog(
            String userId,
            int projectId,
            Integer challengeId,
            Integer todoId,
            LocalDateTime now,
            ProjectLoggingAction action
    ) {
        return ProjectLog.builder()
                .userId(userId)
                .projectId(projectId)
                .challengeId(challengeId)
                .todoId(todoId)
                .userActionAt(now)
                .userActionType(action.getValue())
                .build();
    }
}
