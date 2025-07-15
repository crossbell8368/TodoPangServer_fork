package com.devcrew1os.service.main.project;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.project.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed]" + err.getMessage());
            logger.error("[ProjectService][{}] Request project data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed]" + err.getMessage());
            logger.error("[ProjectService][{}] Failed to fetch project related data: {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       목표 업데이트
    ===========================*/
    public UpdateProjectRes updateProjects(String userId, UpdateProjectReq req) {
        UpdateProjectRes res = new UpdateProjectRes(false, "[Info] Update project initiated", ErrorCode.OK);
        if(!isRequestValid(userId, req, res)) return res;
        try {
            transaction.updateProjectProcess(userId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully update project data");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed]" + err.getMessage());
            logger.error("[ProjectService][{}] Request project update data not found: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            res.addMessage("[Failed]" + err.getMessage());
            logger.error("[ProjectService][{}] Failed to update project data: {}", userId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String userId, UpdateProjectReq req, UpdateProjectRes res) {

        if(req.getChallengeList() == null || req.getChallengeList().isEmpty()) {
            res.addMessage("[Error] Update challenge list is empty");
            logger.error("[ProjectService][{}] Invalid argument detected, at update project: {}", userId, res.getMessage());
            return false;
        } else {
            return true;
        }
    }
}
