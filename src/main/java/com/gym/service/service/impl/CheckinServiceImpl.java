package com.gym.service.service.impl;

import com.gym.service.entity.CheckinRecord;
import com.gym.service.entity.Points;
import com.gym.service.repository.CheckinRecordRepository;
import com.gym.service.repository.PointsRepository;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int DAILY_POINTS = 10;

    @Override
    @Transactional
    public CheckinRecord checkin(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查是否已经打卡
        if (checkinRecordRepository.findByUserIdAndCheckinDate(userId, today).isPresent()) {
            throw new RuntimeException("今日已打卡");
        }

        // 先写入今日记录，再计算新的连续打卡天数。
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setCheckinTime(LocalDateTime.now());
        record.setPointsEarned(DAILY_POINTS);

        CheckinRecord savedRecord = checkinRecordRepository.save(record);

        int consecutiveDays = calculateConsecutiveDays(userId);
        int earnedPoints = DAILY_POINTS + calculateStreakBonus(consecutiveDays);
        savedRecord.setPointsEarned(earnedPoints);
        savedRecord = checkinRecordRepository.save(savedRecord);

        // 更新积分，真实加上本次获得的每日积分和连续奖励。
        updatePoints(userId, earnedPoints, consecutiveDays);

        return savedRecord;
    }

    @Override
    @Transactional
    public CheckinRecord recordGymScan(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<CheckinRecord> existing = checkinRecordRepository.findByUserIdAndCheckinDate(userId, today);
        if (existing.isPresent()) {
            CheckinRecord r = existing.get();
            r.setCheckinTime(LocalDateTime.now());
            return checkinRecordRepository.save(r);
        }
        return checkin(userId);
    }

    private void updatePoints(Long userId, int earnedPoints, int consecutiveDays) {
        Points points = pointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Points newPoints = new Points();
                    newPoints.setUserId(userId);
                    newPoints.setTotalPoints(0);
                    newPoints.setAvailablePoints(0);
                    newPoints.setUsedPoints(0);
                    newPoints.setConsecutiveDays(0);
                    newPoints.setCreatedAt(LocalDateTime.now());
                    return newPoints;
                });

        points.setTotalPoints(points.getTotalPoints() + earnedPoints);
        points.setAvailablePoints(points.getAvailablePoints() + earnedPoints);
        points.setLastCheckinDate(LocalDateTime.now());
        points.setConsecutiveDays(consecutiveDays);
        points.setUpdatedAt(LocalDateTime.now());

        pointsRepository.save(points);
    }

    private int calculateStreakBonus(int consecutiveDays) {
        if (consecutiveDays == 30) {
            return 200;
        }
        if (consecutiveDays == 7) {
            return 50;
        }
        return 0;
    }

    @Override
    public Map<String, Object> getCheckinStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 检查今日是否已打卡
        boolean hasCheckedInToday = checkinRecordRepository
                .findByUserIdAndCheckinDate(userId, today)
                .isPresent();

        // 获取连续打卡天数
        int consecutiveDays = calculateConsecutiveDays(userId);
        
        // 获取总打卡天数
        long totalCheckins = getTotalCheckins(userId);

        // 获取积分信息
        Points points = pointsRepository.findByUserId(userId).orElse(null);
        int availablePoints = points != null ? points.getAvailablePoints() : 0;

        status.put("hasCheckedInToday", hasCheckedInToday);
        status.put("consecutiveDays", consecutiveDays);
        status.put("totalCheckins", totalCheckins);
        status.put("availablePoints", availablePoints);
        status.put("today", today.toString());

        return status;
    }

    @Override
    public List<CheckinRecord> getCheckinRecords(Long userId) {
        return checkinRecordRepository.findByUserIdOrderByCheckinDateDesc(userId);
    }

    @Override
    public List<CheckinRecord> getCheckinRecordsByMonth(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return checkinRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public int getConsecutiveDays(Long userId) {
        return calculateConsecutiveDays(userId);
    }

    @Override
    public long getTotalCheckins(Long userId) {
        return checkinRecordRepository.countByUserId(userId);
    }

    private int calculateConsecutiveDays(Long userId) {
        LocalDate today = LocalDate.now();
        boolean checkedToday = checkinRecordRepository
                .findByUserIdAndCheckinDate(userId, today)
                .isPresent();

        // 如果今天还没有打卡，展示截至昨天为止的连续天数；
        // 今天打卡后，则展示包含今天在内的连续天数。
        LocalDate startDate = checkedToday ? today : today.minusDays(1);
        int consecutiveDays = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = startDate.minusDays(i);
            boolean checkedIn = checkinRecordRepository
                    .findByUserIdAndCheckinDate(userId, checkDate)
                    .isPresent();

            if (checkedIn) {
                consecutiveDays++;
            } else {
                break;
            }
        }

        return consecutiveDays;
    }
}
