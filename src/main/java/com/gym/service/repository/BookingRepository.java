package com.gym.service.repository;

import com.gym.service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdAndClassIdAndStatus(Long userId, Long classId, String status);
    
    List<Booking> findByClassesId(Long classesId);

    void deleteByClassId(Long classId);

    void deleteByUserId(Long userId);

    @Query("SELECT b.user.id, COUNT(b) FROM bookings b WHERE b.status = 'CONFIRMED' GROUP BY b.user.id ORDER BY COUNT(b) DESC")
    List<Object[]> countBookingsByUser();
    
    @Query("SELECT b.user.id, COUNT(b) FROM bookings b WHERE b.status = 'CONFIRMED' AND b.bookingTime >= :startTime GROUP BY b.user.id ORDER BY COUNT(b) DESC")
    List<Object[]> countBookingsByUserSince(@Param("startTime") LocalDateTime startTime);
}
