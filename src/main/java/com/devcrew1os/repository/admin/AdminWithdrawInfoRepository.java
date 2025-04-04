package com.devcrew1os.repository.admin;

import com.devcrew1os.entity.admin.auth.AdminWithdrawInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminWithdrawInfoRepository extends JpaRepository<AdminWithdrawInfo, Long> {
    List<AdminWithdrawInfo> findAllByOrderByIdDesc();
    Optional<AdminWithdrawInfo> findById(Integer id);
}
