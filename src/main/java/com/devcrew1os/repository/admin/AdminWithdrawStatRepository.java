package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.auth.AdminWithdrawStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminWithdrawStatRepository extends JpaRepository<AdminWithdrawStat, String> {
    Optional<AdminWithdrawStat> findByUserId(String userId);
}
