package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Integer> {
    List<ChallengeTodo> findAllByChallengeInfoId(int challengeInfoId);
}
