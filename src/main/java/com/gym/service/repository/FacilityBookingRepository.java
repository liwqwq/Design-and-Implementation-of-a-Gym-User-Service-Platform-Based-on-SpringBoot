package com.gym.service.repository;

import com.gym.service.entity.FacilityBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacilityBookingRepository extends JpaRepository<FacilityBooking, Long> {
    List<FacilityBooking> findByCoachIdOrderByStartTimeDesc(Long coachId);
    List<FacilityBooking> findByFacilityIdAndStatus(Long facilityId, String status);
    List<FacilityBooking> findByFacilityIdAndStartTimeBetween(Long facilityId, LocalDateTime start, LocalDateTime end);
}
