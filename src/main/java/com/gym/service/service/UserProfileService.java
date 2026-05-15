package com.gym.service.service;

import com.gym.service.entity.User;
import com.gym.service.entity.UserProfile;
import com.gym.service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    public UserProfile createOrUpdateProfile(User user, String fullName, String avatarUrl, String gender, String dateOfBirth, String phone) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }
        profile.setFullName(fullName);
        profile.setAvatarUrl(avatarUrl);
        profile.setGender(gender);
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            profile.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
        }
        profile.setPhone(phone);
        return userProfileRepository.save(profile);
    }
}
