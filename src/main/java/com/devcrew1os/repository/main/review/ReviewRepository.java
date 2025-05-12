package com.devcrew1os.repository.main.review;

import com.devcrew1os.entity.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT rv FROM Review rv WHERE rv.status = :status")
    List<Review> findAllByStatus(@Param("status") int status);
}
