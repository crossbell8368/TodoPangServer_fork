package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.category.*;
import com.devcrew1os.service.admin.category.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class AdminCategoryController {

    private final Response<?> response;
    private final AdminCategoryService categoryService;

    /*===========================
       카테고리 목록조회
    ===========================*/
    @PostMapping("/fetch")
    public ResponseEntity<?> getCategories(){
        GetAdminCategoryRes res = categoryService.getAdminCategories(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(
            @RequestBody SetAdminCategoryReq req
    ){
        SetAdminCategoryRes res = categoryService.setAdminCategories(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(
            @RequestBody UpdateAdminCategoryReq req
    ) {
        UpdateAdminCategoryRes res = categoryService.updateAdminCategory(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                req
        );
        return response.handleResult(res);
    }
}
