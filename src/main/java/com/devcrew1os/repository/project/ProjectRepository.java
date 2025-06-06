package com.devcrew1os.repository.project;

import com.devcrew1os.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("SELECT p FROM Project p WHERE p.users.userId = :userId")
    Optional<Project> findByUserId(@Param("userId") String userId);
}
