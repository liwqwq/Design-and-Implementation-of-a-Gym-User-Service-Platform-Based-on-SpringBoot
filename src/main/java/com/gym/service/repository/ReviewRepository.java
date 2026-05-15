package com.gym.service.repository;

import com.gym.service.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCoachIdOrderByCreatedAtDesc(Long coachId);
    List<Review> findByCoachIdAndRespondedOrderByCreatedAtDesc(Long coachId, Boolean responded);
}
