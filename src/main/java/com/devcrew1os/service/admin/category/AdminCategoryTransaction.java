package com.devcrew1os.service.admin.category;

import com.devcrew1os.dto.admin.category.GetAdminCategoryData;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryData;
import com.devcrew1os.dto.admin.category.UpdateAdminCategoryReq;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.entity.admin.challenge.AdminCategoryInfo;
import com.devcrew1os.repository.admin.AdminCategoryInfoRepository;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryTransaction {

    private final AdminUserRepository adminUserRepo;
    private final AdminCategoryInfoRepository categoryInfoRepo;

    /*===========================
       카테고리 목록
    ===========================*/
    public List<GetAdminCategoryData> getAdminCategoryData() {
        return getCategoryInfoList().stream()
                .map(info -> new GetAdminCategoryData(
                        info.getId(),
                        info.getStatus(),
                        info.getTitle(),
                        info.getStat().getInvolvedCount(),
                        info.getLastUpdatedBy(),
                        info.getLastUpdatedAt()
                )).collect(Collectors.toList());
    }

    public List<AdminCategoryInfo> getCategoryInfoList() {
        List<AdminCategoryInfo> categoryList = categoryInfoRepo.findAllByOrderById();
        if(categoryList.isEmpty()) {
            throw new RuntimeException("Category info list is empty");
        } else {
            return categoryList;
        }
    }

    /*===========================
       카테고리 추가
    ===========================*/
    public AdminUser getAdminData(String adminId) {
        return adminUserRepo.findByUserId(adminId).orElseThrow(
                () -> new RuntimeException("Admin not found")
        );
    }

    @Transactional
    public void saveCategories(List<AdminCategoryInfo> categories) {
        categoryInfoRepo.saveAll(categories);
    }

    /*===========================
       카테고리 변경
    ===========================*/
    public List<AdminCategoryInfo> getCategoryByIdList(List<UpdateAdminCategoryData> data) {
        // extract IdList
        List<Integer> idList = data.stream()
                .map(UpdateAdminCategoryData::getCategoryId)
                .collect(Collectors.toList());

        // search data
        List<AdminCategoryInfo> categories = categoryInfoRepo.findAllById(idList);

        // exception handling
        if (categories.size() != idList.size()) {
            List<Integer> foundIds = categories.stream()
                    .map(AdminCategoryInfo::getId)
                    .collect(Collectors.toList());

            List<Integer> missingIds = idList.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());

            throw new RuntimeException("Categories not found: " + missingIds);
        }
        return categories;
    }

    @Transactional
    public void updateCategory(List<AdminCategoryInfo> entities, UpdateAdminCategoryReq req) {
        // prepare data
        Map<Integer, UpdateAdminCategoryData> updated = req.getUpdatedCategories().stream()
                .collect(Collectors.toMap(
                        UpdateAdminCategoryData::getCategoryId, data -> data));

        // process update
        for(AdminCategoryInfo entity : entities) {
            UpdateAdminCategoryData updatedData = updated.get(entity.getId());

            if (updatedData == null) {
                throw new IllegalArgumentException("[Error] No update data found for ID: " + entity.getId());
            }
            entity.setStatus(updatedData.getCategoryStatus());
            entity.setTitle(updatedData.getCategoryTitle());
        }
    }
}
