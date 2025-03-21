package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Integer> {
    List<ChallengeCategory> findAllByOrderByIdDesc();
}
