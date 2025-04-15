package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeInfoRepository extends JpaRepository<ChallengeInfo, Integer> {
    List<ChallengeInfo> findAllByOrderByIdDesc();
    boolean existsChallengeInfoById(Integer id);
}
