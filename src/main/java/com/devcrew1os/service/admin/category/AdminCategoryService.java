package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.CategoryStatus;
import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.category.*;
import com.devcrew1os.entity.admin.challenge.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public GetAdminCategoryRes getAdminCategories(String adminId) {
        GetAdminCategoryRes res = new GetAdminCategoryRes(false, false, "[Info] Get admin categories initiated", ErrorCode.OK);

        GetAdminCategoryData[] data = getCategoryData(adminId, res);
        if (data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully fetch categories");
        logger.info("[AdminCategory][{}] Successfully fetch categories", adminId);
        return res;
    }

    private GetAdminCategoryData[] getCategoryData(String adminId, GetAdminCategoryRes res) {
        try {
            List<AdminCategory> adminCategories = categoryRepo.findAllByOrderByIdDesc();
            GetAdminCategoryData[] data =new GetAdminCategoryData[adminCategories.size()];
            for (int i = 0; i < adminCategories.size(); i++) {
                data[i] = new GetAdminCategoryData(
                        adminCategories.get(i).getId(),
                        adminCategories.get(i).getStatus(),
                        adminCategories.get(i).getTitle()
                );
            }
            logger.info("[AdminCategory][{}] Successfully retrieved {} categories", adminId, data.length);
            return data;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Internal Database Error");
            logger.info("[AdminCategory][{}] Failed to retrieved categories", adminId);
            return null;
        }
    }

    /*===========================
       카테고리 추가
    ===========================*/
    public SetAdminCategoryRes setAdminCategories(String adminId, SetAdminCategoryReq req) {
        SetAdminCategoryRes  res = new SetAdminCategoryRes(false, false, "[Info] Set categories initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        if(!setAdminCategory(adminId, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Set categories finished");
        logger.info("[AdminCategory][{}] Set categories finished", adminId);
        return res;
    }

    private boolean isRequestValid(String adminId, SetAdminCategoryReq req, SetAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getNewCategories() == null || req.getNewCategories().isEmpty()) {
            errors.add("[Failed] New categories must not be null or empty");
        } else {
            for(String title : req.getNewCategories()) {
                if(title == null || title.isEmpty()) {
                    errors.add("[Failed] New category title must not be null or empty");
                }
            }
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategory][{}] Invalid argument detected, at set category request: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Set category request is valid");
        logger.info("[AdminCategory][{}] Valid request, at set category", adminId);
        return true;
    }

    private boolean setAdminCategory(String adminId, SetAdminCategoryReq req, SetAdminCategoryRes res) {

        List<AdminCategory> data = new ArrayList<>();
        for(String category : req.getNewCategories()) {
            data.add(createCategory(category));
        }
        logger.info("[AdminCategory][{}] Successfully retrieved {} categories at request", adminId, data.size());

        try {
            categoryTrans.saveCategories(data);
        } catch (Exception err){
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to save new category data.");
            logger.error("[AdminCategory][{}] Set category transaction failed, rolling back: {}", adminId, err.getMessage());
            return false;
        }
        res.addMessage("[Success] Successfully saved " + data.size() + " categories");
        logger.info("[AdminCategory][{}] Successfully saved {} categories", adminId, data.size());
        return true;
    }

    private AdminCategory createCategory(String title){
        return AdminCategory.builder()
                .status(1)
                .title(title)
                .build();
    }

    /*===========================
       카테고리 변경
    ===========================*/
    public UpdateAdminCategoryRes updateAdminCategory(String adminId, UpdateAdminCategoryReq req) {
        UpdateAdminCategoryRes res = new UpdateAdminCategoryRes(false, false, "[Info] Update categories initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        List<AdminCategory> data = fetchCategory(adminId, req, res);
        if(data == null || data.isEmpty()) return res;

        if(!updateCategory(data, adminId, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Update categories finished");
        logger.info("[AdminCategory][{}] Update categories finished", adminId);
        return res;
    }

    private boolean isRequestValid(String adminId, UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        for(UpdateAdminCategoryData data : req.getUpdatedCategories()){
            if (data.getCategoryId() == null) {
                errors.add("[Failed] Updated categoryId must not be null");
            }
            if (data.getCategoryStatus() == null || !CategoryStatus.isValidValue(data.getCategoryStatus())) {
                errors.add("[Failed] Updated category(" + data.getCategoryId() + ") status must not be null or valid");
            }
            if (data.getCategoryTitle() == null || data.getCategoryTitle().isEmpty()) {
                errors.add("[Failed] Updated category(" + data.getCategoryId() + ")title must not be null or empty");
            }
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategory][{}] Invalid argument detected, at updated categories: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Update category request is valid");
        logger.info("[AdminCategory][{}] Update category request is valid", adminId);
        return true;
    }

    private List<AdminCategory> fetchCategory(String adminId, UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        List<Integer> categoryIdList = req.getUpdatedCategories().stream()
                .map(UpdateAdminCategoryData::getCategoryId)
                .collect(Collectors.toList());
        try {
            List<AdminCategory> category = categoryTrans.getCategoryByIdList(categoryIdList);
            res.addMessage("[Success] Fetched " + category.size() + " categories data");
            logger.info("[AdminCategory][{}] Fetched {} categories data", adminId, category.size());
            return category;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminCategory][{}] {}", adminId, err.getMessage());
            return null;
        }
    }

    private boolean updateCategory(List<AdminCategory> category, String adminId, UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {

        Map<Integer, UpdateAdminCategoryData> updatedData = req.getUpdatedCategories().stream()
                .collect(Collectors.toMap(UpdateAdminCategoryData::getCategoryId, data -> data));
        try {
            categoryTrans.updateCategory(category, updatedData);
            res.addMessage("[Success] " + category.size() + " Category data updated");
            logger.info("[AdminCategory][{}] {} Categories data updated", adminId, category.size());
            return true;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Category data not updated: " + err.getMessage());
            logger.error("[AdminCategory][{}] Failed to update categories data: {}", adminId, err.getMessage());
            return false;
        }
    }
}
