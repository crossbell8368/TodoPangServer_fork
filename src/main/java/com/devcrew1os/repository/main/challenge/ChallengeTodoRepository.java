package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Integer> {
    List<ChallengeTodo> findAllByChallengeInfoId(int challengeInfoId);

    @Query("SELECT t.id FROM ChallengeTodo t WHERE t.challengeInfoId = :challengeId AND t.id IN :todoIds")
    List<Integer> findIdsByChallengeInfoIdAndIdIn(@Param("challengeId") int challengeId, @Param("todoIds") List<Integer> todoIds);
}
