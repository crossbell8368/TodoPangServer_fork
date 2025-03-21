package com.devcrew1os.controller.admin;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.admin.category.GetAdminCategoryReq;
import com.devcrew1os.dto.admin.category.GetAdminCategoryRes;
import com.devcrew1os.service.admin.category.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/category")
public class CategoryController {

    private final Response<?> response;
    private final AdminCategoryService categoryService;

    /*===========================
       카테고리 목록조회
    ===========================*/
    @PostMapping("/list")
    public ResponseEntity<?> getCategories(
            @RequestBody GetAdminCategoryReq req
    ){
        GetAdminCategoryRes res = categoryService.getAdminCategories(req);
        return response.handleResult(res);
    }
}
