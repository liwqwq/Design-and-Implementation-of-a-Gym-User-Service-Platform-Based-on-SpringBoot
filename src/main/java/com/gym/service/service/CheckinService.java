package com.gym.service.service;

import com.gym.service.entity.CheckinRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckinService {
    CheckinRecord checkin(Long userId);

    /** 门店扫码：若当日尚未打卡则等同打卡；若已打卡则刷新打卡时间用于场内活跃统计 */
    CheckinRecord recordGymScan(Long userId);
    Map<String, Object> getCheckinStatus(Long userId);
    List<CheckinRecord> getCheckinRecords(Long userId);
    List<CheckinRecord> getCheckinRecordsByMonth(Long userId, int year, int month);
    int getConsecutiveDays(Long userId);
    long getTotalCheckins(Long userId);
}
