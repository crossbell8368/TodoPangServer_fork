package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.category.*;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final AdminCategoryRepository categoryRepo;
    private final AdminUserRepository userRepo;

    private final AdminCategoryTransaction categoryTrans;

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryService.class);

    /*===========================
       카테고리 목록조회
    ===========================*/
    public GetAdminCategoryRes getAdminCategories(GetAdminCategoryReq req) {
        GetAdminCategoryRes res = new GetAdminCategoryRes(false, false, "[Info] Get admin categories initiated", ErrorCode.OK);
        GetAdminCategoryData data = new GetAdminCategoryData();

        if(!isRequestValid(req, res)) return res;

        if(!isAdminIdExist(req, res)) return res;

        data.setCategories(getCategoryData(req, res));
        if (data.getCategories() == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully admin categories");
        logger.info("[AdminCategoryService][{}] Successfully get admin categories", req.getAdminId());
        return res;
    }

    private boolean isRequestValid(GetAdminCategoryReq req, GetAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getAdminId() == null || req.getAdminId().isEmpty()) {
            errors.add("[Failed] AdminID must not be null or empty");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategoryService][{}] Invalid argument detected during get admin categories: {}", req.getAdminId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Get admin category request is valid");
        logger.info("[AdminCategoryService][{}] Get admin category request is valid", req.getAdminId());
        return true;
    }

    private boolean isAdminIdExist(GetAdminCategoryReq req, GetAdminCategoryRes res) {
        if (userRepo.existsAdminUserByUserId(req.getAdminId())) {
            res.addMessage("[Success] AdminId exists");
            logger.info("[AdminCategoryService][{}] Admin exists, at get admin categories", req.getAdminId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] AdminId not found");
        logger.warn("[AdminCategoryService][{}] User not found, at get admin categories", req.getAdminId());
        return false;
    }

    private Map<Integer, String> getCategoryData(GetAdminCategoryReq req, GetAdminCategoryRes res) {

        Map<Integer, String> data = new HashMap<>();
        try {
            List<AdminCategory> adminCategories = categoryRepo.findAllByOrderByIdDesc();
            for (AdminCategory category : adminCategories) {
                data.put(category.getId(), category.getTitle());
            }
            logger.info("[AdminCategoryService][{}] Successfully retrieved {} categories", req.getAdminId(), data.size());
            return data;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Internal Database Error");
            logger.info("[AdminCategoryService][{}] Failed to retrieved admin categories", req.getAdminId());
            return null;
        }
    }

    /*===========================
       카테고리 추가
    ===========================*/
    public SetAdminCategoryRes setAdminCategories(SetAdminCategoryReq req) {
        SetAdminCategoryRes  res = new SetAdminCategoryRes(false, false, "[Info] Set categories initiated", ErrorCode.OK);

        if(!isRequestValid(req, res)) return res;

        if(!isAdminIdExist(req, res)) return res;

        if(!setAdminCategory(req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Set categories finished");
        logger.info("[AuthService][{}] Set categories finished", req.getAdminId());
        return res;
    }

    private boolean isRequestValid(SetAdminCategoryReq req, SetAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getAdminId() == null || req.getAdminId().isEmpty()) {
            errors.add("[Failed] AdminID must not be null or empty");
        }
        if (req.getNewCategories() == null || req.getNewCategories().isEmpty()) {
            errors.add("[Failed] New categories must not be null or empty");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategoryService][{}] Invalid argument detected during set category request: {}", req.getAdminId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid set category request");
        logger.info("[AdminCategoryService][{}] Valid request, at set category", req.getAdminId());
        return true;
    }

    private boolean isAdminIdExist(SetAdminCategoryReq req, SetAdminCategoryRes res) {
        if (userRepo.existsAdminUserByUserId(req.getAdminId())) {
            res.addMessage("[Success] AdminId exists");
            logger.info("[AdminCategoryService][{}] Admin exists, at set categories", req.getAdminId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] AdminId not found");
        logger.warn("[AdminCategoryService][{}] User not found, at set categories", req.getAdminId());
        return false;
    }

    private boolean setAdminCategory(SetAdminCategoryReq req, SetAdminCategoryRes res) {

        List<AdminCategory> data = new ArrayList<>();
        for(String category : req.getNewCategories()) {
            data.add(createCategory(category));
        }
        logger.info("[AdminCategoryService][{}] Successfully retrieved {} categories at request", req.getAdminId(), data.size());

        try {
            categoryTrans.saveCategories(data);
        } catch (Exception err){
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to save new category data.");
            logger.error("[AuthTransaction][{}] Set category transaction failed, rolling back: {}", req.getAdminId(), err.getMessage());
            return false;
        }
        res.addMessage("[Success] Successfully saved " + data.size() + " categories");
        logger.info("[AdminCategoryService][{}] Successfully saved {} categories", req.getAdminId(), data.size());
        return true;
    }

    private AdminCategory createCategory(String title){
        return AdminCategory.builder()
                .title(title)
                .build();
    }


    /*===========================
       카테고리 변경
    ===========================*/



    /*===========================
       카테고리 제거
    ===========================*/
}
