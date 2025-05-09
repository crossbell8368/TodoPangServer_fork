package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.user.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, String> {
    Optional<UserStat> findByUserId(String userId);

    boolean existsByUserId(String userId);

    @Modifying
    @Query("UPDATE UserStat us SET us.serviceTerm = us.serviceTerm + 1, us.lastLoginAt = :current WHERE us.userId = :userId")
    void updateTermAndLastLogin(@Param("userId") String userId, @Param("current")LocalDateTime current);

    @Modifying
    @Query("UPDATE UserStat us SET us.registeredChallenges = us.registeredChallenges + 1 WHERE us.userId = :userId")
    void updateRegisterChallenges(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserStat us SET us.completedChallenges = us.completedChallenges + 1 WHERE us.userId = :userId")
    void updateCompleteChallenges(@Param("userId") String userId);
}
