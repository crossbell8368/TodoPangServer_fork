package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.challenge.AdminCategoryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminCategoryInfoRepository extends JpaRepository<AdminCategoryInfo, Integer> {
    List<AdminCategoryInfo> findAllByOrderById();
    Optional<AdminCategoryInfo> findById(int id);
}
