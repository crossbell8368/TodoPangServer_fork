package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.category.GetAdminCategoryData;
import com.devcrew1os.dto.admin.category.GetAdminCategoryReq;
import com.devcrew1os.dto.admin.category.GetAdminCategoryRes;
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
            logger.info("[AdminCategoryService][{}] Failed to retrieved admin categories", req.getAdminId());
            return null;
        }
    }
}
