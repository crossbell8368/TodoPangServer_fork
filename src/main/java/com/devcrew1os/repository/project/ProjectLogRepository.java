package com.devcrew1os.repository.project;

import com.devcrew1os.entity.log.ProjectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectLogRepository extends JpaRepository<ProjectLog, Integer> {
    List<ProjectLog> findAllByUserId(String userId);
}
