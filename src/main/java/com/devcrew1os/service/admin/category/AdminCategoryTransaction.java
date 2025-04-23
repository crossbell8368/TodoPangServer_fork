package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.AdminStatus;
import com.devcrew1os.dto.admin.category.GetAdminCategoryData;
import com.devcrew1os.dto.admin.category.SetAdminCategoryReq;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryData;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryReq;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryTransaction {

    private final AdminUserRepository adminUserRepo;
    private final AdminCategoryRepository categoryRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryTransaction.class);

    /*===========================
       카테고리 목록
    ===========================*/
    @Transactional(readOnly = true)
    public List<GetAdminCategoryData> getCategoryProcess(String adminId) {
        // 1. fetch data
        List<AdminCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
        if(categoryList.isEmpty()) {
            throw new RuntimeException("Category not exist");
        }
        Map<String, String> adminMap = getAdminData(categoryList);

        // 2. set data
        List<GetAdminCategoryData> dataList = new ArrayList<>();
        for(AdminCategory category : categoryList) {
            dataList.add(
                    new GetAdminCategoryData(
                            category.getId(),
                            category.getStatus(),
                            category.getTitle(),
                            category.getChallengesCount(),
                            adminMap.get(category.getLastUpdatedBy()),
                            category.getLastUpdatedAt()
                    )
            );
        }
        return dataList;
    }

    private Map<String, String> getAdminData(List<AdminCategory> categoryList){
        List<String> adminIdList = categoryList.stream()
                .map(AdminCategory::getLastUpdatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<AdminUser> adminList = adminUserRepo.findAllByIdIn(adminIdList);

        if (adminList.size() != adminIdList.size()) {
            Set<String> foundAdminIds = adminList.stream().map(AdminUser::getId).collect(Collectors.toSet());
            List<String> missingAdminIds = adminIdList.stream().filter(id -> !foundAdminIds.contains(id)).collect(Collectors.toList());
            logger.warn("[AdminCategory] Unidentified adminId detected: {}", missingAdminIds);
        }
        return adminList.stream()
                .collect(Collectors.toMap(AdminUser::getId, AdminUser::getName));
    }

    /*===========================
       카테고리 추가
    ===========================*/
    @Transactional
    public void setCategoryProcess(String adminId, SetAdminCategoryReq req) {
        LocalDateTime now = LocalDateTime.now();
        List<AdminCategory> categoryData = new ArrayList<>();

        for(String categoryName : req.getNewCategories()) {
            categoryData.add(structCategoryEntity(categoryName, adminId, now));
        }
        categoryRepo.saveAll(categoryData);
    }

    private AdminCategory structCategoryEntity(String title, String adminId, LocalDateTime updatedAt){
        return AdminCategory.builder()
                .title(title)
                .status(AdminStatus.PREPARE.getValue())
                .challengesCount(0)
                .lastUpdatedAt(updatedAt)
                .lastUpdatedBy(adminId)
                .build();
    }

    /*===========================
       카테고리 변경
    ===========================*/
    @Transactional
    public void updateCategoryProcess(String adminId, UpdateAdminCategoryReq req) {

        // 1. fetch target data
        List<AdminCategory> categories = getCategoryByIdList(req.getUpdatedCategories());
        logger.info("[AdminCategory][{}] Fetched {} categories data", adminId, categories.size());

        // 2. update target data
        updateCategory(adminId, categories, req);
        logger.info("[AdminCategory][{}] {} Categories data updated", adminId, categories.size());
    }

    private List<AdminCategory> getCategoryByIdList(List<UpdateAdminCategoryData> data) {
        // extract IdList
        List<Integer> idList = data.stream()
                .map(UpdateAdminCategoryData::getCategoryId)
                .collect(Collectors.toList());

        // search data
        List<AdminCategory> categories = categoryRepo.findAllByIdIn(idList);

        // exception handling
        if (categories.size() != idList.size()) {
            List<Integer> foundIds = categories.stream()
                    .map(AdminCategory::getId)
                    .collect(Collectors.toList());

            List<Integer> missingIds = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());

            throw new RuntimeException("Categories not found: " + missingIds);
        }
        return categories;
    }

    private void updateCategory(String adminId, List<AdminCategory> entities, UpdateAdminCategoryReq req) {
        // prepare data
        LocalDateTime updatedAt = LocalDateTime.now();
        Map<Integer, UpdateAdminCategoryData> updated = req.getUpdatedCategories().stream()
                .collect(Collectors.toMap(UpdateAdminCategoryData::getCategoryId, data -> data));

        // process update
        for(AdminCategory entity : entities) {
            UpdateAdminCategoryData updatedData = updated.get(entity.getId());

            if (updatedData == null) {
                throw new IllegalArgumentException("[Error] No update data found for ID: " + entity.getId());
            }
            entity.setStatus(updatedData.getCategoryStatus());
            entity.setTitle(updatedData.getCategoryTitle());
            entity.setLastUpdatedAt(updatedAt);
            entity.setLastUpdatedBy(adminId);
        }
    }
}
