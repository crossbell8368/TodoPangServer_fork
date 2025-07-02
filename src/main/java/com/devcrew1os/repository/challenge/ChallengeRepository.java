package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.repository.projection.AdminChallengeProjection;
import com.devcrew1os.repository.projection.ChallengeCardProjection;
import com.devcrew1os.repository.projection.ChallengeDetailProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    @Modifying
    @Query("UPDATE Challenge ch " +
            "SET ch.status = :status, ch.updatedBy = :adminId, ch.updatedAt = :now " +
            "WHERE ch.id IN :ids")
    void updateStatusAndAuditByIds(
            @Param("status") int status,
            @Param("adminId") String adminId,
            @Param("now") LocalDateTime now,
            @Param("ids") List<Integer> ids
    );

    @Query("SELECT count(ch) > 0 FROM Challenge ch " +
            "WHERE ch.id = :challengeId " +
            "AND ch.status = :status")
    boolean existsChallengeByIdAndStatus(
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query("SELECT ch.title FROM Challenge ch " +
            "WHERE ch.title IN :title " +
            "AND ch.status = :status")
    Set<String> findExistTitleByTitlesAndStatus(
            @Param("title") List<String> title,
            @Param("status") int status
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "WHERE ch.id = :challengeId")
    Optional<Challenge> findChallengeByIdWithStat(
            @Param("challengeId") int challengeId
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "WHERE ch.id IN :challengeIds")
    List<Challenge> findAllByChallengeIds(
            @Param("challengeIds") Set<Integer> challengeIds
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.stat st " +
            "WHERE ch.id IN :challengeIds AND ch.status = :status")
    List<Challenge> findAllByChallengeIdsAndStatus(
            @Param("challengeIds") List<Integer> challengeIds,
            @Param("status") int status
    );

    @Query("SELECT ch FROM Challenge ch " +
            "JOIN FETCH ch.category cat " +
            "JOIN FETCH ch.stat st " +
            "WHERE cat.status = :status " +
            "AND ch.status = :status ORDER BY ch.id DESC")
    List<Challenge> findAllByStatusDesc(
            @Param("status") int status
    );

    @Query("SELECT ch FROM Challenge ch " +
            "WHERE ch.status <> :status")
    List<Challenge> findAllByStatus(
            @Param("status") int status
    );

    @Query(value = "SELECT " +
            "ch.id as id, cat.id as categoryId, ch.title as title, " +
            "ch.diff as diff, st.participateUserCount as popularity " +
            "FROM Challenge ch " +
            "LEFT JOIN ch.category cat " +
            "LEFT JOIN ch.stat st " +
            "WHERE ch.status = :status AND cat.status = :status")
    List<ChallengeCardProjection> findAllCardProjByStatus(
            @Param("status") int status
    );

    @Query(value = "SELECT " +
            "ch.id as id, cat.id as categoryId, ch.title as title, " +
            "ch.diff as diff, ch.term as term, st.participateUserCount as popularity " +
            "FROM Challenge ch " +
            "LEFT JOIN ch.category cat " +
            "LEFT JOIN ch.stat st " +
            "WHERE ch.id = :challengeId " +
            "AND ch.status = :status " +
            "AND cat.status = :status")
    ChallengeDetailProjection findAllDetailProjByStatus(
            @Param("challengeId") int challengeId,
            @Param("status") int status
    );

    @Query(value = "SELECT " +
            "ch.id as id, cat.id as categoryId, ch.title as title, ch.term as term, " +
            "ch.diff as diff, ch.status as status, ch.updatedAt as updatedAt, " +
            "u.userName as updatedBy, st.includeTodoCount as includeTodoCount " +
            "FROM Challenge ch " +
            "LEFT JOIN ch.category cat " +
            "LEFT JOIN ch.stat st " +
            "LEFT JOIN Users u ON ch.updatedBy = u.userId",
            countQuery = "SELECT count(ch) FROM Challenge ch")
    Page<AdminChallengeProjection> findAllByPageable(Pageable pageable);
}
