package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    boolean existsAdminUserByUserId(String userId);
    Optional<AdminUser> findByUserId(String userId);
}
