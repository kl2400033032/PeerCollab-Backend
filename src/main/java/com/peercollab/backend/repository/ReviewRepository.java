package com.peercollab.backend.repository;

import com.peercollab.backend.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    long countByReviewerId(Long reviewerId);

    boolean existsByProjectIdAndReviewerId(Long projectId, Long reviewerId);
}
