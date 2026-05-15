package com.gym.service.service;

import com.gym.service.entity.Classes;
import com.gym.service.entity.Coach;
import com.gym.service.repository.BookingRepository;
import com.gym.service.repository.ClassesRepository;
import com.gym.service.repository.CoachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassesService {
    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Classes> getAllClasses() {
        return classesRepository.findAll();
    }

    public Classes getClassById(Long id) {
        return classesRepository.findById(id).orElse(null);
    }

    public Classes createClass(String name, Integer capacity, String startTime, String endTime, String location, String status) {
        return createClassWithCoach(name, capacity, startTime, endTime, location, status, null);
    }

    public Classes createClassWithCoach(String name, Integer capacity, String startTime, String endTime, String location, String status, Long coachId) {
        Classes classes = new Classes();
        classes.setName(name);
        classes.setCapacity(capacity);
        if (startTime != null && !startTime.isEmpty()) {
            classes.setStartTime(LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            classes.setEndTime(LocalDateTime.parse(endTime));
        }
        classes.setLocation(location);
        classes.setStatus(status != null && !status.isEmpty() ? status : "ACTIVE");
        classes.setCreatedAt(LocalDateTime.now());
        if (coachId != null) {
            coachRepository.findById(coachId).ifPresent(classes::setCoach);
        }
        return classesRepository.save(classes);
    }

    public Classes updateClass(Long id, String name, Integer capacity, String startTime, String endTime, String location, String status) {
        return updateClassWithCoach(id, name, capacity, startTime, endTime, location, status, null);
    }

    public Classes updateClassWithCoach(Long id, String name, Integer capacity, String startTime, String endTime, String location, String status, Long coachId) {
        Classes classes = classesRepository.findById(id).orElse(null);
        if (classes == null) {
            return null;
        }
        if (name != null) {
            classes.setName(name);
        }
        if (capacity != null) {
            classes.setCapacity(capacity);
        }
        if (startTime != null && !startTime.isEmpty()) {
            classes.setStartTime(LocalDateTime.parse(startTime));
        }
        if (endTime != null && !endTime.isEmpty()) {
            classes.setEndTime(LocalDateTime.parse(endTime));
        }
        if (location != null) {
            classes.setLocation(location);
        }
        if (status != null) {
            classes.setStatus(status);
        }
        if (coachId != null) {
            Coach coach = coachRepository.findById(coachId).orElse(null);
            classes.setCoach(coach);
        }
        return classesRepository.save(classes);
    }

    @Transactional
    public void deleteClass(Long id) {
        bookingRepository.deleteByClassId(id);
        classesRepository.deleteById(id);
    }
}
