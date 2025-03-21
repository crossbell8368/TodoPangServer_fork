package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.main.user.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, String> {
    Optional<UserStat> findByUserId(String userId);
}
