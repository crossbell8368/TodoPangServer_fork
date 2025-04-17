package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.AdminChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminChallengeRepository extends JpaRepository<AdminChallenge, Integer> {
    List<AdminChallenge> findAllByOrderByIdDesc();
}
