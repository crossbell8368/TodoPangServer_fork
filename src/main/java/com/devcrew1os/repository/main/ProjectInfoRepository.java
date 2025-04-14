package com.devcrew1os.repository.main;

import com.devcrew1os.entity.main.project.ProjectInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInfoRepository extends JpaRepository<ProjectInfo, Integer> {
    Optional<ProjectInfo> findByUserId(String userId);
}
