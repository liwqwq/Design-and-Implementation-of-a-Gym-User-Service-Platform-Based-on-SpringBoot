package com.gym.service.controller;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.entity.User;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.BookingService;
import com.gym.service.service.ClassesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
public class ClassesController {

    @Autowired
    private ClassesService classesService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllClasses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Classes> classes = classesService.getAllClasses();
            
            // 用户端只显示已经上架的团课，避免私教课或管理员下架/待审核课程混入预约列表。
            classes = classes.stream()
                    .filter(c -> c.getCapacity() == null || c.getCapacity() > 10)
                    .filter(c -> c.getStatus() == null || "ACTIVE".equalsIgnoreCase(String.valueOf(c.getStatus())) || "active".equalsIgnoreCase(String.valueOf(c.getStatus())))
                    .collect(Collectors.toList());
            
            // 如果传入了日期参数，按日期筛选
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                classes = classes.stream()
                        .filter(c -> c.getStartTime() != null 
                                && !c.getStartTime().isBefore(startOfDay)
                                && !c.getStartTime().isAfter(endOfDay))
                        .collect(Collectors.toList());
            }
            
            String username = null;
            Long currentUserId = null;
            try {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
                User currentUser = userRepository.findByUsername(username).orElse(null);
                currentUserId = currentUser != null ? currentUser.getId() : null;
            } catch (Exception e) {
            }
            
            classes = classes.stream()
                    .sorted((a, b) -> {
                        if (a.getStartTime() == null && b.getStartTime() == null) return 0;
                        if (a.getStartTime() == null) return 1;
                        if (b.getStartTime() == null) return -1;
                        return a.getStartTime().compareTo(b.getStartTime());
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> classData = new ArrayList<>();
            for (Classes c : classes) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getName());
                map.put("nameEn", c.getNameEn());
                String instructor = "—";
                Coach coach = c.getCoach();
                if (coach != null) {
                    String nm = coach.getName();
                    if (nm != null && !nm.trim().isEmpty()) {
                        instructor = nm.trim();
                    } else if (coach.getUsername() != null && !coach.getUsername().isEmpty()) {
                        instructor = coach.getUsername();
                    }
                }
                if ("—".equals(instructor) && c.getLocation() != null && c.getLocation().startsWith("私教区-")) {
                    instructor = c.getLocation().substring("私教区-".length());
                }
                map.put("instructor", instructor);
                map.put("instructorId", coach != null ? coach.getId() : null);
                map.put("capacity", c.getCapacity());
                map.put("startTime", c.getStartTime());
                map.put("endTime", c.getEndTime());
                map.put("location", c.getLocation());
                map.put("status", c.getStatus());
                
                List<Booking> bookings = bookingService.getBookingsByClassId(c.getId());
                int bookedCount = 0;
                boolean isBooked = false;
                Long currentUserBookingId = null;
                for (Booking b : bookings) {
                    if ("CONFIRMED".equals(b.getStatus())) {
                        bookedCount++;
                        if (currentUserId != null && currentUserId.equals(b.getUserId())) {
                            isBooked = true;
                            currentUserBookingId = b.getId();
                        }
                    }
                }
                map.put("bookedCount", bookedCount);
                int capacity = c.getCapacity() != null ? c.getCapacity() : 0;
                map.put("spotsLeft", Math.max(0, capacity - bookedCount));
                map.put("isBooked", isBooked);
                map.put("bookingId", currentUserBookingId);
                
                classData.add(map);
            }
            
            response.put("success", true);
            response.put("data", classData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting classes: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getClassById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Classes classes = classesService.getClassById(id);
            if (classes == null) {
                response.put("success", false);
                response.put("message", "Class not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            response.put("success", true);
            response.put("data", classes);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting class: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
