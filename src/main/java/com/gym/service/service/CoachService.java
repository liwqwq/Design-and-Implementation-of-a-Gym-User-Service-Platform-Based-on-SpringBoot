package com.gym.service.service;

import com.gym.service.entity.*;
import com.gym.service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoachService {

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Coach findByUsername(String username) {
        return coachRepository.findByUsername(username).orElse(null);
    }

    public Coach findById(Long id) {
        return coachRepository.findById(id).orElse(null);
    }

    public List<Coach> findAll() {
        return coachRepository.findAll();
    }

    public Coach createCoach(Coach coach) {
        coach.setPassword(passwordEncoder.encode(coach.getPassword()));
        if (coach.getRating() == null) {
            coach.setRating(0.0);
        }
        if (coach.getTotalClasses() == null) {
            coach.setTotalClasses(0);
        }
        if (coach.getTotalStudents() == null) {
            coach.setTotalStudents(0);
        }
        coach.setQrCode("COACH-" + String.format("%06d", coach.getId() != null ? coach.getId() : 1));
        return coachRepository.save(coach);
    }

    public Coach updateCoach(Long id, Coach coachDetails) {
        Coach coach = findById(id);
        if (coach != null) {
            if (coachDetails.getName() != null) {
                coach.setName(coachDetails.getName());
            }
            if (coachDetails.getEmail() != null) {
                coach.setEmail(coachDetails.getEmail());
            }
            if (coachDetails.getPhone() != null) {
                coach.setPhone(coachDetails.getPhone());
            }
            if (coachDetails.getExperienceYears() != null) {
                coach.setExperienceYears(coachDetails.getExperienceYears());
            }
            if (coachDetails.getCertifications() != null) {
                coach.setCertifications(coachDetails.getCertifications());
            }
            if (coachDetails.getSpecialty() != null) {
                coach.setSpecialty(coachDetails.getSpecialty());
            }
            if (coachDetails.getHourlyRate() != null) {
                coach.setHourlyRate(coachDetails.getHourlyRate());
            }
            if (coachDetails.getNotificationSettings() != null) {
                coach.setNotificationSettings(coachDetails.getNotificationSettings());
            }
            return coachRepository.save(coach);
        }
        return null;
    }

    public void deleteCoach(Long id) {
        coachRepository.deleteById(id);
    }

    public boolean authenticate(String username, String password) {
        Coach coach = findByUsername(username);
        return coach != null && passwordEncoder.matches(password, coach.getPassword());
    }

    public List<Classes> getCoachClasses(Long coachId) {
        return classesRepository.findByCoachId(coachId);
    }

    public List<Classes> getTodayClasses(Long coachId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return classesRepository.findByCoachId(coachId).stream()
                .filter(c -> c.getStartTime() != null && 
                        !c.getStartTime().isBefore(startOfDay) && 
                        !c.getStartTime().isAfter(endOfDay))
                .collect(Collectors.toList());
    }

    public Classes addClass(Long coachId, Classes classData) {
        Coach coach = findById(coachId);
        if (coach != null) {
            classData.setCoach(coach);
            classData.setStatus("ACTIVE");
            classData.setCreatedAt(LocalDateTime.now());
            return classesRepository.save(classData);
        }
        return null;
    }

    @Transactional
    public void deleteClassForCoach(Long coachId, Long classId) {
        Classes c = classesRepository.findById(classId).orElse(null);
        if (c == null || c.getCoach() == null || !c.getCoach().getId().equals(coachId)) {
            throw new RuntimeException("无权删除该课程");
        }
        bookingRepository.deleteByClassId(classId);
        classesRepository.deleteById(classId);
    }

    @Transactional
    public Classes updateCoachClass(Long coachId, Long classId, String name, Integer capacity,
                                    LocalDateTime startTime, LocalDateTime endTime, String location) {
        Classes c = classesRepository.findById(classId).orElse(null);
        if (c == null || c.getCoach() == null || !c.getCoach().getId().equals(coachId)) {
            return null;
        }
        if (name != null && !name.isEmpty()) {
            c.setName(name);
        }
        if (capacity != null) {
            c.setCapacity(capacity);
        }
        if (startTime != null) {
            c.setStartTime(startTime);
        }
        if (endTime != null) {
            c.setEndTime(endTime);
        }
        if (location != null && !location.isEmpty()) {
            c.setLocation(location);
        }
        return classesRepository.save(c);
    }

    public List<User> getCoachStudents(Long coachId) {
        List<Classes> coachClasses = getCoachClasses(coachId);
        java.util.Map<Long, User> uniqueStudents = new java.util.LinkedHashMap<>();
        for (Classes c : coachClasses) {
            for (Booking b : bookingRepository.findByClassesId(c.getId())) {
                if (!"CONFIRMED".equalsIgnoreCase(String.valueOf(b.getStatus()))) {
                    continue;
                }
                if (b.getUserId() != null && !uniqueStudents.containsKey(b.getUserId())) {
                    java.util.Optional<User> userOpt = userRepository.findById(b.getUserId());
                    userOpt.ifPresent(user -> uniqueStudents.put(user.getId(), user));
                }
            }
        }
        return new java.util.ArrayList<>(uniqueStudents.values());
    }

    public List<Booking> getClassBookings(Long classId) {
        return bookingRepository.findByClassesId(classId);
    }

    public List<Booking> getConfirmedClassBookings(Long classId) {
        return bookingRepository.findByClassesId(classId).stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(String.valueOf(b.getStatus())))
                .collect(Collectors.toList());
    }

    public List<Booking> getPendingClassBookings(Long classId) {
        return bookingRepository.findByClassesId(classId).stream()
                .filter(b -> "PENDING".equalsIgnoreCase(String.valueOf(b.getStatus())))
                .collect(Collectors.toList());
    }

    public String getUserMembershipType(Long userId) {
        if (userId == null) {
            return "普通会员";
        }
        java.util.Optional<Membership> membershipOpt = membershipRepository.findByUserId(userId);
        if (membershipOpt.isPresent()) {
            Membership membership = membershipOpt.get();
            boolean active = "ACTIVE".equalsIgnoreCase(String.valueOf(membership.getStatus()))
                    || "有效".equals(String.valueOf(membership.getStatus()))
                    || "active".equalsIgnoreCase(String.valueOf(membership.getStatus()));
            boolean notExpired = membership.getEndDate() == null || !membership.getEndDate().isBefore(LocalDate.now());
            String type = String.valueOf(membership.getMembershipType());
            if (active && notExpired && type.toLowerCase().contains("vip")) {
                return "VIP会员";
            }
        }
        return "普通会员";
    }

    public void saveNotificationSettings(Long coachId, String serializedSettings) {
        Coach coach = findById(coachId);
        if (coach != null) {
            coach.setNotificationSettings(serializedSettings);
            coachRepository.save(coach);
        }
    }

    public List<Review> getCoachReviews(Long coachId) {
        return reviewRepository.findByCoachIdOrderByCreatedAtDesc(coachId);
    }

    public Review respondToReview(Long reviewId, String response, String responseEn) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review != null) {
            review.setResponded(true);
            review.setResponse(response);
            if (responseEn != null && !responseEn.trim().isEmpty()) {
                review.setResponseEn(responseEn.trim());
            }
            review.setRespondedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        return null;
    }

    public List<Complaint> getCoachComplaints(Long coachId) {
        return complaintRepository.findByCoachIdOrderByCreatedAtDesc(coachId);
    }

    public Complaint respondToComplaint(Long complaintId, String response, String responseEn) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint != null) {
            complaint.setStatus("processed");
            complaint.setResponse(response);
            if (responseEn != null && !responseEn.trim().isEmpty()) {
                complaint.setResponseEn(responseEn.trim());
            }
            complaint.setProcessedAt(LocalDateTime.now());
            return complaintRepository.save(complaint);
        }
        return null;
    }

    public List<Facility> getFacilities() {
        return facilityRepository.findAll();
    }

    public FacilityBooking bookFacility(Long coachId, Long facilityId, 
            LocalDateTime startTime, LocalDateTime endTime) {
        Coach coach = findById(coachId);
        Facility facility = facilityRepository.findById(facilityId).orElse(null);
        
        if (coach != null && facility != null) {
            for (FacilityBooking existing : getFacilityBookings(facilityId)) {
                if (existing.getStartTime() == null || existing.getEndTime() == null) {
                    continue;
                }
                boolean overlaps = startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime());
                if (overlaps) {
                    return null;
                }
            }

            FacilityBooking booking = new FacilityBooking();
            booking.setCoach(coach);
            booking.setFacility(facility);
            booking.setStartTime(startTime);
            booking.setEndTime(endTime);
            booking.setStatus("confirmed");
            booking.setCreatedAt(LocalDateTime.now());
            return facilityBookingRepository.save(booking);
        }
        return null;
    }

    public List<FacilityBooking> getCoachFacilityBookings(Long coachId) {
        return facilityBookingRepository.findByCoachIdOrderByStartTimeDesc(coachId);
    }

    @Transactional
    public boolean cancelFacilityBooking(Long coachId, Long bookingId) {
        FacilityBooking booking = facilityBookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getCoach() == null || !booking.getCoach().getId().equals(coachId)) {
            return false;
        }
        if (!"confirmed".equalsIgnoreCase(String.valueOf(booking.getStatus()))) {
            return false;
        }

        Facility facility = booking.getFacility();
        if (facility != null && booking.getStartTime() != null && booking.getEndTime() != null) {
            boolean alreadyUsedByClass = getCoachClasses(coachId).stream()
                    .filter(c -> facility.getName().equals(c.getLocation()))
                    .filter(c -> c.getStartTime() != null && c.getEndTime() != null)
                    .anyMatch(c -> c.getStartTime().isBefore(booking.getEndTime())
                            && c.getEndTime().isAfter(booking.getStartTime()));
            if (alreadyUsedByClass) {
                throw new RuntimeException("该场地预约已用于课程，不能直接取消");
            }
        }

        facilityBookingRepository.deleteById(bookingId);
        return true;
    }

    public List<FacilityBooking> getFacilityBookings(Long facilityId) {
        List<FacilityBooking> all = new java.util.ArrayList<>();
        all.addAll(facilityBookingRepository.findByFacilityIdAndStatus(facilityId, "confirmed"));
        all.addAll(facilityBookingRepository.findByFacilityIdAndStatus(facilityId, "CONFIRMED"));
        return all.stream().distinct().collect(Collectors.toList());
    }

    public int getActiveUsersCount() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return (int) checkinRecordRepository.findByCheckinTimeAfter(oneHourAgo).stream()
                .map(CheckinRecord::getUserId)
                .distinct()
                .count();
    }

    @Autowired
    private ClassesRepository classesRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private FacilityRepository facilityRepository;
    
    @Autowired
    private FacilityBookingRepository facilityBookingRepository;

    @Autowired
    private MembershipRepository membershipRepository;
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
}