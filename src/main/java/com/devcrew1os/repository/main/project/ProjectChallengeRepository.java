package com.devcrew1os.repository.main.project;

import com.devcrew1os.entity.main.project.ProjectChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProjectChallengeRepository extends JpaRepository<ProjectChallenge, Integer> {
    List<ProjectChallenge> findAllByProjectId(Integer projectId);
    List<ProjectChallenge> findAllByIdIn(Set<Integer> projectIds);
}
