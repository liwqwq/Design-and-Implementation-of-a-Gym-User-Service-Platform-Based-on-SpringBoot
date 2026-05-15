package com.gym.service.repository;

import com.gym.service.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findByAvailableTrue();
    List<Facility> findByNameContaining(String name);
}
