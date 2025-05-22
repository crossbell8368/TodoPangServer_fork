package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.project.ProjectChallengeStatus;
import com.devcrew1os.common.enums.project.ProjectTodoStatus;
import com.devcrew1os.common.enums.project.ProjectUpdateType;
import com.devcrew1os.dto.main.project.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        try {
            GetProjectData data = transaction.getProjectProcess(userId);
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Success] Fetch ProjectData complete");
            logger.info("[ProjectService][{}] Successfully fetch project related data", userId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed]" + err.getMessage());
            logger.info("[ProjectService][{}] Request project data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed]" + err.getMessage());
            logger.info("[ProjectService][{}] Failed to fetch project related data: {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       목표 업데이트
    ===========================*/
    public UpdateProjectRes updateProjects(String userId, UpdateProjectReq req) {
        UpdateProjectRes res = new UpdateProjectRes(false, "[Info] Update project initiated", ErrorCode.OK);
        ProjectUpdateType type = ProjectUpdateType.UNKNOWN;

        type = identifiedUpdateType(userId, req, res);
        if(type == ProjectUpdateType.UNKNOWN || type == ProjectUpdateType.INVALID) return res;

        try {
            transaction.updateProjectProcess(userId, req, type);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully update project data");
            logger.info("[ProjectService][{}] Successfully update project data", userId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed]" + err.getMessage());
            logger.info("[ProjectService][{}] Request project update data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed]" + err.getMessage());
            logger.info("[ProjectService][{}] Failed to update project data: {}", userId, err.getMessage());
            return res;
        }
    }

    private ProjectUpdateType identifiedUpdateType(String userId, UpdateProjectReq req, UpdateProjectRes res) {
        ProjectUpdateType determinedRequestType = ProjectUpdateType.UNKNOWN;
        List<String> errors = new ArrayList<>();
        boolean isTypeDetermined = false;
        boolean mixedTypeDetected = false;

        // 1. check update list
        if(req.getChallengeList() == null || req.getChallengeList().isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] ChallengeList must not be null or empty");
            logger.error("[ProjectService][{}] Invalid argument detected, at update project: {}", userId, res.getMessage());
            return ProjectUpdateType.INVALID;
        }

        // 2. check update element
        List<UpdateProjectChallenge> challengesToUpdate = req.getChallengeList();
        for (int i = 0; i < challengesToUpdate.size(); i++) {
            UpdateProjectChallenge challenge = challengesToUpdate.get(i);
            ProjectUpdateType currentEntryType = ProjectUpdateType.INVALID;

            if (challenge == null) {
                errors.add("[Failed] Challenge data at index(" + i + ") is null.");
                continue;
            }
            // 2-1. ID value check
            Integer currentChallengeId = challenge.getChallengeId();
            if (currentChallengeId == null) {
                errors.add("[Failed] ChallengeId at index " + i + " is null");
            }

            // 2-2. update type check
            boolean hasStatusUpdate = challenge.getUpdatedStatus() != null && ProjectChallengeStatus.contains(challenge.getUpdatedStatus());
            boolean hasTodoUpdates = challenge.getTodoList() != null;

            if (!hasStatusUpdate && !hasTodoUpdates) {
                errors.add("[Failed] No update data for all at index: " + i);
                currentEntryType = ProjectUpdateType.INVALID;
            } else if (hasStatusUpdate && hasTodoUpdates) {
                errors.add("[Failed] Both update data exist " + " at index: " + i);
                currentEntryType = ProjectUpdateType.INVALID;
            } else if (hasStatusUpdate) {
                currentEntryType = ProjectUpdateType.CHALLENGE;
            } else {
                currentEntryType = ProjectUpdateType.TODO;

                // 2-3 Todo update case: element check
                List<UpdateProjectTodo> todoList = challenge.getTodoList();
                for (int j = 0; j < todoList.size(); j++) {
                    UpdateProjectTodo todo = todoList.get(j);
                    if (todo == null) {
                        errors.add("[Failed] Todo at index (" + j + ") within " + "challenge(" + challenge.getChallengeId() + ") at index " + i + " is null");
                        continue;
                    }
                    if (todo.getTodoId() == null) {
                        errors.add("[Failed] TodoId at index (" + j + ") within " + "challenge(" + challenge.getChallengeId() + ") at index " + i + " is null");
                    }
                    if(todo.getUpdatedStatus() == null) {
                        errors.add("[Failed] Todo updatedStatus at index (" + j + ") within " + "challenge(" + challenge.getChallengeId() + ") at index " + i + " is null");
                    } else {
                        if(!ProjectTodoStatus.contains(todo.getUpdatedStatus())){
                            errors.add("[Failed] Todo updatedStatus at index (" + j + ") within " + "challenge(" + challenge.getChallengeId() + ") at index " + i + " is invalid");
                        }
                    }
                }
            }

            if (currentEntryType != ProjectUpdateType.INVALID) {
                if (!isTypeDetermined) {
                    determinedRequestType = currentEntryType;
                    isTypeDetermined = true;
                } else if (determinedRequestType != currentEntryType) {
                    mixedTypeDetected = true;
                    break;
                }
            }
        }
        if (mixedTypeDetected) {
            errors.add("[Failed] Mixed update types found. Request must contain only Challenge status updates OR only Todo list updates.");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            String errorMessage = String.join("\n", errors);
            res.addMessage(errorMessage);
            logger.error("[ProjectService][{}] Invalid arguments detected during project update validation:\n{}", userId, errorMessage);
            return ProjectUpdateType.INVALID;
        }
        if (!isTypeDetermined) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] No valid update entries found in the request.");
            logger.error("[ProjectService][{}] No valid update entries found in the request challenge list.", userId);
            return ProjectUpdateType.INVALID;
        }
        res.addMessage("[Success] Update project request is valid");
        logger.info("[ProjectService][{}] Update project request is valid", userId);
        return determinedRequestType;
    }
}
