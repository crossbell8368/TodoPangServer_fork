package com.devcrew1os.repository.main.challenge;

import com.devcrew1os.entity.main.challenge.ChallengeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChallengeInfoRepository extends JpaRepository<ChallengeInfo, Integer> {
    List<ChallengeInfo> findAllByOrderByIdDesc();
    boolean existsChallengeInfoById(Integer id);

    @Query("SELECT c.id AS id, c.title AS title FROM ChallengeInfo c WHERE c.id IN :ids")
    List<ChallengeInfoProjection> findAllByIdIn(@Param("ids") List<Integer> ids);
}
