package com.gym.service.service;

import com.gym.service.entity.UserRanking;
import java.util.List;
import java.util.Map;

public interface RankingService {
    List<UserRanking> getTotalRanking();
    List<UserRanking> getWeekRanking();
    List<UserRanking> getMonthRanking();
    Map<String, Object> getUserRankingInfo(Long userId);
    Map<String, Object> getUserRankingInfo(Long userId, String period);
}
