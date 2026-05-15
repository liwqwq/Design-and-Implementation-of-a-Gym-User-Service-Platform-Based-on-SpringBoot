package com.gym.service.service;

import com.gym.service.entity.User;
import com.gym.service.entity.Membership;
import com.gym.service.repository.UserRepository;
import com.gym.service.repository.MembershipRepository;
import com.gym.service.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getBirthday() != null) {
            user.setBirthday(userDetails.getBirthday());
        }
        if (userDetails.getGender() != null) {
            user.setGender(userDetails.getGender());
        }
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }
        if (userDetails.getMedicalNotes() != null) {
            user.setMedicalNotes(userDetails.getMedicalNotes());
        }
        // 只有当userDetails不为null时才更新active和role
        if (userDetails != null) {
            user.setActive(userDetails.isActive());
            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole());
            }
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        return userRepository.save(user);
    }

    /** Partial update for admin API: only keys present in {@code patch} are applied (avoids Jackson defaulting booleans). */
    public User patchUser(Long id, Map<String, Object> patch) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (patch.containsKey("name") && patch.get("name") != null) {
            user.setName(String.valueOf(patch.get("name")));
        }
        if (patch.containsKey("email") && patch.get("email") != null) {
            user.setEmail(String.valueOf(patch.get("email")));
        }
        if (patch.containsKey("phone") && patch.get("phone") != null) {
            user.setPhone(String.valueOf(patch.get("phone")));
        }
        if (patch.containsKey("gender") && patch.get("gender") != null) {
            user.setGender(String.valueOf(patch.get("gender")));
        }
        if (patch.containsKey("address") && patch.get("address") != null) {
            user.setAddress(String.valueOf(patch.get("address")));
        }
        if (patch.containsKey("active")) {
            Object a = patch.get("active");
            if (a instanceof Boolean) {
                user.setActive((Boolean) a);
            } else {
                user.setActive(Boolean.parseBoolean(String.valueOf(a)));
            }
        }
        if (patch.containsKey("role") && patch.get("role") != null) {
            user.setRole(String.valueOf(patch.get("role")));
        }
        if (patch.containsKey("medicalNotes")) {
            user.setMedicalNotes(patch.get("medicalNotes") == null ? null : String.valueOf(patch.get("medicalNotes")));
        }
        if (patch.containsKey("password")) {
            String pwd = String.valueOf(patch.get("password"));
            if (pwd != null && !pwd.isEmpty() && !"null".equals(pwd)) {
                user.setPassword(passwordEncoder.encode(pwd));
            }
        }
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        bookingRepository.deleteByUserId(id);
        membershipRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
    
    public Optional<Membership> getMembershipByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return membershipRepository.findByUserId(user.getId());
    }
    
    public Membership updateMembership(Membership membership) {
        return membershipRepository.save(membership);
    }

    @Transactional
    public Membership upgradeToVipMembership(String username, String plan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String normalizedPlan = plan == null ? "yearly" : plan.trim().toLowerCase();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate baseStart = today;
        java.time.LocalDate baseEnd = today;
        java.util.Optional<Membership> existingOpt = membershipRepository.findByUserId(user.getId());
        Membership membership = existingOpt.orElseGet(Membership::new);

        if (existingOpt.isPresent()) {
            Membership existing = existingOpt.get();
            if (existing.getEndDate() != null && existing.getEndDate().isAfter(today)) {
                baseStart = existing.getStartDate() != null ? existing.getStartDate() : today;
                baseEnd = existing.getEndDate();
            }
        } else {
            membership.setUserId(user.getId());
        }

        String membershipType;
        java.time.LocalDate newEnd;
        if ("monthly".equals(normalizedPlan)) {
            membershipType = "VIP月卡";
            newEnd = baseEnd.plusMonths(1);
        } else if ("quarterly".equals(normalizedPlan)) {
            membershipType = "VIP季卡";
            newEnd = baseEnd.plusMonths(3);
        } else {
            membershipType = "VIP年卡";
            newEnd = baseEnd.plusYears(1);
        }

        membership.setUserId(user.getId());
        membership.setMembershipType(membershipType);
        membership.setStartDate(baseStart);
        membership.setEndDate(newEnd);
        membership.setStatus("ACTIVE");
        membership.setTotalSessions(Math.max(membership.getTotalSessions(), 60));
        membership.setUsedSessions(Math.max(membership.getUsedSessions(), 0));
        membership.setRemainingSessions(Math.max(membership.getRemainingSessions(), 60 - membership.getUsedSessions()));
        membership.setDaysRemaining((int) java.time.temporal.ChronoUnit.DAYS.between(today, newEnd));

        // VIP 是会员等级，不是登录身份。
        // 登录身份必须保持 USER / COACH / ADMIN，否则用户端登录会被判定为身份不匹配。
        user.setRole("USER");
        userRepository.save(user);
        return membershipRepository.save(membership);
    }
}