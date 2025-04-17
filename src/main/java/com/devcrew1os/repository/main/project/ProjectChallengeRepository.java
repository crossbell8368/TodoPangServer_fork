package com.devcrew1os.repository.main.project;

import com.devcrew1os.entity.main.project.ProjectChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectChallengeRepository extends JpaRepository<ProjectChallenge, Integer> {
    List<ProjectChallenge> findAllByProjectId(Integer projectId);
}
