package com.devcrew1os.service.admin.category;

import com.devcrew1os.entity.admin.AdminCategory;
import com.devcrew1os.repository.admin.AdminCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryTransaction {

    private final AdminCategoryRepository categoryRepo;

    @Transactional
    public void saveCategories(List<AdminCategory> categories) {
        categoryRepo.saveAll(categories);
    }

    public AdminCategory getCategoryById(int id) {
        return categoryRepo.findById(id).orElseThrow(
                () -> new RuntimeException("Category <" + id + "> not found")
        );
    }

    @Transactional
    public void updateCategory(AdminCategory category, String newTitle) {
        category.setTitle(newTitle);
        categoryRepo.save(category);
    }
}
