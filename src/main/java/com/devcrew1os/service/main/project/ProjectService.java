package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.project.*;
import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import com.devcrew1os.entity.main.project.ProjectChallenge;
import com.devcrew1os.entity.main.project.ProjectInfo;
import com.devcrew1os.entity.main.project.ProjectTodo;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.challenge.ChallengeInfoProjection;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    /*===========================
       목표조회
    ===========================*/
    public GetProjectRes getProject(String userId) {
        GetProjectRes res = new GetProjectRes(false, "[Info] Get project initiated", ErrorCode.OK);

        GetProjectData data = getProjectData(userId, res);
        if(data == null) return res;
        res.setData(data);

        res.setSuccess(true);
        res.addMessage("[Success] Fetch ProjectData complete");
        logger.error("[ProjectService][{}] Successfully fetch project related data", userId);
        return res;
    }

    private GetProjectData getProjectData(String userId, GetProjectRes res) {
        try {
            // 1.prepare data
            UserInfo userData = transaction.getUserInfo(userId);
            ProjectInfo projectData = transaction.getProjectInfo(userId);
            List<ProjectTodo> projectTodos = transaction.getProjectTodos(projectData.getId());
            List<ProjectChallenge> projectChallenges = transaction.getProjectChallenges(projectData.getId());

            // 2-1. fetch ProjectChallenge
            List<Integer> challengeIds = projectChallenges.stream()
                    .map(ProjectChallenge::getChallengeInfoId)
                    .collect(Collectors.toList());
            List<ChallengeInfoProjection> challengeInfoList = transaction.getChallengeList(challengeIds);

            // 2-2. fetch ProjectTodo
            List<Integer> todoIds = projectTodos.stream()
                    .map(ProjectTodo::getProjectTodoId)
                    .collect(Collectors.toList());
            List<ChallengeTodo> challengeTodoList = transaction.getChallengeTodos(todoIds);

            // 3. assemble data
            List<GetProjectChallenge> dataList = assembleData(
                    challengeIds,
                    challengeInfoList,
                    challengeTodoList,
                    projectTodos
            );

            // 4. struct return dto
            return new GetProjectData(
                    userData.getUserName(),
                    dataList.size(),
                    dataList
            );

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected, while fetch project related data");
            logger.error("[ProjectService][{}] Failed to fetch project related data: {}", userId, err.getMessage());
            return null;
        }
    }

    private List<GetProjectChallenge> assembleData(
            List<Integer> challengeIds,
            List<ChallengeInfoProjection> challengeInfoList,
            List<ChallengeTodo> challengeTodoList,
            List<ProjectTodo> projectTodos
    ) {
        // 3-1. Indexing challengeInfo
        Map<Integer, ChallengeInfoProjection> challengeInfoMap = challengeInfoList.stream()
                .collect(Collectors.toMap(ChallengeInfoProjection::getId, Function.identity()));

        // 3-2. Indexing challengeTodo
        Map<Integer, ChallengeTodo> challengeTodoMap = challengeTodoList.stream()
                .collect(Collectors.toMap(ChallengeTodo::getId, Function.identity()));

        // 3-3. Grouping ProjectTodo
        Map<Integer, List<ProjectTodo>> projectTodoMap = projectTodos.stream()
                .collect(Collectors.groupingBy(ProjectTodo::getProjectChallengeId));

        // 3-4. struct return data: Challenge
        List<GetProjectChallenge> challengeDtoList = new ArrayList<>();
        for(Integer challengeId : challengeIds) {

            ChallengeInfoProjection challengeInfo = challengeInfoMap.get(challengeId);

            List<ProjectTodo> challengeInfoUnitTodo = projectTodoMap.getOrDefault(challengeId, Collections.emptyList());

            List<GetProjectTodo> projectTodoList = new ArrayList<>();
            for(ProjectTodo pt : challengeInfoUnitTodo){
                ChallengeTodo todo = challengeTodoMap.get(pt.getProjectTodoId());
                String title = (todo != null) ? todo.getDesc() : "Unknown Todo";
                projectTodoList.add(
                        new GetProjectTodo(
                                challengeId,
                                pt.getProjectTodoId(),
                                title,
                                pt.getProjectTodoStatus()
                        )
                );
            }
            challengeDtoList.add(new GetProjectChallenge(
                    challengeId,
                    challengeInfo != null ? challengeInfo.getTitle() : "Unknown Challenge",
                    projectTodoList
            ));
        }
        return challengeDtoList;
    }

    /*===========================
       목표 업데이트
    ===========================*/
    public UpdateProjectRes updateProjectRes(String userId, UpdateProjectReq req) {
        UpdateProjectRes res = new UpdateProjectRes(false, "[Info] Update project initiated", ErrorCode.OK);

        if(!isRequestValid(userId, req, res)) return res;

        if(!transaction.updateProjectProcess(userId, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Successfully update project todo data");
        logger.info("[ProjectService][{}] Successfully update project todo data", userId);
        return res;
    }

    private boolean isRequestValid(String userId, UpdateProjectReq req, UpdateProjectRes res) {

        if(req.getChallengeList() == null || req.getChallengeList().isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] ChallengeList must not be null or empty");
            logger.error("[ProjectService][{}] Invalid argument detected, at update project: {}", userId, res.getMessage());
            return false;
        }

        for(UpdateProjectChallenge challenge : req.getChallengeList()) {
            if(challenge.getTodoList() == null || challenge.getTodoList().isEmpty()){
                res.setErrorCode(ErrorCode.BAD_REQUEST);
                res.addMessage("[Failed] ChallengeList must not be null or empty");
                logger.error("[ProjectService][{}] Invalid argument detected, at update project: {}", userId, res.getMessage());
                return false;
            }
        }
        return true;
    }
}
