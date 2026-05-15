package com.gym.service.repository;

import com.gym.service.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    List<Advertisement> findByIsActiveTrueAndExpiresAtAfterOrderByPositionAsc(LocalDateTime now);
}
