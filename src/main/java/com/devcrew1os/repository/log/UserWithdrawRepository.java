package com.devcrew1os.repository.log;

import com.devcrew1os.entity.log.UserWithdrawLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWithdrawRepository extends JpaRepository<UserWithdrawLog, Integer> {}
