package com.devcrew1os.service.admin.category;

import com.devcrew1os.dto.admin.category.UpdateAdminCategoryData;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryReq;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryTransaction {

    private final AdminUserRepository adminUserRepo;
    private final AdminCategoryRepository categoryRepo;

    /*===========================
       카테고리 목록
    ===========================*/
    public List<AdminCategory> getAdminCategoryList() {
        List<AdminCategory> categoryList = categoryRepo.findAllByOrderById();
        if(categoryList.isEmpty()) {
            throw new RuntimeException("Category info list is empty");
        } else {
            return categoryList;
        }
    }

    /*===========================
       카테고리 추가
    ===========================*/
    public AdminUser getAdminUser(String adminId){
        return adminUserRepo.findById(adminId).orElseThrow(
                () -> new RuntimeException("Admin user not found")
        );
    }

    @Transactional
    public void saveCategories(List<AdminCategory> categories) {
        try {
            categoryRepo.saveAll(categories);
        } catch(Exception err) {
            throw new RuntimeException("Set categories transaction failed, initiate rolling back: " + err.getMessage(), err);
        }
    }

    /*===========================
       카테고리 변경
    ===========================*/
    public List<AdminCategory> getCategoryByIdList(List<UpdateAdminCategoryData> data) {
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

    @Transactional
    public void updateCategory( String adminId, List<AdminCategory> entities, UpdateAdminCategoryReq req) {
        // prepare data
        LocalDateTime updatedAt = LocalDateTime.now();
        String adminName = getAdminUser(adminId).getName();
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
            entity.setLastUpdatedBy(adminName);
        }
    }
}
