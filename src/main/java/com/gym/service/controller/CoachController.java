package com.gym.service.controller;

import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.entity.Complaint;
import com.gym.service.entity.Facility;
import com.gym.service.entity.FacilityBooking;
import com.gym.service.entity.Review;
import com.gym.service.entity.User;
import com.gym.service.service.CoachService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/coach")
public class CoachController {

    @Autowired
    private CoachService coachService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (coachService.authenticate(username, password)) {
                Coach coach = coachService.findByUsername(username);
                String token = jwtUtil.generateCoachToken(username, coach.getId());

                Map<String, Object> data = new HashMap<>();
                data.put("id", coach.getId());
                data.put("username", coach.getUsername());
                data.put("name", coach.getName());
                data.put("token", token);

                response.put("success", true);
                response.put("data", data);
            } else {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);

            if (coach != null) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("id", coach.getId());
                profile.put("username", coach.getUsername());
                profile.put("name", coach.getName());
                profile.put("email", coach.getEmail());
                profile.put("phone", coach.getPhone());
                profile.put("experience", coach.getExperienceYears());
                profile.put("certifications", coach.getCertifications());
                profile.put("specialty", coach.getSpecialty());
                profile.put("hourlyRate", coach.getHourlyRate());
                List<Classes> coachClasses = coachService.getCoachClasses(coach.getId());
                List<Review> coachReviews = coachService.getCoachReviews(coach.getId());
                int totalClasses = coachClasses.size();
                int totalStudents = coachService.getCoachStudents(coach.getId()).size();
                LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
                LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);
                long thisMonthClasses = coachClasses.stream()
                        .filter(c -> c.getStartTime() != null)
                        .filter(c -> !c.getStartTime().toLocalDate().isBefore(firstDayOfMonth))
                        .filter(c -> c.getStartTime().toLocalDate().isBefore(firstDayOfNextMonth))
                        .count();
                double avgRating = coachReviews.isEmpty()
                        ? (coach.getRating() != null ? coach.getRating() : 0.0)
                        : coachReviews.stream().filter(r -> r.getRating() != null).mapToInt(Review::getRating).average().orElse(0.0);

                profile.put("rating", Math.round(avgRating * 10.0) / 10.0);
                profile.put("qrCode", coach.getQrCode());
                profile.put("totalClasses", totalClasses);
                profile.put("totalStudents", totalStudents);
                profile.put("thisMonthClasses", (int) thisMonthClasses);
                profile.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
                profile.put("notificationSettings", parseNotificationSettings(coach.getNotificationSettings()));

                response.put("success", true);
                response.put("data", profile);
            } else {
                response.put("success", false);
                response.put("message", "教练不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取信息失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active-users")
    public ResponseEntity<Map<String, Object>> getActiveUsers() {
        Map<String, Object> response = new HashMap<>();
        try {
            int activeCount = coachService.getActiveUsersCount();
            response.put("success", true);
            response.put("data", activeCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取活跃人数失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today-schedule")
    public ResponseEntity<Map<String, Object>> getTodaySchedule() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            
            List<Map<String, Object>> schedule = new ArrayList<>();
            if (coach != null) {
                List<com.gym.service.entity.Classes> todayClasses = coachService.getTodayClasses(coach.getId());
                for (com.gym.service.entity.Classes c : todayClasses) {
                    int participantCount = coachService.getConfirmedClassBookings(c.getId()).size();
                    String status = getClassStatus(c.getStartTime());
                    String statusText = getStatusText(status);
                    schedule.add(createScheduleItem(
                        c.getId().intValue(),
                        formatTime(c.getStartTime(), c.getEndTime()),
                        c.getName(),
                        participantCount,
                        c.getLocation(),
                        status,
                        statusText
                    ));
                }
            }
            
            response.put("success", true);
            response.put("data", schedule);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取日程失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    private String getClassStatus(LocalDateTime startTime) {
        if (startTime == null) return "pending";
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isAfter(now.plusMinutes(30))) return "upcoming";
        if (startTime.isAfter(now)) return "starting";
        return "completed";
    }
    
    private String getStatusText(String status) {
        switch(status) {
            case "upcoming": return "即将开始";
            case "starting": return "即将开始";
            case "completed": return "已完成";
            default: return "待确认";
        }
    }
    
    private String formatTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) return "";
        String startStr = start.toLocalTime().toString().substring(0, 5);
        String endStr = end != null ? end.toLocalTime().toString().substring(0, 5) : "";
        return startStr + "-" + endStr;
    }

    private LocalDate parseFlexibleLocalDate(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("date required");
        }
        String s = raw.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            throw new IllegalArgumentException("date required");
        }
        if (s.length() >= 10 && s.charAt(4) == '-') {
            String datePart = s.substring(0, 10);
            return LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.US));
        } catch (DateTimeParseException e) {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.UK));
        }
    }

    private LocalTime parseLocalTime(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("time required");
        }
        String s = raw.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            throw new IllegalArgumentException("time required");
        }
        if (s.contains("AM") || s.contains("PM") || s.contains("am") || s.contains("pm")) {
            DateTimeFormatter[] fmts = new DateTimeFormatter[]{
                    DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                    DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                    DateTimeFormatter.ofPattern("h:mm:ss a", Locale.US)
            };
            for (DateTimeFormatter f : fmts) {
                try {
                    return LocalTime.parse(s, f);
                } catch (DateTimeParseException ignored) {
                    // try next
                }
            }
            throw new IllegalArgumentException("invalid time: " + s);
        }
        String t = s;
        if (t.length() == 5 && t.indexOf(':') == t.lastIndexOf(':')) {
            t = t + ":00";
        }
        return LocalTime.parse(t);
    }

    private Long toLong(Object o) {
        if (o == null) {
            return null;
        }
        try {
            String text = String.valueOf(o).trim();
            if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
                return null;
            }
            return Long.valueOf(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer toInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        String s = String.valueOf(o).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return Integer.parseInt(s);
    }

    private Map<String, Object> parseNotificationSettings(String raw) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("classReminder", true);
        settings.put("bookingNotification", true);
        settings.put("reviewNotification", true);
        settings.put("complaintNotification", true);
        if (raw == null || raw.trim().isEmpty()) {
            return settings;
        }
        String[] parts = raw.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && settings.containsKey(kv[0])) {
                settings.put(kv[0], Boolean.parseBoolean(kv[1]));
            }
        }
        return settings;
    }

    private String serializeNotificationSettings(Map<String, Object> settings) {
        Map<String, Object> merged = parseNotificationSettings(null);
        if (settings != null) {
            for (String key : merged.keySet()) {
                if (settings.containsKey(key)) {
                    Object value = settings.get(key);
                    merged.put(key, value instanceof Boolean ? value : Boolean.parseBoolean(String.valueOf(value)));
                }
            }
        }
        return "classReminder=" + merged.get("classReminder")
                + ";bookingNotification=" + merged.get("bookingNotification")
                + ";reviewNotification=" + merged.get("reviewNotification")
                + ";complaintNotification=" + merged.get("complaintNotification");
    }

    private boolean notificationEnabled(Coach coach, String key) {
        if (coach == null) {
            return false;
        }
        Object v = parseNotificationSettings(coach.getNotificationSettings()).get(key);
        return v instanceof Boolean ? (Boolean) v : Boolean.parseBoolean(String.valueOf(v));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);

            List<Map<String, Object>> notifications = new ArrayList<>();
            int nid = 1;
            if (coach != null) {
                LocalDateTime now = LocalDateTime.now();
                if (notificationEnabled(coach, "classReminder")) {
                    for (Classes c : coachService.getTodayClasses(coach.getId())) {
                        if (c.getStartTime() == null) {
                            continue;
                        }
                        long minutes = java.time.Duration.between(now, c.getStartTime()).toMinutes();
                        if (minutes > 0 && minutes <= 120) {
                            String timeRange = formatTime(c.getStartTime(), c.getEndTime());
                            Map<String, Object> n = createNotification(nid++, "⏰",
                                    String.format("「%s」将于 %s 开始（约 %d 分钟后）", c.getName(), timeRange, minutes),
                                    String.format("%s starts at %s in about %d minutes", englishCourseName(c), timeRange, minutes),
                                    "即将上课",
                                    "Class reminder",
                                    "classReminder");
                            notifications.add(n);
                        }
                    }
                }

                if (notificationEnabled(coach, "bookingNotification")) {
                    int pendingBookings = coachService.getCoachClasses(coach.getId()).stream()
                            .mapToInt(cl -> coachService.getPendingClassBookings(cl.getId()).size())
                            .sum();
                    if (pendingBookings > 0) {
                        notifications.add(createNotification(nid++, "📩",
                                String.format("您有 %d 条待确认的课程预约", pendingBookings),
                                String.format("You have %d pending class booking request(s)", pendingBookings),
                                "预约提醒",
                                "Booking alert",
                                "bookingRequest"));
                    }
                }
            }
            if (notifications.isEmpty()) {
                notifications.add(createNotification(1, "📌", "当前暂无临近开始的课程提醒", "No upcoming class reminders", "系统", "System", "system"));
            }

            response.put("success", true);
            response.put("data", notifications);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取通知失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/classes")
    public ResponseEntity<Map<String, Object>> getMyClasses() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            
            List<Map<String, Object>> classes = new ArrayList<>();
            if (coach != null) {
                List<com.gym.service.entity.Classes> coachClasses = coachService.getCoachClasses(coach.getId());
                for (com.gym.service.entity.Classes c : coachClasses) {
                    int participantCount = coachService.getConfirmedClassBookings(c.getId()).size();
                    String status = getClassStatus(c.getStartTime());
                    String statusText = getStatusText(status);
                    Map<String, Object> item = createClassItem(
                        c.getId().intValue(),
                        c.getName(),
                        formatTime(c.getStartTime(), c.getEndTime()),
                        c.getLocation(),
                        participantCount,
                        c.getCapacity(),
                        status,
                        statusText
                    );
                    if (c.getStartTime() != null) {
                        item.put("classDate", c.getStartTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                        item.put("startSlot", c.getStartTime().toLocalTime().toString().substring(0, 5));
                    }
                    if (c.getEndTime() != null) {
                        item.put("endSlot", c.getEndTime().toLocalTime().toString().substring(0, 5));
                    }
                    classes.add(item);
                }
            }

            response.put("success", true);
            response.put("data", classes);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取课程失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/classes")
    public ResponseEntity<Map<String, Object>> addClass(@RequestBody Map<String, Object> classData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);

            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            Object nameObj = classData.get("name");
            String name = nameObj != null ? String.valueOf(nameObj).trim() : "";
            if (name.isEmpty() || "null".equalsIgnoreCase(name)) {
                response.put("success", false);
                response.put("message", "请填写课程名称");
                return ResponseEntity.ok(response);
            }

            Integer capacity = toInteger(classData.get("capacity"));
            if (capacity == null || capacity < 1) {
                response.put("success", false);
                response.put("message", "请填写有效的容纳人数");
                return ResponseEntity.ok(response);
            }

            String location = classData.get("location") != null ? String.valueOf(classData.get("location")) : "";
            String dateStr = classData.get("date") != null ? String.valueOf(classData.get("date")) : "";
            String startStr = classData.get("startTime") != null ? String.valueOf(classData.get("startTime")) : "";
            String endStr = classData.get("endTime") != null ? String.valueOf(classData.get("endTime")) : "";

            LocalDate d = parseFlexibleLocalDate(dateStr);
            LocalTime st = parseLocalTime(startStr);
            LocalTime et = parseLocalTime(endStr);
            if (st.equals(et)) {
                response.put("success", false);
                response.put("message", "结束时间必须与开始时间不同");
                return ResponseEntity.ok(response);
            }

            LocalDateTime startDt = LocalDateTime.of(d, st);
            LocalDateTime endDt = LocalDateTime.of(d, et);
            if (!endDt.isAfter(startDt)) {
                endDt = endDt.plusDays(1);
            }

            Long reservedBookingId = toLong(classData.get("reservedBookingId"));
            if (!hasConfirmedFacilityBooking(coach.getId(), reservedBookingId, location, startDt, endDt)) {
                response.put("success", false);
                response.put("message", "请先预约该场地对应时段，再上架课程");
                return ResponseEntity.ok(response);
            }

            Classes newClass = new Classes();
            newClass.setName(name);
            newClass.setCapacity(capacity);
            newClass.setLocation(location);
            newClass.setStartTime(startDt);
            newClass.setEndTime(endDt);

            Classes saved = coachService.addClass(coach.getId(), newClass);
            if (saved == null) {
                response.put("success", false);
                response.put("message", "上架失败: 无法关联教练账号，请重新登录");
                return ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("message", "课程上架成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上架失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<Map<String, Object>> updateClass(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            String name = body.get("name") != null ? String.valueOf(body.get("name")).trim() : null;
            Integer capacity = body.containsKey("capacity") ? toInteger(body.get("capacity")) : null;
            String location = body.get("location") != null ? String.valueOf(body.get("location")) : null;

            LocalDateTime startDt = null;
            LocalDateTime endDt = null;
            if (body.get("date") != null && body.get("startTime") != null && body.get("endTime") != null) {
                LocalDate d = parseFlexibleLocalDate(String.valueOf(body.get("date")));
                LocalTime st = parseLocalTime(String.valueOf(body.get("startTime")));
                LocalTime et = parseLocalTime(String.valueOf(body.get("endTime")));
                startDt = LocalDateTime.of(d, st);
                endDt = LocalDateTime.of(d, et);
                if (!endDt.isAfter(startDt)) {
                    endDt = endDt.plusDays(1);
                }
            }

            Classes updated = coachService.updateCoachClass(coach.getId(), id, name, capacity, startDt, endDt, location);
            if (updated == null) {
                response.put("success", false);
                response.put("message", "课程不存在或无权修改");
                return ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("message", "更新成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Map<String, Object>> deleteClass(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }
            coachService.deleteClassForCoach(coach.getId(), id);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/facilities")
    public ResponseEntity<Map<String, Object>> getFacilities() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> facilities = new ArrayList<>();
            List<String> defaultSlots = Arrays.asList("09:00-11:00", "14:00-16:00", "19:00-21:00");
            LocalDate day = LocalDate.now();
            for (Facility f : coachService.getFacilities()) {
                boolean baseAvailable = f.getAvailable() == null || Boolean.TRUE.equals(f.getAvailable());
                List<String> openSlots = new ArrayList<>();
                if (baseAvailable) {
                    for (String slot : defaultSlots) {
                        String[] parts = slot.split("-");
                        LocalDateTime slotStart = LocalDateTime.of(day, parseLocalTime(parts[0].trim()));
                        LocalDateTime slotEnd = LocalDateTime.of(day, parseLocalTime(parts[1].trim()));
                        if (!slotEnd.isAfter(slotStart)) {
                            slotEnd = slotEnd.plusDays(1);
                        }
                        boolean occupied = false;
                        for (FacilityBooking existing : coachService.getFacilityBookings(f.getId())) {
                            if (existing.getStartTime() == null || existing.getEndTime() == null) {
                                continue;
                            }
                            boolean overlaps = slotStart.isBefore(existing.getEndTime()) && slotEnd.isAfter(existing.getStartTime());
                            if ("confirmed".equalsIgnoreCase(String.valueOf(existing.getStatus())) && overlaps) {
                                occupied = true;
                                break;
                            }
                        }
                        if (!occupied) {
                            openSlots.add(slot);
                        }
                    }
                }
                facilities.add(createFacility(
                        f.getId().intValue(),
                        f.getName(),
                        f.getDescription() != null ? f.getDescription() : "",
                        translateFacilityDescription(f.getName(), f.getDescription()),
                        f.getEquipment() != null ? f.getEquipment() : "",
                        translateFacilityEquipment(f.getName(), f.getEquipment()),
                        baseAvailable && !openSlots.isEmpty(),
                        openSlots
                ));
            }
            response.put("success", true);
            response.put("data", facilities);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取场地失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/facilities/book")
    public ResponseEntity<Map<String, Object>> bookFacility(@RequestBody Map<String, Object> bookingData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            Long facilityId = Long.valueOf(String.valueOf(bookingData.get("facilityId")));
            String slot = String.valueOf(bookingData.get("timeSlot"));
            String[] parts = slot.split("-");
            if (parts.length < 2) {
                response.put("success", false);
                response.put("message", "时间段格式无效");
                return ResponseEntity.ok(response);
            }
            LocalDate day = LocalDate.now();
            LocalTime t0 = parseLocalTime(parts[0].trim());
            LocalTime t1 = parseLocalTime(parts[1].trim());
            LocalDateTime start = LocalDateTime.of(day, t0);
            LocalDateTime end = LocalDateTime.of(day, t1);
            if (!end.isAfter(start)) {
                end = end.plusDays(1);
            }

            FacilityBooking fb = coachService.bookFacility(coach.getId(), facilityId, start, end);
            if (fb == null) {
                response.put("success", false);
                response.put("message", "场地不存在、该时段已被预约，或预约失败");
                return ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("message", "预约成功");
            response.put("data", toFacilityBookingItem(fb, false));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "预约失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/facilities/bookings/{bookingId}")
    public ResponseEntity<Map<String, Object>> cancelFacilityBooking(@PathVariable Long bookingId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            boolean cancelled = coachService.cancelFacilityBooking(coach.getId(), bookingId);
            response.put("success", cancelled);
            response.put("message", cancelled ? "场地预约已取消" : "场地预约不存在或无权取消");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消场地预约失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/facilities/my-bookings")
    public ResponseEntity<Map<String, Object>> getMyFacilityBookings() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            for (FacilityBooking b : coachService.getCoachFacilityBookings(coach.getId())) {
                if (b.getStartTime() == null || b.getEndTime() == null) {
                    continue;
                }
                if (!"confirmed".equalsIgnoreCase(String.valueOf(b.getStatus()))) {
                    continue;
                }
                boolean usedByClass = isFacilityBookingUsedByClass(coach.getId(), b);
                rows.add(toFacilityBookingItem(b, usedByClass));
            }
            response.put("success", true);
            response.put("data", rows);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取已预约场地失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/students")
    public ResponseEntity<Map<String, Object>> getStudents() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            
            List<Map<String, Object>> students = new ArrayList<>();
            if (coach != null) {
                List<User> coachStudents = coachService.getCoachStudents(coach.getId());
                for (User user : coachStudents) {
                    int classCount = getStudentClassCount(user.getId(), coach.getId());
                    String medical = user.getMedicalNotes() != null ? user.getMedicalNotes().trim() : "";
                    boolean hasDisease = !medical.isEmpty();
                    students.add(createStudent(
                            user.getId().intValue(),
                            user.getName(),
                            maskPhone(user.getPhone()),
                            coachService.getUserMembershipType(user.getId()),
                            user.getBirthday() != null ? user.getBirthday().toString() : "",
                            hasDisease,
                            hasDisease ? medical : "",
                            classCount > 0,
                            classCount
                    ));
                }
            }

            response.put("success", true);
            response.put("data", students);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取学员失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    private int getStudentClassCount(Long userId, Long coachId) {
        List<com.gym.service.entity.Classes> coachClasses = coachService.getCoachClasses(coachId);
        return (int) coachClasses.stream()
                .flatMap(c -> coachService.getClassBookings(c.getId()).stream())
                .filter(b -> userId.equals(b.getUserId()))
                .count();
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    @GetMapping("/complaints")
    public ResponseEntity<Map<String, Object>> getComplaints() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            
            List<Map<String, Object>> complaints = new ArrayList<>();
            if (coach != null) {
                List<Complaint> coachComplaints = coachService.getCoachComplaints(coach.getId());
                for (Complaint complaint : coachComplaints) {
                    complaints.add(createComplaint(
                        complaint.getId().intValue(),
                        complaint.getUser() != null ? complaint.getUser().getUsername() : "unknown",
                        complaint.getTitle(),
                        complaint.getTitleEn(),
                        complaint.getContent(),
                        complaint.getContentEn(),
                        complaint.getStatus(),
                        complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString().replace("T", " ") : "",
                        complaint.getResponse(),
                        complaint.getResponseEn()
                    ));
                }
            }

            response.put("success", true);
            response.put("data", complaints);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取投诉失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complaints/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondComplaint(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String responseText = data.get("response");
            String responseEn = data.get("responseEn");
            Complaint complaint = coachService.respondToComplaint(id, responseText, responseEn);
            if (complaint != null) {
                response.put("success", true);
                response.put("message", "处理成功");
            } else {
                response.put("success", false);
                response.put("message", "投诉不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getReviews() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            
            List<Map<String, Object>> reviews = new ArrayList<>();
            if (coach != null) {
                List<Review> coachReviews = coachService.getCoachReviews(coach.getId());
                for (Review review : coachReviews) {
                    Classes cls = review.getClasses();
                    String className = cls != null ? cls.getName() : "";
                    String classNameEn = cls != null && cls.getNameEn() != null && !cls.getNameEn().isEmpty()
                            ? cls.getNameEn() : className;
                    reviews.add(createReview(
                        review.getId().intValue(),
                        review.getUser() != null ? review.getUser().getUsername() : "unknown",
                        review.getRating(),
                        review.getContent(),
                        review.getContentEn(),
                        className,
                        classNameEn,
                        review.getCreatedAt() != null ? review.getCreatedAt().toString().replace("T", " ") : "",
                        review.getResponded() != null && review.getResponded(),
                        review.getResponse() != null ? review.getResponse() : "",
                        review.getResponseEn() != null ? review.getResponseEn() : ""
                    ));
                }
            }

            response.put("success", true);
            response.put("data", reviews);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取评价失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reviews/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondReview(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            String responseText = data.get("response");
            String responseEn = data.get("responseEn");
            Review review = coachService.respondToReview(id, responseText, responseEn);
            if (review != null) {
                response.put("success", true);
                response.put("message", "回复成功");
            } else {
                response.put("success", false);
                response.put("message", "评价不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "回复失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notification-settings")
    public ResponseEntity<Map<String, Object>> saveNotificationSettings(@RequestBody Map<String, Object> settings) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Coach coach = coachService.findByUsername(username);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }
            coachService.saveNotificationSettings(coach.getId(), serializeNotificationSettings(settings));
            response.put("success", true);
            response.put("message", "设置保存成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createScheduleItem(int id, String time, String name, int participants, String location, String status, String statusText) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("time", time);
        item.put("name", name);
        item.put("participants", participants);
        item.put("location", location);
        item.put("status", status);
        item.put("statusText", statusText);
        return item;
    }

    private String englishCourseName(Classes c) {
        if (c == null) {
            return "Class";
        }
        if (c.getNameEn() != null && !c.getNameEn().trim().isEmpty()) {
            return c.getNameEn().trim();
        }
        String name = c.getName() != null ? c.getName().trim() : "";
        switch (name) {
            case "力量训练": return "Strength Training";
            case "瑜伽放松": return "Yoga Relaxation";
            case "动感单车": return "Spinning";
            case "游泳": return "Swimming";
            case "拳击": return "Boxing";
            default: return name.isEmpty() ? "Class" : name;
        }
    }

    private String translateFacilityDescription(String name, String fallback) {
        if ("训练区A".equals(name)) return "Main strength training zone";
        if ("训练区B".equals(name)) return "Cardio and functional training zone";
        if ("瑜伽室".equals(name)) return "Yoga room for group and private sessions";
        if ("操课室".equals(name)) return "Studio for group classes";
        if ("动感单车室".equals(name)) return "Dedicated spinning studio";
        if ("C区力量区".equals(name)) return "Strength zone for barbells and heavy training";
        if ("A区教室".equals(name)) return "Multi-purpose classroom for small group sessions";
        if ("B区动感单车房".equals(name)) return "Spin room for cycling classes";
        if ("D区搏击室".equals(name)) return "Boxing and combat training room";
        if ("E区舞蹈室".equals(name)) return "Dance and stretching studio";
        if ("游泳池".equals(name)) return "Indoor swimming pool";
        if ("F区有氧区".equals(name)) return "Cardio zone for aerobic classes";
        return fallback != null ? fallback : "";
    }

    private String translateFacilityEquipment(String name, String fallback) {
        if ("训练区A".equals(name)) return "Barbells, dumbbells, cable machine";
        if ("训练区B".equals(name)) return "Treadmills, ellipticals, rowing machines";
        if ("瑜伽室".equals(name)) return "Yoga mats, yoga balls";
        if ("操课室".equals(name)) return "Audio system, mirrors, step platforms";
        if ("动感单车室".equals(name)) return "Spin bikes, audio system";
        if ("C区力量区".equals(name)) return "Power racks, barbells, plates, dumbbells";
        if ("A区教室".equals(name)) return "Projector, mats, light training tools";
        if ("B区动感单车房".equals(name)) return "Spin bikes, audio system, fans";
        if ("D区搏击室".equals(name)) return "Punching bags, gloves, pads";
        if ("E区舞蹈室".equals(name)) return "Mirrors, ballet barre, audio system";
        if ("游泳池".equals(name)) return "Lane ropes, kickboards, rescue equipment";
        if ("F区有氧区".equals(name)) return "Treadmills, ellipticals, rowing machines";
        return fallback != null ? fallback : "";
    }

    private Map<String, Object> createNotification(int id, String icon, String title, String titleEn,
                                                   String time, String timeEn, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("icon", icon);
        notification.put("title", title);
        notification.put("titleEn", titleEn);
        notification.put("time", time);
        notification.put("timeEn", timeEn);
        notification.put("type", type);
        return notification;
    }

    private Map<String, Object> createClassItem(int id, String name, String time, String location, int participants, int capacity, String status, String statusText) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("time", time);
        item.put("location", location);
        item.put("participants", participants);
        item.put("capacity", capacity);
        item.put("status", status);
        item.put("statusText", statusText);
        return item;
    }

    private Map<String, Object> toFacilityBookingItem(FacilityBooking b, boolean usedByClass) {
        Map<String, Object> item = new HashMap<>();
        Facility f = b.getFacility();
        item.put("id", b.getId());
        item.put("facilityId", f != null ? f.getId() : null);
        item.put("facilityName", f != null ? f.getName() : "");
        item.put("facilityNameEn", f != null ? translateVenueName(f.getName()) : "");
        item.put("date", b.getStartTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        item.put("startTime", b.getStartTime().toLocalTime().toString().substring(0, 5));
        item.put("endTime", b.getEndTime().toLocalTime().toString().substring(0, 5));
        item.put("label", (f != null ? f.getName() : "") + " " + formatTime(b.getStartTime(), b.getEndTime()));
        item.put("labelEn", (f != null ? translateVenueName(f.getName()) : "") + " " + formatTime(b.getStartTime(), b.getEndTime()));
        item.put("usedByClass", usedByClass);
        item.put("status", b.getStatus());
        return item;
    }

    private boolean isFacilityBookingUsedByClass(Long coachId, FacilityBooking booking) {
        if (booking == null || booking.getFacility() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
            return false;
        }
        String facilityName = booking.getFacility().getName();
        return coachService.getCoachClasses(coachId).stream()
                .filter(c -> facilityName.equals(c.getLocation()))
                .filter(c -> c.getStartTime() != null && c.getEndTime() != null)
                .anyMatch(c -> c.getStartTime().isBefore(booking.getEndTime()) && c.getEndTime().isAfter(booking.getStartTime()));
    }

    private boolean hasConfirmedFacilityBooking(Long coachId, Long reservedBookingId, String location, LocalDateTime classStart, LocalDateTime classEnd) {
        if (coachId == null || location == null || location.trim().isEmpty() || classStart == null || classEnd == null) {
            return false;
        }
        for (FacilityBooking b : coachService.getCoachFacilityBookings(coachId)) {
            if (!"confirmed".equalsIgnoreCase(String.valueOf(b.getStatus()))) {
                continue;
            }
            if (reservedBookingId != null && !reservedBookingId.equals(b.getId())) {
                continue;
            }
            Facility f = b.getFacility();
            if (f == null || f.getName() == null || !f.getName().equals(location)) {
                continue;
            }
            if (b.getStartTime() == null || b.getEndTime() == null) {
                continue;
            }
            boolean withinReservedSlot = !classStart.isBefore(b.getStartTime()) && !classEnd.isAfter(b.getEndTime());
            if (withinReservedSlot && !isFacilityBookingUsedByClass(coachId, b)) {
                return true;
            }
        }
        return false;
    }

    private String translateVenueName(String name) {
        if ("训练区A".equals(name)) return "Training Zone A";
        if ("训练区B".equals(name)) return "Training Zone B";
        if ("瑜伽室".equals(name)) return "Yoga Studio";
        if ("操课室".equals(name)) return "Group Class Room";
        if ("动感单车室".equals(name)) return "Spinning Studio";
        if ("C区力量区".equals(name)) return "Zone C — Strength";
        if ("A区教室".equals(name)) return "Classroom A";
        if ("B区动感单车房".equals(name)) return "Zone B — Spin Studio";
        if ("D区搏击室".equals(name)) return "Zone D — Boxing";
        if ("E区舞蹈室".equals(name)) return "Zone E — Dance";
        if ("游泳池".equals(name)) return "Swimming Pool";
        if ("F区有氧区".equals(name)) return "Zone F — Cardio";
        return name != null ? name : "";
    }

    private Map<String, Object> createFacility(int id, String name, String description, String descriptionEn,
                                               String equipment, String equipmentEn, boolean available, List<String> timeSlots) {
        Map<String, Object> facility = new HashMap<>();
        facility.put("id", id);
        facility.put("name", name);
        facility.put("description", description);
        facility.put("descriptionEn", descriptionEn);
        facility.put("equipment", equipment);
        facility.put("equipmentEn", equipmentEn);
        facility.put("available", available);
        facility.put("timeSlots", timeSlots);
        return facility;
    }

    private Map<String, Object> createStudent(int id, String name, String phone, String memberType, String joinDate, boolean hasDisease, String diseaseInfo, boolean hasAttended, int classCount) {
        Map<String, Object> student = new HashMap<>();
        student.put("id", id);
        student.put("name", name);
        student.put("phone", phone);
        student.put("memberType", memberType);
        student.put("joinDate", joinDate);
        student.put("hasDisease", hasDisease);
        student.put("diseaseInfo", diseaseInfo);
        student.put("hasAttended", hasAttended);
        student.put("classCount", classCount);
        return student;
    }

    private Map<String, Object> createComplaint(int id, String userName, String title, String titleEn,
                                                String content, String contentEn, String status,
                                                String createdAt, String response, String responseEn) {
        Map<String, Object> complaint = new HashMap<>();
        complaint.put("id", id);
        complaint.put("userName", userName);
        complaint.put("title", title);
        complaint.put("titleEn", titleEn);
        complaint.put("content", content);
        complaint.put("contentEn", contentEn);
        complaint.put("status", status);
        complaint.put("createdAt", createdAt);
        complaint.put("response", response != null ? response : "");
        complaint.put("responseEn", responseEn != null ? responseEn : "");
        return complaint;
    }

    private Map<String, Object> createReview(int id, String userName, int rating, String content, String contentEn,
                                             String className, String classNameEn, String createdAt,
                                             boolean responded, String response, String responseEn) {
        Map<String, Object> review = new HashMap<>();
        review.put("id", id);
        review.put("userName", userName);
        review.put("rating", rating);
        review.put("content", content);
        review.put("contentEn", contentEn);
        review.put("className", className);
        review.put("classNameEn", classNameEn);
        review.put("createdAt", createdAt);
        review.put("responded", responded);
        review.put("response", response);
        review.put("responseEn", responseEn);
        return review;
    }
}