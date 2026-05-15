package com.gym.service.controller;

import com.gym.service.entity.Membership;
import com.gym.service.entity.User;
import com.gym.service.repository.MembershipRepository;
import com.gym.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private MembershipRepository membershipRepository;

    private static String deriveLevelKey(String membershipType) {
        if (membershipType == null || membershipType.isEmpty()) {
            return "normal";
        }
        String s = membershipType.toUpperCase();
        if (s.contains("VIP")) {
            return "vip";
        }
        if (s.contains("黄金") || s.contains("GOLD")) {
            return "gold";
        }
        if (s.contains("银") || s.contains("SILVER")) {
            return "silver";
        }
        if (s.contains("青") || s.contains("铜") || s.contains("BRONZE")) {
            return "bronze";
        }
        return "normal";
    }

    private static Map<String, Object> toUserRow(User u, Optional<Membership> memOpt) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole());
        m.put("active", u.isActive());
        m.put("statusKey", u.isActive() ? "active" : "inactive");
        if (memOpt.isPresent()) {
            Membership mm = memOpt.get();
            m.put("membershipType", mm.getMembershipType());
            m.put("levelKey", deriveLevelKey(mm.getMembershipType()));
            m.put("membershipStartDate", mm.getStartDate() != null ? mm.getStartDate().toString() : null);
        } else {
            m.put("membershipType", null);
            m.put("levelKey", "normal");
            m.put("membershipStartDate", null);
        }
        return m;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> rows = new ArrayList<>();
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);

            int totalMembers = 0;
            int activeMembers = 0;
            long newMembers = 0;

            for (User u : users) {
                Optional<Membership> memOpt = membershipRepository.findByUserId(u.getId());
                rows.add(toUserRow(u, memOpt));

                if ("USER".equalsIgnoreCase(u.getRole())) {
                    totalMembers++;
                    if (u.isActive()) {
                        activeMembers++;
                    }
                    if (memOpt.isPresent()) {
                        LocalDate sd = memOpt.get().getStartDate();
                        if (sd != null && !sd.isBefore(firstOfMonth)) {
                            newMembers++;
                        }
                    }
                }
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalMembers", totalMembers);
            stats.put("activeMembers", activeMembers);
            stats.put("newMembers", newMembers);

            response.put("success", true);
            response.put("data", rows);
            response.put("stats", stats);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = body.get("username") != null ? String.valueOf(body.get("username")).trim() : "";
            String password = body.get("password") != null ? String.valueOf(body.get("password")).trim() : "";
            String name = body.get("name") != null ? String.valueOf(body.get("name")).trim() : username;
            String email = body.get("email") != null ? String.valueOf(body.get("email")).trim() : "";
            String phone = body.get("phone") != null ? String.valueOf(body.get("phone")).trim() : "";

            if (username.isEmpty() || password.length() < 6 || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                response.put("success", false);
                response.put("message", "请完整填写用户名、密码、姓名、邮箱和手机号，密码至少6位");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRole("USER");
            user.setActive(body.get("active") == null || Boolean.parseBoolean(String.valueOf(body.get("active"))));

            User saved = userService.createUser(user);

            String membershipType = body.get("membershipType") != null ? String.valueOf(body.get("membershipType")).trim() : "标准会员";
            if (membershipType.isEmpty()) {
                membershipType = "标准会员";
            }
            Membership membership = new Membership();
            LocalDate start = LocalDate.now();
            membership.setUserId(saved.getId());
            membership.setMembershipType(membershipType);
            membership.setStartDate(start);
            membership.setEndDate(start.plusYears(1));
            membership.setStatus("ACTIVE");
            membership.setTotalSessions(30);
            membership.setUsedSessions(0);
            membership.setRemainingSessions(30);
            membership.setDaysRemaining(365);
            Membership savedMembership = membershipRepository.save(membership);

            response.put("success", true);
            response.put("data", toUserRow(saved, Optional.of(savedMembership)));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = userService.patchUser(id, body);
            response.put("success", true);
            response.put("data", updatedUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "User deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
