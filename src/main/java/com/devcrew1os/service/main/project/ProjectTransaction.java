package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.project.*;
import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.project.ProjectChallenge;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.project.ProjectTodo;
import com.devcrew1os.entity.main.user.UserInfo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTransaction {

    private final UserInfoRepository userInfoRepo;

    private final ChallengeInfoRepository challengeInfoRepo;
    private final ChallengeTodoRepository challengeTodoRepo;

    private final ProjectInfoRepository projectInfoRepo;
    private final ProjectTodoRepository projectTodoRepo;
    private final ProjectChallengeRepository projectChallengeRepo;

    private static final Logger logger = LoggerFactory.getLogger(ProjectTransaction.class);

    /*===========================
       전체 목표조회
    ===========================*/
    public UserInfo getUserInfo(String userId) {
        return userInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }

    public ProjectInfo getProjectInfo(String userId) {
        return projectInfoRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("Project not found")
        );
    }

    public List<ProjectChallenge> getProjectChallenges(Integer projectId) {
       return projectChallengeRepo.findAllByProjectId(projectId);
    }

    public List<ProjectTodo> getProjectTodos(Integer projectId) {
        return projectTodoRepo.findAllByProjectId(projectId);
    }

    public List<ChallengeInfoProjection> getChallengeList(List<Integer> challengeIds) {
        return challengeInfoRepo.findAllByIdIn(challengeIds);
    }

    public List<ChallengeTodo> getChallengeTodos(List<Integer> todoIds) {
        return challengeTodoRepo.findAllByIdIn(todoIds);
    }

    /*===========================
       도전과제 업데이트
    ===========================*/
    @Transactional
    public boolean updateProjectProcess(String userId, UpdateProjectReq req, UpdateProjectRes res) {
        try {
            // 1. prepare data
            UpdateProjectDTO dto = prepareProjectData(userId,req, res);
            if(dto == null) return false;

            // 2. validate check
            validateProjectData(dto.getProjectId(), dto.getChallengeIdList(), dto.getTodoIdList());

            // 3. update data
            updateProjectData(dto.getEntityList(), req);
            return true;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] ProjectData update failed");
            logger.error("[ProjectTrans][{}] Failed to update project data: {}", userId, err.getMessage());
            return false;
        }
    }

    private UpdateProjectDTO prepareProjectData(String userId, UpdateProjectReq req, UpdateProjectRes res) {
        try {
            ProjectInfo projectData = getProjectInfo(userId);
            List<Integer> challengeIdList = new ArrayList<>();
            List<Integer> todoIdList = new ArrayList<>();

            for (UpdateProjectChallenge challenge : req.getChallengeList()) {
                challengeIdList.add(challenge.getChallengeId());
                for (UpdateProjectTodo todo : challenge.getTodoList()) {
                    todoIdList.add(todo.getTodoId());
                }
            }
            List<ProjectTodo> entityList = projectTodoRepo.findByProjectIdAndProjectTodoIdIn(projectData.getId(), todoIdList);

            return new UpdateProjectDTO(
                    projectData.getId(),
                    challengeIdList,
                    todoIdList,
                    entityList
            );
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] ProjectData preparation failed, while fetch project related data");
            logger.error("[ProjectTrans][{}] Failed to fetch project data: {}", userId, err.getMessage());
            return null;
        }
    }

    private void validateProjectData(Integer projectId, List<Integer> challengeIdList, List<Integer> todoIdList) {

        Set<Integer> existChallengeIds = projectChallengeRepo.findIdsByProjectIdAndChallengeInfoIdIn(projectId, challengeIdList);
        List<Integer> missingChallengeIds = challengeIdList.stream()
                .filter(id -> !existChallengeIds.contains(id))
                .collect(Collectors.toList());
        if(!missingChallengeIds.isEmpty()) {
            throw new RuntimeException("Request challenge not exist: " + missingChallengeIds);
        }

        Set<Integer> existTodoIds = projectTodoRepo.findIdsByProjectIdAndProjectTodoIdIn(projectId, todoIdList);
        List<Integer> missingTodoIds = todoIdList.stream()
                .filter(id -> !existTodoIds.contains(id))
                .collect(Collectors.toList());
        if(!missingTodoIds.isEmpty()) {
            throw new RuntimeException("Request todo not exist: " + missingTodoIds);
        }
    }

    private void updateProjectData(List<ProjectTodo> todoList, UpdateProjectReq req) {
        Map<Integer, Integer> updateMap = req.getChallengeList().stream()
                .flatMap(challenge -> challenge.getTodoList().stream())
                .collect(Collectors.toMap(UpdateProjectTodo::getTodoId, UpdateProjectTodo::getUpdatedStatus));

        for (ProjectTodo todo : todoList) {
            if (updateMap.containsKey(todo.getProjectTodoId())) {
                todo.setProjectTodoStatus(updateMap.get(todo.getProjectTodoId()));
            }
        }
    }
}
