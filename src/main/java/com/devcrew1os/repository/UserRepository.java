package com.devcrew1os.repository;

import com.devcrew1os.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByUserId(String userId);
    Optional<Users> findByUserId(String userId);
}