package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, String> {

    Optional<AdminUser> findByUserId(String userId);

    List<AdminUser> findAllByUserIdIn(List<String> userIds);

    @Query("SELECT au FROM AdminUser au WHERE au.email = :email AND au.status = :status")
    Optional<AdminUser> findByUserEmailAndStatus(@Param("email") String email, @Param("status") int status);

}
