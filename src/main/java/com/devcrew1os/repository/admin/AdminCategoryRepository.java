package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.challenge.AdminCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminCategoryRepository extends JpaRepository<AdminCategory, Integer> {
    List<AdminCategory> findAllByOrderByIdDesc();
    Optional<AdminCategory> findById(int id);
}
