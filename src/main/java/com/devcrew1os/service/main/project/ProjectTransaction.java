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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        // fetch data: projectId
        Integer projectId = usersRepo.findProjectIdByUserId(userId);
        if(projectId == null) {
            logger.error("[ProjectTrans][{}] Request users project data not found", userId);
            throw new RuntimeException("Request users project data not found");
        }
        LocalDateTime now = LocalDateTime.now();
        List<ProjectLog> logs = new ArrayList<>();
        for(UpdateProjectChallenge challenge : req.getChallengeList()) {

            Integer originalChallengeId = projectChallengeRepo.getOriginalChallengeId(projectId, challenge.getChallengeId());
            if(originalChallengeId == null) {
                logger.error("[ProjectTrans][{}] Request challenge data not found", userId);
                throw new RuntimeException("Request challenge data not found");
            }
            // challenge update
            if(challenge.getChallengeId() != null && challenge.getUpdatedStatus() != null) {
                ProjectLog newChallengeLog = ProjectLog.builder()
                        .userId(userId)
                        .projectId(projectId)
                        .challengeId(originalChallengeId)
                        .userActionAt(now)
                        .build();

                switch(ProjectChallengeStatus.fromValue(challenge.getUpdatedStatus())){
                    case FINISHED:
                        projectChallengeRepo.updateProjectChallengeStatus(
                                ProjectChallengeStatus.FINISHED.getValue(),
                                projectId,
                                challenge.getChallengeId()
                        );
                        projectTodoRepo.updateAllTodosByProjectIdAndChallengeId(
                                ProjectTodoStatus.COMPLETE.getValue(),
                                projectId,
                                challenge.getChallengeId()
                        );
                        userStatRepo.updateCompleteChallenges(userId);

                        newChallengeLog.setUserActionType(ProjectLoggingAction.COMPLETE_CHALLENGE.getValue());
                        logs.add(newChallengeLog);
                        break;

                    case REMOVED:
                        if(projectTodoRepo.countActiveTodoCount(
                                projectId,
                                challenge.getChallengeId(),
                                ProjectTodoStatus.CHECK.getValue(),
                                ProjectTodoStatus.UNCHECK.getValue(),
                                ProjectTodoStatus.COMPLETE.getValue()) == 0
                        ) {
                            projectChallengeRepo.updateProjectChallengeStatus(
                                    ProjectChallengeStatus.REMOVED.getValue(),
                                    projectId,
                                    challenge.getChallengeId()
                            );
                            newChallengeLog.setUserActionType(ProjectLoggingAction.REMOVE_CHALLENGE.getValue());
                            logs.add(newChallengeLog);
                        } else {
                            throw new RuntimeException("Invalid challenge delete request");
                        }
                        break;
                }
            }
            // Todo update
            for(UpdateProjectTodo todo : challenge.getTodoList()) {
                ProjectLog newTodoLog = ProjectLog.builder()
                    .userId(userId)
                    .projectId(projectId)
                    .challengeId(originalChallengeId)
                    .todoId(todo.getTodoId())
                    .userActionAt(now)
                    .build();

                projectTodoRepo.updateTodoStatusByProjectIdAndChallengeId(
                        todo.getUpdatedStatus(),
                        projectId,
                        todo.getTodoId()
                );

                switch(ProjectTodoStatus.fromValue(todo.getUpdatedStatus())) {
                    case CHECK:
                        newTodoLog.setUserActionType(ProjectLoggingAction.COMPLETE_TODO.getValue());
                        logs.add(newTodoLog);
                        break;
                    case UNCHECK:
                        newTodoLog.setUserActionType(ProjectLoggingAction.REVERT_TODO.getValue());
                        logs.add(newTodoLog);
                        break;
                    case REMOVED:
                        newTodoLog.setUserActionType(ProjectLoggingAction.REMOVE_TODO.getValue());
                        logs.add(newTodoLog);
                        break;
                }
            }
        }
        projectLogRepo.saveAll(logs);
    }
}
