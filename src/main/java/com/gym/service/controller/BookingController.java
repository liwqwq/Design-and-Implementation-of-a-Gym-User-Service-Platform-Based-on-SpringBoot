package com.gym.service.controller;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.entity.User;
import com.gym.service.service.BookingService;
import com.gym.service.service.ClassesService;
import com.gym.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ClassesService classesService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            response.put("success", true);
            response.put("data", bookings);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting bookings: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            response.put("success", true);
            response.put("data", bookings);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting bookings by user: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyBookings() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            List<Booking> bookings = bookingService.getBookingsByUserId(user.getId());
            
            // 构建安全的响应数据。
            // 注意：前端“我的课程/我的课表”需要课程名称、教练、时间等信息，
            // 不能只返回 classId，否则页面会显示“未知课程/未知教练”。
            List<Map<String, Object>> bookingDataList = new java.util.ArrayList<>();
            for (Booking booking : bookings) {
                Map<String, Object> bookingData = new java.util.HashMap<>();
                bookingData.put("id", booking.getId());
                bookingData.put("classId", booking.getClassId());
                bookingData.put("userId", booking.getUserId());
                bookingData.put("status", booking.getStatus());
                bookingData.put("bookingTime", booking.getBookingTime());

                Classes bookedClass = null;
                if (booking.getClassId() != null) {
                    bookedClass = classesService.getClassById(booking.getClassId());
                }
                if (bookedClass == null) {
                    bookedClass = booking.getClasses();
                }

                if (bookedClass != null) {
                    Map<String, Object> classData = new java.util.HashMap<>();
                    classData.put("id", bookedClass.getId());
                    classData.put("name", bookedClass.getName());
                    classData.put("nameEn", bookedClass.getNameEn());
                    classData.put("capacity", bookedClass.getCapacity());
                    classData.put("startTime", bookedClass.getStartTime());
                    classData.put("endTime", bookedClass.getEndTime());
                    classData.put("location", bookedClass.getLocation());
                    classData.put("status", bookedClass.getStatus());

                    String instructor = "—";
                    Coach coach = bookedClass.getCoach();
                    if (coach != null) {
                        if (coach.getName() != null && !coach.getName().trim().isEmpty()) {
                            instructor = coach.getName().trim();
                        } else if (coach.getUsername() != null && !coach.getUsername().trim().isEmpty()) {
                            instructor = coach.getUsername().trim();
                        }
                        classData.put("coachId", coach.getId());
                    }
                    if ("—".equals(instructor) && bookedClass.getLocation() != null && bookedClass.getLocation().startsWith("私教区-")) {
                        instructor = bookedClass.getLocation().substring("私教区-".length());
                    }
                    classData.put("instructor", instructor);

                    // 同时保留扁平字段，方便旧页面或调试时直接读取。
                    bookingData.put("className", bookedClass.getName());
                    bookingData.put("classNameEn", bookedClass.getNameEn());
                    bookingData.put("coachName", instructor);
                    bookingData.put("startTime", bookedClass.getStartTime());
                    bookingData.put("endTime", bookedClass.getEndTime());
                    bookingData.put("classes", classData);
                }

                bookingDataList.add(bookingData);
            }
            
            response.put("success", true);
            response.put("data", bookingDataList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting my bookings: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElse(null);
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Long classId = Long.parseLong(request.get("classId").toString());
            
            // 只判断当前用户是否已经有该课程的有效预约。
            // 旧演示数据会给同一个用户生成大量重复 CONFIRMED 预约，
            // 会导致前端所有课程都显示“已预约”。
            Booking activeBooking = bookingService.getActiveBooking(user.getId(), classId);
            if (activeBooking != null) {
                response.put("success", false);
                response.put("message", "You have already booked this class");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Classes classes = classesService.getClassById(classId);
            if (classes == null) {
                response.put("success", false);
                response.put("message", "Class not found. Please refresh the page and try again.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Booking> existingBookings = bookingService.getBookingsByClassId(classId);
            
            int capacity = classes.getCapacity() != null ? classes.getCapacity() : 0;
            long confirmedCount = existingBookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
            if (capacity > 0 && confirmedCount >= capacity) {
                response.put("success", false);
                response.put("message", "Class is full");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Booking booking = bookingService.createBooking(user, classes, classId);
            
            // 构建安全的响应，避免循环引用
            Map<String, Object> bookingData = new java.util.HashMap<>();
            bookingData.put("id", booking.getId());
            bookingData.put("classId", booking.getClassId());
            bookingData.put("status", booking.getStatus());
            bookingData.put("bookingTime", booking.getBookingTime());
            
            response.put("success", true);
            response.put("data", bookingData);
            response.put("message", "Booking successful");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long id) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            Booking booking = bookingService.cancelBooking(id);
            if (booking == null) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("success", true);
            response.put("data", booking);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/class/{classId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelMyBookingByClass(@PathVariable Long classId) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.getUserByUsername(username).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Booking booking = bookingService.cancelActiveBooking(user.getId(), classId);
            if (booking == null) {
                response.put("success", false);
                response.put("message", "No active booking found for this class");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Map<String, Object> bookingData = new java.util.HashMap<>();
            bookingData.put("id", booking.getId());
            bookingData.put("classId", booking.getClassId());
            bookingData.put("status", booking.getStatus());
            bookingData.put("cancelledAt", booking.getCancelledAt());

            response.put("success", true);
            response.put("data", bookingData);
            response.put("message", "Booking cancelled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error cancelling my booking: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBooking(@PathVariable Long id) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            bookingService.deleteBooking(id);
            response.put("success", true);
            response.put("message", "Booking deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error deleting booking: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
