package com.devcrew1os.repository.main.users;

import com.devcrew1os.entity.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    boolean existsUsersByUserId(String userId);
    Optional<Users> findByUserId(String userId);

    @Query("SELECT u FROM Users u JOIN FETCH u.project p WHERE u.userId = :userId")
    Optional<Users> findUsersAndProjectByUserId(@Param("userId") String userId);
}