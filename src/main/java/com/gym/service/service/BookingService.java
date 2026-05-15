package com.gym.service.service;

import com.gym.service.entity.Booking;
import com.gym.service.entity.Classes;
import com.gym.service.entity.User;
import com.gym.service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getBookingsByClassId(Long classId) {
        return bookingRepository.findByClassesId(classId);
    }

    public Booking getActiveBooking(Long userId, Long classId) {
        List<Booking> activeBookings = bookingRepository.findByUserIdAndClassIdAndStatus(userId, classId, "CONFIRMED");
        return activeBookings.isEmpty() ? null : activeBookings.get(0);
    }

    public Booking createBooking(User user, Classes classes) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setUserId(user.getId());
        booking.setClasses(classes);
        booking.setClassId(classes.getId());
        booking.setBookingTime(java.time.LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }
    
    public Booking createBooking(User user, Classes classes, Long classId) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setUserId(user.getId());
        // 只有当课程存在时才设置关联
        if (classes != null) {
            booking.setClasses(classes);
        }
        booking.setClassId(classId);
        booking.setBookingTime(java.time.LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStatus("CANCELLED");
            booking.setCancelledAt(java.time.LocalDateTime.now());
            return bookingRepository.save(booking);
        }
        return null;
    }

    public Booking cancelActiveBooking(Long userId, Long classId) {
        Booking booking = getActiveBooking(userId, classId);
        if (booking != null) {
            booking.setStatus("CANCELLED");
            booking.setCancelledAt(java.time.LocalDateTime.now());
            return bookingRepository.save(booking);
        }
        return null;
    }

    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
}
