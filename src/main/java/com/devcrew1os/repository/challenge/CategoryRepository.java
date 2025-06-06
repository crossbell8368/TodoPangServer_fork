package com.devcrew1os.repository.challenge;

import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.repository.projection.CategoryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT count(cat) > 0 FROM Category cat WHERE cat.title = :title AND cat.status = :status")
    boolean existsByTitleAndStatus(@Param("title")String title, @Param("status") int status);

    @Query("SELECT count(cat) > 0 FROM Category cat WHERE cat.id = :categoryId AND cat.status = :status")
    boolean existsByIdAndStatus(@Param("categoryId") int categoryId, @Param("status") int status);

    List<Category> findAllByIdIn(Set<Integer> ids);

    @Query("SELECT ct FROM Category ct WHERE ct.status = :status ORDER BY ct.id DESC")
    List<Category> findAllByStatusDesc(@Param("status") Integer status);

    @Query("SELECT c.id as id, c.title as title, c.status as status, c.updatedAt as updatedAt, c.updatedBy as updatedBy, count(ch.id) as challengeCount FROM Category c LEFT JOIN Challenge ch ON ch.category = c GROUP BY c.id, c.title, c.status, c.updatedAt, c.updatedBy, c.updatedBy ORDER BY c.id ASC")
    List<CategoryProjection> findAllWithChallengeCount();
}
