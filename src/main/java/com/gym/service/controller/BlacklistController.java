package com.gym.service.controller;

import com.gym.service.entity.Blacklist;
import com.gym.service.repository.BlacklistRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBlacklist() {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")).getId();

            List<Blacklist> blacklist = blacklistRepository.findByUserId(userId);
            response.put("success", true);
            response.put("data", blacklist);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting blacklist: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addToBlacklist(@RequestBody Map<String, Long> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")).getId();
            Long blockedUserId = request.get("blockedUserId");

            // 检查是否已经拉黑
            if (blacklistRepository.findByUserIdAndBlockedUserId(userId, blockedUserId).isPresent()) {
                response.put("success", false);
                response.put("message", "已经拉黑该用户");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            String blockedUsername = userRepository.findById(blockedUserId).orElse(null).getUsername();

            Blacklist blacklist = new Blacklist();
            blacklist.setUserId(userId);
            blacklist.setBlockedUserId(blockedUserId);
            blacklist.setBlockedUsername(blockedUsername);
            blacklist.setCreatedAt(LocalDateTime.now());
            blacklistRepository.save(blacklist);

            response.put("success", true);
            response.put("message", "成功拉黑用户");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error adding to blacklist: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeFromBlacklist(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")).getId();

            Blacklist blacklist = blacklistRepository.findById(id).orElse(null);
            if (blacklist == null || !blacklist.getUserId().equals(userId)) {
                response.put("success", false);
                response.put("message", "记录不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            blacklistRepository.delete(blacklist);

            response.put("success", true);
            response.put("message", "成功取消拉黑");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error removing from blacklist: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
