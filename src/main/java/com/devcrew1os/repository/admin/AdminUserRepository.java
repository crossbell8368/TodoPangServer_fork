package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    boolean existsAdminUserById(String id);
    List<AdminUser> findAllByIdIn(List<String> ids);
}
