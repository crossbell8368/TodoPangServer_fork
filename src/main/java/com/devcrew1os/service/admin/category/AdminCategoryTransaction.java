package com.devcrew1os.service.admin.category;

import com.devcrew1os.dto.admin.category.UpdateAdminCategoryData;
import com.devcrew1os.entity.admin.challenge.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryTransaction {

    private final AdminCategoryRepository categoryRepo;

    @Transactional
    public void saveCategories(List<AdminCategory> categories) {
        categoryRepo.saveAll(categories);
    }

    public List<AdminCategory> getCategoryByIdList(List<Integer> idList) {
        List<AdminCategory> categories = categoryRepo.findAllById(idList);

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
    public void updateCategory(List<AdminCategory> entities, Map<Integer, UpdateAdminCategoryData> updated) {
        for(AdminCategory entity : entities) {
            UpdateAdminCategoryData updatedData = updated.get(entity.getId());

            if (updatedData == null) {
                throw new IllegalArgumentException("[Error] No update data found for ID: " + entity.getId());
            }
            entity.setStatus(updatedData.getUpdatedCategoryStatus());
            entity.setTitle(updatedData.getUpdatedCategoryTitle());
        }
    }
}
