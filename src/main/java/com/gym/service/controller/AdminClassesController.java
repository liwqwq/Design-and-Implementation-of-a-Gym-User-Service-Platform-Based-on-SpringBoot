package com.gym.service.controller;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.service.BookingService;
import com.gym.service.service.ClassesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/classes")
public class AdminClassesController {

    @Autowired
    private ClassesService classesService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> rows = classesService.getAllClasses().stream()
                    .sorted(Comparator.comparing(Classes::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(this::toClassMap)
                    .collect(Collectors.toList());
            response.put("success", true);
            response.put("data", rows);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> toClassMap(Classes c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("nameEn", c.getNameEn());
        map.put("capacity", c.getCapacity());
        map.put("startTime", c.getStartTime());
        map.put("endTime", c.getEndTime());
        map.put("location", c.getLocation());
        map.put("status", c.getStatus());

        Coach coach = c.getCoach();
        String instructor = "—";
        if (coach != null) {
            map.put("coachId", coach.getId());
            if (coach.getName() != null && !coach.getName().trim().isEmpty()) {
                instructor = coach.getName().trim();
            } else if (coach.getUsername() != null && !coach.getUsername().trim().isEmpty()) {
                instructor = coach.getUsername().trim();
            }
        }
        if ("—".equals(instructor) && c.getLocation() != null && c.getLocation().startsWith("私教区-")) {
            instructor = c.getLocation().substring("私教区-".length());
        }
        map.put("instructor", instructor);

        List<Booking> bookings = new ArrayList<>(bookingService.getBookingsByClassId(c.getId()));
        long bookedCount = bookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(String.valueOf(b.getStatus()))).count();
        map.put("bookedCount", bookedCount);
        int capacity = c.getCapacity() != null ? c.getCapacity() : 0;
        map.put("spotsLeft", Math.max(0, capacity - bookedCount));
        return map;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = body.get("name") != null ? body.get("name").toString().trim() : "";
            if (name.isEmpty()) {
                response.put("success", false);
                response.put("message", "课程名称不能为空");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            Integer capacity = body.get("capacity") instanceof Number
                    ? ((Number) body.get("capacity")).intValue()
                    : Integer.parseInt(String.valueOf(body.getOrDefault("capacity", 20)));
            String startTime = body.get("startTime") != null ? body.get("startTime").toString() : null;
            String endTime = body.get("endTime") != null ? body.get("endTime").toString() : null;
            String location = body.get("location") != null ? body.get("location").toString() : "";
            String status = body.get("status") != null ? body.get("status").toString() : "ACTIVE";
            Long coachId = parseCoachId(body.get("coachId"));

            Classes saved = classesService.createClassWithCoach(name, capacity, startTime, endTime, location, normalizeStatus(status), coachId);
            response.put("success", true);
            response.put("data", toClassMap(saved));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = body.get("name") != null ? body.get("name").toString() : null;
            Integer capacity = body.get("capacity") instanceof Number
                    ? ((Number) body.get("capacity")).intValue()
                    : null;
            String startTime = body.get("startTime") != null ? body.get("startTime").toString() : null;
            String endTime = body.get("endTime") != null ? body.get("endTime").toString() : null;
            String location = body.get("location") != null ? body.get("location").toString() : null;
            String status = body.get("status") != null ? normalizeStatus(body.get("status").toString()) : null;
            Long coachId = body.containsKey("coachId") ? parseCoachId(body.get("coachId")) : null;
            Classes updated = classesService.updateClassWithCoach(id, name, capacity, startTime, endTime, location, status, coachId);
            if (updated == null) {
                response.put("success", false);
                response.put("message", "课程不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("success", true);
            response.put("data", toClassMap(updated));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Long parseCoachId(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        String s = raw.toString().trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return Long.parseLong(s);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ACTIVE";
        }
        String s = status.trim();
        if ("active".equalsIgnoreCase(s) || "已上架".equals(s)) {
            return "ACTIVE";
        }
        if ("inactive".equalsIgnoreCase(s) || "disabled".equalsIgnoreCase(s) || "已下架".equals(s)) {
            return "INACTIVE";
        }
        if ("pending".equalsIgnoreCase(s) || "待审核".equals(s)) {
            return "PENDING";
        }
        return s.toUpperCase();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            classesService.deleteClass(id);
            response.put("success", true);
            response.put("message", "deleted");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
