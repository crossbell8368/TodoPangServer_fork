package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.log.UserProjectActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLogRepository extends JpaRepository<UserProjectActionLog, Integer> {
    List<UserProjectActionLog> findAllByUserId(String userId);
}
