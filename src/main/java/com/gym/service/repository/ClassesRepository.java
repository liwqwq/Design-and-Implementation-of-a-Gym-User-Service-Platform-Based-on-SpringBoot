package com.gym.service.repository;

import com.gym.service.entity.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassesRepository extends JpaRepository<Classes, Long> {
    List<Classes> findByCoachId(Long coachId);
}
