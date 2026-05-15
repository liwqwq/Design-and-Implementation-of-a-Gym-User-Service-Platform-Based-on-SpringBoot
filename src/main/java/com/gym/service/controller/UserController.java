package com.gym.service.controller;

import com.gym.service.entity.User;
import com.gym.service.entity.Membership;
import com.gym.service.service.UserService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            User createdUser = userService.createUser(user);
            response.put("success", true);
            response.put("data", createdUser);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            response.put("success", true);
            response.put("data", users);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            response.put("success", true);
            response.put("data", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting profile: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** 会员个人中心展示「入场二维码」载荷（JWT），门店调用 POST /api/checkin/scan 核销 */
    @GetMapping("/user/checkin-qr-token")
    public ResponseEntity<Map<String, Object>> getCheckinQrToken() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            String token = jwtUtil.generateCheckinQrToken(user.getId());
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("token", token);
            response.put("success", true);
            response.put("data", data);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody User userDetails) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            User updatedUser = userService.updateUser(user.getId(), userDetails);
            response.put("success", true);
            response.put("data", updatedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/user/membership/purchase-vip")
    public ResponseEntity<Map<String, Object>> purchaseVipMembership(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            String plan = body.get("plan") != null ? String.valueOf(body.get("plan")) : "yearly";
            String paymentMethod = body.get("paymentMethod") != null ? String.valueOf(body.get("paymentMethod")) : "demo";

            Membership membership = userService.upgradeToVipMembership(username, plan);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("membership", membership);
            data.put("plan", plan);
            data.put("paymentMethod", paymentMethod);
            data.put("orderNo", "VIP" + System.currentTimeMillis());
            if ("monthly".equalsIgnoreCase(plan)) {
                data.put("amount", 99);
            } else if ("quarterly".equalsIgnoreCase(plan)) {
                data.put("amount", 259);
            } else {
                data.put("amount", 699);
            }

            response.put("success", true);
            response.put("message", "VIP购买成功");
            response.put("data", data);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "购买VIP失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/membership")
    public ResponseEntity<Map<String, Object>> getMembership() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Current username: " + username);
            java.util.Optional<Membership> membershipOptional = userService.getMembershipByUsername(username);
            Membership membership;
            if (membershipOptional.isPresent()) {
                membership = membershipOptional.get();
                System.out.println("Membership found: " + membership);
            } else {
                // 返回默认会员对象
                membership = new Membership();
                membership.setId(1L);
                membership.setUserId(4L);
                membership.setMembershipType("标准会员");
                membership.setStartDate(java.time.LocalDate.now());
                membership.setEndDate(java.time.LocalDate.now().plusMonths(3));
                membership.setStatus("ACTIVE");
                membership.setTotalSessions(30);
                membership.setUsedSessions(5);
                membership.setRemainingSessions(25);
                membership.setDaysRemaining(75);
                System.out.println("Membership not found, returning default membership");
            }
            response.put("success", true);
            response.put("data", membership);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting membership: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}