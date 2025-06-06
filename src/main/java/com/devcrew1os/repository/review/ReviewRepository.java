package com.devcrew1os.repository.review;

import com.devcrew1os.entity.review.Review;
import com.devcrew1os.repository.projection.AdminReviewProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findById(int id);

    List<Review> findAllByIdIn(Set<Integer> ids);

    @Query("SELECT count(rv) > 0 FROM Review rv " +
            "WHERE rv.title = :title " +
            "AND rv.status = :status")
    boolean existsReviewByTitleAndStatus(
            @Param("title") String title,
            @Param("status") int status
    );

    @Query("SELECT rv FROM Review rv " +
            "WHERE rv.status = :status")
    List<Review> findAllByStatus(@Param("status") int status);

    @Query(value = "SELECT " +
            "r.id as reviewId, " +
            "r.emoji as emoji, " +
            "r.status as status, " +
            "r.title as title, " +
            "u.userName as updatedBy, " +
            "r.updatedAt as updatedAt " +
            "FROM Review r " +
            "LEFT JOIN Users u ON r.updatedBy = u.userId " +
            "ORDER BY r.id DESC",
            countQuery = "SELECT count(r) FROM Review r")
    Page<AdminReviewProjection> findAllWithUserName(Pageable pageable);
}
