package com.devcrew1os.repository.users;

import com.devcrew1os.entity.user.UserWithdrawReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWithdrawReasonRepository extends JpaRepository<UserWithdrawReason, Integer> {

    @Query("SELECT uwr FROM UserWithdrawReason uwr WHERE uwr.status = :status")
    List<UserWithdrawReason> findAllByStatus(@Param("status") int status);
}
