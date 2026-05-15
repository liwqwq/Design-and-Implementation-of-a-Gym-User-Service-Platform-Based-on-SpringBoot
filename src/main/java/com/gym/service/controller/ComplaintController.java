package com.gym.service.controller;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.entity.Complaint;
import com.gym.service.entity.User;
import com.gym.service.repository.BookingRepository;
import com.gym.service.repository.ClassesRepository;
import com.gym.service.repository.CoachRepository;
import com.gym.service.repository.ComplaintRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createComplaint(@RequestBody Map<String, Object> complaintData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);

            Complaint complaint = new Complaint();
            complaint.setUser(user);

            String type = str(complaintData.get("type"));
            if (type == null) {
                type = "other";
            }
            complaint.setComplaintType(type);

            String coachUsername = str(complaintData.get("coachUsername"));
            Coach targetCoach = coachUsername != null
                    ? coachRepository.findByUsername(coachUsername).orElse(null)
                    : findCoachFromLatestBooking(user);
            if (targetCoach == null) {
                targetCoach = coachRepository.findByUsername("coach_1").orElse(null);
            }
            if (targetCoach != null) {
                complaint.setCoach(targetCoach);
            }

            String content = str(complaintData.get("content"));
            complaint.setContent(content != null ? content : "");
            complaint.setContentEn(str(complaintData.get("contentEn")));

            String title = str(complaintData.get("title"));
            String titleEn = str(complaintData.get("titleEn"));
            if (title == null || title.isEmpty()) {
                title = defaultComplaintTitleZh(type);
                titleEn = defaultComplaintTitleEn(type);
            }
            complaint.setTitle(title);
            complaint.setTitleEn(titleEn);

            complaint.setCreatedAt(LocalDateTime.now());
            complaint.setStatus("pending");

            complaintRepository.save(complaint);

            response.put("success", true);
            response.put("message", "投诉提交成功");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String str(Object o) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return s;
    }

    private Coach findCoachFromLatestBooking(User user) {
        if (user == null) {
            return null;
        }
        return bookingRepository.findByUserId(user.getId()).stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(String.valueOf(b.getStatus())))
                .sorted(Comparator.comparing(Booking::getBookingTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(b -> b.getClassId() == null ? null : classesRepository.findById(b.getClassId()).orElse(null))
                .filter(c -> c != null && c.getCoach() != null)
                .map(Classes::getCoach)
                .findFirst()
                .orElse(null);
    }


    private static String defaultComplaintTitleZh(String type) {
        switch (type) {
            case "coach":
                return "【教练服务】投诉与建议";
            case "facility":
                return "【场地设施】投诉与建议";
            case "course":
                return "【课程安排】投诉与建议";
            default:
                return "【其他】投诉与建议";
        }
    }

    private static String defaultComplaintTitleEn(String type) {
        switch (type) {
            case "coach":
                return "[Coach service] Feedback";
            case "facility":
                return "[Facility] Feedback";
            case "course":
                return "[Scheduling] Feedback";
            default:
                return "[Other] Feedback";
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getComplaints() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Complaint> complaints = complaintRepository.findAll();
            response.put("success", true);
            response.put("data", complaints);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取投诉列表失败");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserComplaints() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            
            List<Complaint> complaints = complaintRepository.findAll().stream()
                    .filter(c -> c.getUser() != null && user != null && c.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
            response.put("success", true);
            response.put("data", complaints);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取投诉列表失败");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getComplaintById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return complaintRepository.findById(id)
                .map(complaint -> {
                    response.put("success", true);
                    response.put("data", complaint);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "投诉不存在");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateComplaint(@PathVariable Long id, @RequestBody Map<String, Object> updatedData) {
        Map<String, Object> response = new HashMap<>();
        return complaintRepository.findById(id)
                .map(complaint -> {
                    if (updatedData.containsKey("title")) {
                        complaint.setTitle((String) updatedData.get("title"));
                    }
                    if (updatedData.containsKey("content")) {
                        complaint.setContent((String) updatedData.get("content"));
                    }
                    if (updatedData.containsKey("status")) {
                        complaint.setStatus((String) updatedData.get("status"));
                    }
                    complaintRepository.save(complaint);
                    response.put("success", true);
                    response.put("message", "投诉更新成功");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "投诉不存在");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteComplaint(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        if (complaintRepository.existsById(id)) {
            complaintRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "投诉删除成功");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        response.put("success", false);
        response.put("message", "投诉不存在");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
