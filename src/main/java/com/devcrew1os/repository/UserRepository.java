package com.devcrew1os.repository;

import com.devcrew1os.entity.user.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {
    boolean existsByUserId(String userId);
    Optional<UserInfo> findByUserId(String userId);
}