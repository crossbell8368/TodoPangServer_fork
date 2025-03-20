package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.ChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeCategoryRepository extends JpaRepository<ChallengeCategory, Integer> {
    List<ChallengeCategory> findAllByOrderByIdDesc();
}
