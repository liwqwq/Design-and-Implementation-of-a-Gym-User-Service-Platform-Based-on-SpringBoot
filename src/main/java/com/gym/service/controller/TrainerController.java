package com.gym.service.controller;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.PersonalTrainer;
import com.gym.service.entity.User;
import com.gym.service.repository.BookingRepository;
import com.gym.service.repository.ClassesRepository;
import com.gym.service.repository.PersonalTrainerRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    @Autowired
    private PersonalTrainerRepository trainerRepository;
    
    @Autowired
    private ClassesRepository classesRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTrainers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PersonalTrainer> trainers = trainerRepository.findAll();
            response.put("success", true);
            response.put("data", trainers);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTrainerById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PersonalTrainer trainer = trainerRepository.findById(id).orElse(null);
            if (trainer != null) {
                response.put("success", true);
                response.put("data", trainer);
                response.put("message", "获取成功");
            } else {
                response.put("success", false);
                response.put("message", "教练不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<Map<String, Object>> getTrainerCourses(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PersonalTrainer trainer = trainerRepository.findById(id).orElse(null);
            if (trainer == null) {
                response.put("success", false);
                response.put("message", "教练不存在");
                return ResponseEntity.ok(response);
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElse(null);
            List<Long> bookedClassIds = new ArrayList<>();
            if (user != null) {
                bookedClassIds = bookingRepository.findByUserId(user.getId()).stream()
                    .filter(b -> "CONFIRMED".equals(b.getStatus()))
                    .map(Booking::getClassId)
                    .collect(Collectors.toList());
            }

            List<Map<String, Object>> courses = new ArrayList<>();
            CourseTemplate[] templates = templatesForTrainer(id);
            for (CourseTemplate template : templates) {
                Classes cls = findOrCreatePrivateClass(trainer, template);
                Map<String, Object> course = new HashMap<>();
                course.put("id", cls.getId());
                course.put("name", cls.getName());
                course.put("nameEn", cls.getNameEn());
                course.put("time", template.timeRange);
                course.put("duration", template.durationMinutes + "分钟");
                course.put("durationMinutes", template.durationMinutes);
                long confirmedCount = bookingRepository.findByClassesId(cls.getId()).stream()
                        .filter(b -> "CONFIRMED".equals(b.getStatus()))
                        .count();
                int capacity = cls.getCapacity() != null ? cls.getCapacity() : 1;
                boolean isBooked = bookedClassIds.contains(cls.getId());
                course.put("bookedCount", confirmedCount);
                course.put("capacity", capacity);
                course.put("isBooked", isBooked);
                course.put("isFull", !isBooked && confirmedCount >= capacity);
                courses.add(course);
            }
            
            response.put("success", true);
            response.put("data", courses);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    private Classes findOrCreatePrivateClass(PersonalTrainer trainer, CourseTemplate template) {
        String privateLocation = "私教区-" + trainer.getName();
        List<Classes> allClasses = classesRepository.findAll();
        for (Classes c : allClasses) {
            if (template.nameZh.equals(c.getName()) && privateLocation.equals(c.getLocation())) {
                // 补齐旧数据缺失的英文名，避免英文界面仍显示中文
                if (c.getNameEn() == null || c.getNameEn().trim().isEmpty()) {
                    c.setNameEn(template.nameEn);
                    classesRepository.save(c);
                }
                return c;
            }
        }

        LocalTime startLocalTime = LocalTime.parse(template.timeRange.substring(0, 5));
        LocalDateTime start = LocalDateTime.now()
                .withHour(startLocalTime.getHour())
                .withMinute(startLocalTime.getMinute())
                .withSecond(0)
                .withNano(0);

        Classes cls = new Classes();
        cls.setName(template.nameZh);
        cls.setNameEn(template.nameEn);
        cls.setCapacity(1);
        cls.setLocation(privateLocation);
        cls.setStartTime(start);
        cls.setEndTime(start.plusMinutes(template.durationMinutes));
        cls.setStatus("ACTIVE");
        cls.setCreatedAt(LocalDateTime.now());
        return classesRepository.save(cls);
    }

    private CourseTemplate[] templatesForTrainer(Long trainerId) {
        int index = trainerId == null ? 0 : (int) ((trainerId - 1) % 6);
        CourseTemplate[][] templateGroups = new CourseTemplate[][]{
                {
                        new CourseTemplate("少儿体适能基础", "Kids Fitness Basics", "10:00-11:00", 60),
                        new CourseTemplate("普拉提核心", "Pilates Core", "15:00-16:00", 60),
                        new CourseTemplate("产后恢复训练", "Postnatal Recovery Training", "19:00-20:00", 60)
                },
                {
                        new CourseTemplate("流瑜伽", "Flow Yoga", "09:00-10:00", 60),
                        new CourseTemplate("阴瑜伽", "Yin Yoga", "18:00-19:00", 60),
                        new CourseTemplate("冥想放松", "Meditation Relaxation", "20:00-20:30", 30)
                },
                {
                        new CourseTemplate("力量训练入门", "Strength Training Basics", "10:00-11:00", 60),
                        new CourseTemplate("增肌训练", "Muscle Gain Training", "15:00-16:00", 60),
                        new CourseTemplate("功能性训练", "Functional Training", "19:00-20:00", 60)
                },
                {
                        new CourseTemplate("产后修复课", "Postnatal Repair Class", "10:00-10:45", 45),
                        new CourseTemplate("美臀塑形", "Glute Sculpting", "14:00-15:00", 60),
                        new CourseTemplate("体态矫正", "Posture Correction", "18:00-19:00", 60)
                },
                {
                        new CourseTemplate("普拉提拉伸", "Pilates Stretch", "09:00-10:00", 60),
                        new CourseTemplate("瑜伽拉伸", "Yoga Stretch", "14:00-15:00", 60),
                        new CourseTemplate("康复训练", "Rehabilitation Training", "19:00-19:30", 30)
                },
                {
                        new CourseTemplate("散打基础", "Sanda Basics", "10:00-11:00", 60),
                        new CourseTemplate("爆发力训练", "Power Training", "16:00-17:00", 60),
                        new CourseTemplate("少儿搏击", "Kids Boxing", "18:00-18:45", 45)
                }
        };
        return templateGroups[index];
    }

    private static class CourseTemplate {
        String nameZh;
        String nameEn;
        String timeRange;
        int durationMinutes;

        CourseTemplate(String nameZh, String nameEn, String timeRange, int durationMinutes) {
            this.nameZh = nameZh;
            this.nameEn = nameEn;
            this.timeRange = timeRange;
            this.durationMinutes = durationMinutes;
        }
    }
}
