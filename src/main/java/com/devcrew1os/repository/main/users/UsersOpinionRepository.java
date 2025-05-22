package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.user.UsersOpinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersOpinionRepository extends JpaRepository<UsersOpinion, Integer> {

    @Query("SELECT uo FROM UsersOpinion uo JOIN FETCH uo.user " +
            "WHERE uo.user.userId = :userId " +
            "AND uo.lastRegisteredAt = (SELECT MAX(uo2.lastRegisteredAt) FROM UsersOpinion uo2 WHERE uo2.user.userId = :userId) ")
    Optional<UsersOpinion> findLastRegisteredByUserId(@Param("userId") String userId);
}
