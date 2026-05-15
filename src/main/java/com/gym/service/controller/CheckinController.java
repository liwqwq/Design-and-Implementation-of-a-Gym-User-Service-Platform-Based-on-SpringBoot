package com.gym.service.controller;

import com.gym.service.entity.CheckinRecord;
import com.gym.service.entity.User;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.CheckinService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }
        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> checkin(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            CheckinRecord record = checkinService.checkin(userId);
            
            response.put("success", true);
            response.put("record", record);
            response.put("message", "打卡成功");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCheckinStatus(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            Map<String, Object> status = checkinService.getCheckinStatus(userId);
            
            response.put("success", true);
            response.put("data", status);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getCheckinRecords(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            List<CheckinRecord> records = checkinService.getCheckinRecords(userId);
            
            response.put("success", true);
            response.put("records", records);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/records/month")
    public ResponseEntity<Map<String, Object>> getCheckinRecordsByMonth(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            List<CheckinRecord> records = checkinService.getCheckinRecordsByMonth(userId, year, month);
            
            response.put("success", true);
            response.put("records", records);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 门店扫码到店：请求体 {@code { "token": "<JWT>" }}，与会员个人中心二维码内容一致。
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanByQr(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = body != null ? body.get("token") : null;
            if (token == null || token.isEmpty()) {
                response.put("success", false);
                response.put("message", "缺少 token");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            Long userId = jwtUtil.parseCheckinQrUserId(token);
            if (!userRepository.existsById(userId)) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            CheckinRecord record = checkinService.recordGymScan(userId);
            response.put("success", true);
            response.put("message", "到店确认成功");
            response.put("data", record);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
