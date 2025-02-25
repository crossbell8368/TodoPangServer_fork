package com.devcrew1os.repository;

import com.devcrew1os.entity.Stat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface StatRepository extends JpaRepository<Stat, String> {
    Optional<Stat> findByUserId(String userId);

    @Transactional
    @Modifying
    @Query("UPDATE Stat s SET s.statUserStatus = :status WHERE s.userId = :userId")
    int updateStatUserStatus(@Param("status") int status, @Param("userId") String userId);
}
