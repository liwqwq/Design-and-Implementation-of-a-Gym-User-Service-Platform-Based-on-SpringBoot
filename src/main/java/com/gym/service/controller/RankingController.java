package com.gym.service.controller;

import com.gym.service.entity.User;
import com.gym.service.entity.UserRanking;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.RankingService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null ? user.getId() : null;
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalRanking(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserRanking> rankings = rankingService.getTotalRanking();
            result.put("success", true);
            result.put("data", rankings);
            
            Long userId = getUserIdFromToken(authHeader);
            if (userId != null) {
                Map<String, Object> userInfo = rankingService.getUserRankingInfo(userId, "total");
                result.put("myRanking", userInfo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/week")
    public ResponseEntity<Map<String, Object>> getWeekRanking(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserRanking> rankings = rankingService.getWeekRanking();
            result.put("success", true);
            result.put("data", rankings);
            
            Long userId = getUserIdFromToken(authHeader);
            if (userId != null) {
                Map<String, Object> userInfo = rankingService.getUserRankingInfo(userId, "week");
                result.put("myRanking", userInfo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/month")
    public ResponseEntity<Map<String, Object>> getMonthRanking(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserRanking> rankings = rankingService.getMonthRanking();
            result.put("success", true);
            result.put("data", rankings);
            
            Long userId = getUserIdFromToken(authHeader);
            if (userId != null) {
                Map<String, Object> userInfo = rankingService.getUserRankingInfo(userId, "month");
                result.put("myRanking", userInfo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
