package com.devcrew1os.service.admin.category;

import com.devcrew1os.common.enums.DataResult;
import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.common.enums.users.UserRole;
import com.devcrew1os.common.enums.users.UserStatus;
import com.devcrew1os.dto.admin.category.GetAdminCategoryData;
import com.devcrew1os.dto.admin.category.SetAdminCategoryReq;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryData;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryReq;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.repository.challenge.CategoryRepository;
import com.devcrew1os.repository.projection.AdminCategoryProjection;
import com.devcrew1os.repository.users.UsersRepository;
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

    private final CategoryRepository categoryRepo;
    private final UsersRepository usersRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryTransaction.class);

    /*===========================
       카테고리 목록
    ===========================*/
    @Transactional(readOnly = true)
    public List<GetAdminCategoryData> getCategoryProcess() {
        // 1. fetch data: Category
        List<AdminCategoryProjection> categoryList = categoryRepo.findAllWithChallengeCount(false);
        if(categoryList.isEmpty()) {
            throw new RuntimeException("Category not exist");
        }
        // 2. fetch data: Admin
        List<String> categoryUpdateByList = categoryList.stream()
                .map(AdminCategoryProjection::getUpdatedBy)
                .distinct()
                .collect(Collectors.toList());
        List<Users> adminList = usersRepo.findAllByUserIdAndRoleAndStatus(categoryUpdateByList, UserRole.ADMIN.getValue(), UserStatus.ACTIVE.getValue());

        // 3. data inspection
        if (adminList.size() != categoryUpdateByList.size()) {
            Set<String> foundAdminIds = adminList.stream().map(Users::getUserId).collect(Collectors.toSet());
            List<String> missingAdminIds = categoryUpdateByList.stream().filter(id -> !foundAdminIds.contains(id)).collect(Collectors.toList());
            logger.warn("[AdminChallenge] Unidentified adminId detected: {}", missingAdminIds);
        }
        Map<String, String> adminMap = adminList.stream().collect(Collectors.toMap(Users::getUserId, Users::getUserName));

        // 4. assemble data
        return categoryList.stream()
                .map(cat -> new GetAdminCategoryData(
                        cat.getId(),
                        cat.getStatus(),
                        cat.getTitle(),
                        cat.getChallengeCount(),
                        adminMap.get(cat.getUpdatedBy()),
                        cat.getUpdatedAt()
                        )
                ).collect(Collectors.toList());
    }

    /*===========================
       카테고리 추가
    ===========================*/
    @Transactional
    public void setCategoryProcess(String adminId, SetAdminCategoryReq req) {
        LocalDateTime now = LocalDateTime.now();

        List<Category> newCategoryList = req.getNewCategories().stream()
                .map(newTitle -> Category.builder()
                        .title(newTitle)
                        .status(DataStatus.PREPARE.getValue())
                        .isServerOnly(false)
                        .updatedAt(now)
                        .updatedBy(adminId)
                        .build()
                ).collect(Collectors.toList());
        categoryRepo.saveAll(newCategoryList);
    }

    /*===========================
       카테고리 변경
    ===========================*/
    @Transactional
    public void updateCategoryProcess(String adminId, UpdateAdminCategoryReq req) {

        // 1. fetch target data
        List<Category> categories = getCategoryByIdList(req.getUpdatedCategories());
        logger.info("[AdminCategory][{}] Fetched {} categories data", adminId, categories.size());

        // 2. update target data
        updateCategory(adminId, categories, req);
        logger.info("[AdminCategory][{}] {} Categories data updated", adminId, categories.size());
    }

    private List<Category> getCategoryByIdList(List<UpdateAdminCategoryData> data) {
        // extract IdList
        Set<Integer> idList = data.stream()
                .map(UpdateAdminCategoryData::getCategoryId)
                .collect(Collectors.toSet());
        List<Category> categories = categoryRepo.findAllByIdIn(idList);

        // exception handling
        if (categories.size() != idList.size()) {
            List<Integer> foundIds = categories.stream().map(Category::getId).collect(Collectors.toList());
            List<Integer> missingIds = idList.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());

            throw new RuntimeException("Categories not found: " + missingIds);
        }
        return categories;
    }

    private void updateCategory(String adminId, List<Category> entities, UpdateAdminCategoryReq req) {
        // prepare data
        LocalDateTime updatedAt = LocalDateTime.now();
        Map<Integer, UpdateAdminCategoryData> updated = req.getUpdatedCategories().stream()
                .collect(Collectors.toMap(UpdateAdminCategoryData::getCategoryId, data -> data));

        // process update
        for(Category entity : entities) {
            UpdateAdminCategoryData updatedData = updated.get(entity.getId());

            boolean statusUpdated = false;
            boolean titleUpdated = false;

            if (updatedData.getCategoryStatus() != null) {
                DataResult res = updateCategoryStatus(entity, updatedData);
                if (res.isSuccess()) {
                    entity.setStatus(updatedData.getCategoryStatus());
                    statusUpdated = true;
                } else {
                    throw new RuntimeException("Category status update failed: " + res.getDesc() + " for category ID: " + entity.getId());
                }
            }
            if (updatedData.getCategoryTitle() != null &&
                    !entity.getTitle().equals(updatedData.getCategoryTitle())
            ) {
                entity.setTitle(updatedData.getCategoryTitle());
                titleUpdated = true;
            }
            if(statusUpdated || titleUpdated) {
                entity.setUpdatedAt(updatedAt);
                entity.setUpdatedBy(adminId);
            }
        }
        categoryRepo.saveAll(entities);
    }

    /*===========================
       카테고리 배포
    ===========================*/
    private DataResult updateCategoryStatus(Category category, UpdateAdminCategoryData updatedData) {
        int currentStatus = category.getStatus();
        int newStatusValue = updatedData.getCategoryStatus();
        DataStatus newStatus = DataStatus.fromValue(newStatusValue);

        if(newStatus == null){
            return DataResult.INVALID_STATUS;
        }

        if(currentStatus == newStatusValue){
            switch (newStatus) {
                case DEPLOYED:
                    return DataResult.ALREADY_DEPLOYED;
                case PREPARE:
                    return DataResult.ALREADY_NEUTRALIZED;
                case DELETE:
                    return DataResult.ALREADY_DELETED;
            }
        }
        switch (newStatus) {
            case DEPLOYED:
                if(currentStatus != DataStatus.PREPARE.getValue()){
                    return DataResult.NOT_PREPARED;
                }
                if(categoryRepo.existsByTitleAndStatus(updatedData.getCategoryTitle(), currentStatus)){
                    return DataResult.SUCCESSFULLY_DEPLOY;
                }
            case PREPARE:
                return DataResult.SUCCESSFULLY_NEUTRALIZED;
            case DELETE:
                if(currentStatus != DataStatus.PREPARE.getValue()){
                    return DataResult.NOT_PREPARED;
                }
                return DataResult.SUCCESSFULLY_DELETED;
            default:
                return DataResult.INVALID_STATUS;
        }
    }
}
