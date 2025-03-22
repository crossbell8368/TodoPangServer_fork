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
import java.util.List;

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

        if(!isRequestValid(req, res)) return res;

        if(!isAdminIdExist(req, res)) return res;

        GetAdminCategoryData[] data = getCategoryData(req, res);
        if (data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully fetch categories");
        logger.info("[AdminCategoryService][{}] Successfully fetch categories", req.getAdminId());
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
            logger.error("[AdminCategoryService][{}] Invalid argument detected, at fetch categories: {}", req.getAdminId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Fetch category request is valid");
        logger.info("[AdminCategoryService][{}] Fetch category request is valid", req.getAdminId());
        return true;
    }

    private boolean isAdminIdExist(GetAdminCategoryReq req, GetAdminCategoryRes res) {
        if (userRepo.existsAdminUserByUserId(req.getAdminId())) {
            res.addMessage("[Success] AdminId exists");
            logger.info("[AdminCategoryService][{}] Admin exists, at fetch categories", req.getAdminId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] AdminId not found");
        logger.warn("[AdminCategoryService][{}] User not found, at fetch categories", req.getAdminId());
        return false;
    }

    private GetAdminCategoryData[] getCategoryData(GetAdminCategoryReq req, GetAdminCategoryRes res) {
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
            logger.info("[AdminCategoryService][{}] Successfully retrieved {} categories", req.getAdminId(), data.length);
            return data;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Internal Database Error");
            logger.info("[AdminCategoryService][{}] Failed to retrieved categories", req.getAdminId());
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
        logger.info("[AdminCategoryService][{}] Set categories finished", req.getAdminId());
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
            logger.error("[AdminCategoryService][{}] Invalid argument detected, at set category request: {}", req.getAdminId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Set category request is valid");
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
                .status(1)
                .title(title)
                .build();
    }

    /*===========================
       카테고리 변경
    ===========================*/
    public UpdateAdminCategoryRes updateAdminCategory(UpdateAdminCategoryReq req) {
        UpdateAdminCategoryRes res = new UpdateAdminCategoryRes(false, false, "[Info] Update categories initiated", ErrorCode.OK);

        if(!isRequestValid(req, res)) return res;

        if(!isAdminIdExist(req, res)) return res;

        AdminCategory data = fetchCategory(req, res);
        if(data == null) return res;

        if(!updateCategory(data, req, res)) return res;

        res.setSuccess(true);
        res.addMessage("[Info] Update categories finished");
        logger.info("[AdminCategoryService][{}] Update categories finished", req.getAdminId());
        return res;
    }

    private boolean isRequestValid(UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getAdminId() == null || req.getAdminId().isEmpty()) {
            errors.add("[Failed] AdminID must not be null or empty");
        }
        if (req.getUpdatedCategoryId() == null) {
            errors.add("[Failed] Updated categoryId must not be null");
        }
        if (req.getUpdatedCategoryStatus() == null) {
            errors.add("[Failed] Updated category status must not be null");
        }
        if (req.getUpdatedCategoryTitle() == null || req.getUpdatedCategoryTitle().isEmpty()) {
            errors.add("[Failed] Updated category title must not be null or empty");
        }

        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminCategoryService][{}] Invalid argument detected, at updated categories: {}", req.getAdminId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] Update category request is valid");
        logger.info("[AdminCategoryService][{}] Update category request is valid", req.getAdminId());
        return true;
    }

    private boolean isAdminIdExist(UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        if (userRepo.existsAdminUserByUserId(req.getAdminId())) {
            res.addMessage("[Success] AdminId exists");
            logger.info("[AdminCategoryService][{}] Admin exists, at update categories", req.getAdminId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] AdminId not found");
        logger.warn("[AdminCategoryService][{}] User not found, at update categories", req.getAdminId());
        return false;
    }

    private AdminCategory fetchCategory(UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        try {
            AdminCategory category = categoryTrans.getCategoryById(req.getUpdatedCategoryId());
            res.addMessage("[Success] Fetched category data");
            logger.info("[AdminCategoryService][{}] Fetched category data", req.getAdminId());
            return category;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] Request category data not found");
            logger.error("[AdminCategoryService][{}] Failed to fetch category data", req.getAdminId());
            return null;
        }
    }

    private boolean updateCategory(AdminCategory category, UpdateAdminCategoryReq req, UpdateAdminCategoryRes res) {
        try {
            categoryTrans.updateCategory(category, req.getUpdatedCategoryStatus(), req.getUpdatedCategoryTitle());
            res.addMessage("[Success] Category data updated");
            logger.info("[AdminCategoryService][{}] Category({}) data updated", req.getAdminId(), req.getUpdatedCategoryId());
            return true;
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Category data not updated");
            logger.error("[AdminCategoryService][{}] Failed to update category({}) data", req.getAdminId(), req.getUpdatedCategoryId());
            return false;
        }
    }
}
