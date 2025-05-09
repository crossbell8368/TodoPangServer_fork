package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.category.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final AdminCategoryTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryService.class);

    /*===========================
       카테고리 목록조회
    ===========================*/
    public GetAdminCategoryRes getAdminCategories(String adminId) {
        GetAdminCategoryRes res = new GetAdminCategoryRes(false, "[Info] Get admin categories initiated", ErrorCode.OK);

        try {
            List<GetAdminCategoryData> data = transaction.getCategoryProcess();
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Successfully fetch categories");
            logger.info("[AdminCategory][{}] Successfully fetch categories", adminId);
            return res;

        } catch (RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] Error detected while get categoryData");
            logger.info("[AdminCategory][{}] Failed to get category data: {}", adminId, err.getMessage());
            return res;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Server error detected, while get categoryData");
            logger.info("[AdminCategory][{}] Server error detected, while get category data: {}", adminId, err.getMessage());
            return res;
        }
    }

    /*===========================
       카테고리 추가
    ===========================*/
    public SetAdminCategoryRes setAdminCategories(String adminId, SetAdminCategoryReq req) {
        SetAdminCategoryRes  res = new SetAdminCategoryRes(false, "[Info] Set categories initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setCategoryProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Set categories finished");
            logger.info("[AdminCategory][{}] Set categories finished", adminId);
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to save new category data.");
            logger.error("[AdminCategory][{}] Set new category failed: {}", adminId, err.getMessage());
            return res;
        }
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

    /*===========================
       카테고리 변경
    ===========================*/
    public UpdateAdminCategoryRes updateAdminCategory(String adminId, UpdateAdminCategoryReq req) {
        UpdateAdminCategoryRes res = new UpdateAdminCategoryRes(false, "[Info] Update categories initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.updateCategoryProcess(adminId, req);
            res.setSuccess(true);
            res.addMessage("[Info] Update categories finished");
            logger.info("[AdminCategory][{}] Update categories finished", adminId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminCategory][{}] {}", adminId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Category data not updated: " + err.getMessage());
            logger.error("[AdminCategory][{}] Failed to update categories data: {}", adminId, err.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        for(UpdateAdminCategoryData data : req.getUpdatedCategories()){
            if (data.getCategoryId() == null) {
                errors.add("[Failed] Updated categoryId must not be null");
            }
            if (data.getCategoryStatus() == null || !DataStatus.isValidValue(data.getCategoryStatus())) {
                errors.add("[Failed] Updated category(" + data.getCategoryId() + ") status must not be null or valid");
            }
            if (data.getCategoryTitle() == null || data.getCategoryTitle().isEmpty()) {
                errors.add("[Failed] Updated category(" + data.getCategoryId() + ")title must not be null or empty");
            }
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategory][{}] Invalid argument detected, at update categories: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Update category request is valid");
        logger.info("[AdminCategory][{}] Update category request is valid", adminId);
        return true;
    }
}
