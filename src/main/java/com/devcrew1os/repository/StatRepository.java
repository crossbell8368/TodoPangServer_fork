package com.devcrew1os.repository;

import com.devcrew1os.entity.Stat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatRepository extends JpaRepository<Stat, String> {
    Optional<Stat> findByUserId(String userId);
}
