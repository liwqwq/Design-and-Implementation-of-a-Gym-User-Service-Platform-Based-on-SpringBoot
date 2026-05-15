package com.gym.service.service;

import com.gym.service.entity.Course;
import com.gym.service.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public Course createCourse(Course course) {
        course.setBookedCount(0);
        if (course.getStatus() == null) {
            course.setStatus("ACTIVE");
        }
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setName(courseDetails.getName());
        course.setDescription(courseDetails.getDescription());
        course.setInstructor(courseDetails.getInstructor());
        course.setCapacity(courseDetails.getCapacity());
        course.setStartTime(courseDetails.getStartTime());
        course.setEndTime(courseDetails.getEndTime());
        course.setStatus(courseDetails.getStatus());
        
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public Course bookCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getBookedCount() >= course.getCapacity()) {
            throw new RuntimeException("Course is full");
        }
        
        if (!"ACTIVE".equals(course.getStatus())) {
            throw new RuntimeException("Course is not active");
        }
        
        course.setBookedCount(course.getBookedCount() + 1);
        return courseRepository.save(course);
    }

    public Course cancelBooking(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getBookedCount() > 0) {
            course.setBookedCount(course.getBookedCount() - 1);
            return courseRepository.save(course);
        }
        
        return course;
    }
}
