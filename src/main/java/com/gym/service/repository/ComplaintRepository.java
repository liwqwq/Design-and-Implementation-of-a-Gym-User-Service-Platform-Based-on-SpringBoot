package com.gym.service.repository;

import com.gym.service.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCoachIdOrderByCreatedAtDesc(Long coachId);
    List<Complaint> findByCoachIdAndStatusOrderByCreatedAtDesc(Long coachId, String status);
}
