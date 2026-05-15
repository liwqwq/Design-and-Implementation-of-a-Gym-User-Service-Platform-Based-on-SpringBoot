package com.gym.service.repository;

import com.gym.service.entity.CheckinRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> {
    
    Optional<CheckinRecord> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);
    
    List<CheckinRecord> findByCheckinTimeAfter(LocalDateTime checkinTime);
    
    List<CheckinRecord> findByUserIdOrderByCheckinDateDesc(Long userId);
    
    @Query("SELECT c FROM CheckinRecord c WHERE c.userId = :userId AND c.checkinDate BETWEEN :startDate AND :endDate ORDER BY c.checkinDate ASC")
    List<CheckinRecord> findByUserIdAndDateRange(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT COUNT(c) FROM CheckinRecord c WHERE c.userId = :userId AND c.checkinDate >= :startDate")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    long countByUserId(Long userId);
}
