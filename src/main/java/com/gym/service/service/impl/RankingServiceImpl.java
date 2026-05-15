package com.gym.service.service.impl;

import com.gym.service.entity.Points;
import com.gym.service.entity.User;
import com.gym.service.entity.UserRanking;
import com.gym.service.repository.BookingRepository;
import com.gym.service.repository.CheckinRecordRepository;
import com.gym.service.repository.PointsRepository;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private PointsRepository pointsRepository;

    @Override
    public List<UserRanking> getTotalRanking() {
        List<Object[]> bookingCounts = bookingRepository.countBookingsByUser();
        return buildRankingList(bookingCounts, null);
    }

    @Override
    public List<UserRanking> getWeekRanking() {
        LocalDateTime weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
        List<Object[]> bookingCounts = bookingRepository.countBookingsByUserSince(weekStart);
        return buildRankingList(bookingCounts, weekStart.toLocalDate());
    }

    @Override
    public List<UserRanking> getMonthRanking() {
        LocalDateTime monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        List<Object[]> bookingCounts = bookingRepository.countBookingsByUserSince(monthStart);
        return buildRankingList(bookingCounts, monthStart.toLocalDate());
    }

    @Override
    public Map<String, Object> getUserRankingInfo(Long userId) {
        return getUserRankingInfo(userId, "total");
    }

    @Override
    public Map<String, Object> getUserRankingInfo(Long userId, String period) {
        List<UserRanking> rankingList;
        if ("week".equalsIgnoreCase(period)) {
            rankingList = getWeekRanking();
        } else if ("month".equalsIgnoreCase(period)) {
            rankingList = getMonthRanking();
        } else {
            rankingList = getTotalRanking();
        }
        return buildUserRankingInfo(userId, rankingList);
    }

    private Map<String, Object> buildUserRankingInfo(Long userId, List<UserRanking> rankingList) {
        Map<String, Object> result = new HashMap<>();

        int userRank = -1;
        int userScore = 0;
        int nextScore = 0;
        int prevScore = 0;
        int userCheckinDays = 0;
        int userPoints = 0;
        int neededClassesToNext = 0;

        for (int i = 0; i < rankingList.size(); i++) {
            UserRanking current = rankingList.get(i);
            if (current.getUserId() != null && current.getUserId().equals(userId)) {
                userRank = i + 1;
                userScore = safeInt(current.getTotalClasses());
                userCheckinDays = safeInt(current.getCheckinDays());
                userPoints = safeInt(current.getAvailablePoints());
                if (i > 0) {
                    UserRanking next = rankingList.get(i - 1);
                    nextScore = safeInt(next.getTotalClasses());
                    neededClassesToNext = Math.max(1, nextScore - userScore + 1);
                }
                if (i < rankingList.size() - 1) {
                    prevScore = safeInt(rankingList.get(i + 1).getTotalClasses());
                }
                break;
            }
        }

        if (userRank < 0) {
            userRank = rankingList.size() + 1;
            Points points = pointsRepository.findByUserId(userId).orElse(null);
            userPoints = points != null && points.getAvailablePoints() != null ? points.getAvailablePoints() : 0;
        }

        result.put("rank", userRank);
        result.put("score", userScore);
        result.put("nextScore", nextScore);
        result.put("prevScore", prevScore);
        result.put("checkinDays", userCheckinDays);
        result.put("availablePoints", userPoints);
        result.put("neededClassesToNext", neededClassesToNext);
        result.put("isLeading", userRank == 1 && userScore > 0);

        return result;
    }

    private List<UserRanking> buildRankingList(List<Object[]> bookingCounts, LocalDate checkinStartDate) {
        Map<Long, Integer> bookingCountMap = new HashMap<>();
        if (bookingCounts != null) {
            for (Object[] row : bookingCounts) {
                if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                    continue;
                }
                Long userId = ((Number) row[0]).longValue();
                Integer count = ((Number) row[1]).intValue();
                bookingCountMap.put(userId, count);
            }
        }

        if (bookingCountMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserRanking> rankings = bookingCountMap.entrySet().stream()
                .map(entry -> toRanking(entry.getKey(), entry.getValue(), checkinStartDate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        rankings.sort((a, b) -> {
            int byClasses = Integer.compare(safeInt(b.getTotalClasses()), safeInt(a.getTotalClasses()));
            if (byClasses != 0) return byClasses;

            int byCheckins = Integer.compare(safeInt(b.getCheckinDays()), safeInt(a.getCheckinDays()));
            if (byCheckins != 0) return byCheckins;

            int byPoints = Integer.compare(safeInt(b.getAvailablePoints()), safeInt(a.getAvailablePoints()));
            if (byPoints != 0) return byPoints;

            return Long.compare(a.getUserId() == null ? Long.MAX_VALUE : a.getUserId(),
                    b.getUserId() == null ? Long.MAX_VALUE : b.getUserId());
        });

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    private UserRanking toRanking(Long userId, Integer bookingCount, LocalDate checkinStartDate) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return null;
        }

        User user = userOpt.get();
        LocalDate start = checkinStartDate != null ? checkinStartDate : LocalDate.of(2020, 1, 1);
        long checkinDays = checkinRecordRepository.countByUserIdAndDateAfter(userId, start.minusDays(1));
        Points points = pointsRepository.findByUserId(userId).orElse(null);
        int availablePoints = points != null && points.getAvailablePoints() != null ? points.getAvailablePoints() : 0;

        UserRanking ranking = new UserRanking(
                userId,
                user.getUsername(),
                user.getName(),
                bookingCount != null ? bookingCount : 0,
                (int) checkinDays
        );
        ranking.setAvailablePoints(availablePoints);
        return ranking;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
