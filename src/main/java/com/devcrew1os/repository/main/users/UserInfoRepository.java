package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.main.user.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    boolean existsUserInfoByUserId(String userId);
    Optional<UserInfo> findByUserId(String userId);
}