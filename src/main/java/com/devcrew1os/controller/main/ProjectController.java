package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.project.GetProjectRes;
import com.devcrew1os.dto.main.project.UpdateProjectReq;
import com.devcrew1os.dto.main.project.UpdateProjectRes;
import com.devcrew1os.service.main.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/project")
public class ProjectController {

    private final Response<?> response;
    private final ProjectService service;

    /*===========================
       목표 목록조회
    ===========================*/
    @PostMapping("/list")
    public ResponseEntity<?> getProject() {
        GetProjectRes res = service.getProject(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       목표 업데이트
    ===========================*/
    @PostMapping("/update")
    public ResponseEntity<?> updateProject(
            @RequestBody UpdateProjectReq req
    ) {
        UpdateProjectRes res = service.updateProjectRes(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    /*===========================
       목표 이루기
    ===========================*/
    @PostMapping("/complete")
    public ResponseEntity<?> completeProject(
            @RequestBody UpdateProjectReq req
    ) {
        UpdateProjectRes res = service.updateProjectRes(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    /*===========================
       목표 삭제
    ===========================*/
}
