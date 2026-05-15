package com.gym.service.config;

import com.gym.service.entity.Booking;
import com.gym.service.entity.CheckinRecord;
import com.gym.service.entity.Classes;
import com.gym.service.entity.User;
import com.gym.service.repository.BookingRepository;
import com.gym.service.repository.CheckinRecordRepository;
import com.gym.service.repository.ClassesRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// @Component - 已被 CompleteDataInitializer 替代
public class RankingDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 5) {
            return;
        }

        List<Classes> classes = createSampleClasses();
        List<User> users = createSampleUsers();
        createBookingsForUsers(users, classes);
        createCheckinRecordsForUsers(users);
    }

    private List<Classes> createSampleClasses() {
        List<Classes> classList = new ArrayList<>();
        String[] classNames = {"瑜伽", "动感单车", "力量训练", "普拉提", "搏击", "舞蹈", "游泳", "有氧操"};
        String[] locations = {"A区教室", "B区动感单车房", "C区力量区", "A区教室", "D区搏击室", "E区舞蹈室", "游泳池", "F区有氧区"};
        
        for (int i = 0; i < 8; i++) {
            Classes c = new Classes();
            c.setName(classNames[i]);
            c.setCapacity(20);
            c.setLocation(locations[i]);
            c.setStartTime(LocalDateTime.now().plusDays(i + 1).withHour(9 + i % 3 * 3));
            c.setEndTime(LocalDateTime.now().plusDays(i + 1).withHour(10 + i % 3 * 3));
            c.setStatus("ACTIVE");
            classList.add(classesRepository.save(c));
        }
        return classList;
    }

    private List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();
        String[] usernames = {"fitness_pro", "gym_master", "yoga_lover", "running_king", "dance_queen", "power_lifter", "fit_newbie", "exercise_addict"};
        String[] names = {"健身狂人", "力量小子", "瑜伽达人", "跑步爱好者", "动感少女", "举重王者", "健身新星", "运动达人"};
        
        for (int i = 0; i < 8; i++) {
            User user = new User();
            user.setUsername(usernames[i]);
            user.setName(names[i]);
            user.setEmail(usernames[i] + "@example.com");
            user.setPhone("138" + String.format("%08d", i + 10000000));
            user.setPassword(passwordEncoder.encode("123456"));
            user.setActive(true);
            user.setRole("USER");
            users.add(userRepository.save(user));
        }
        return users;
    }

    private void createBookingsForUsers(List<User> users, List<Classes> classes) {
        int[] bookingCounts = {256, 220, 198, 175, 156, 142, 128, 115};
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int count = bookingCounts[i];
            
            for (int j = 0; j < count; j++) {
                Booking booking = new Booking();
                booking.setUser(user);
                booking.setClasses(classes.get(random.nextInt(classes.size())));
                booking.setStatus("CONFIRMED");
                
                LocalDateTime bookingTime = LocalDateTime.now().minusDays(random.nextInt(365)).minusHours(random.nextInt(24));
                booking.setBookingTime(bookingTime);
                
                bookingRepository.save(booking);
            }
        }
    }

    private void createCheckinRecordsForUsers(List<User> users) {
        int[] checkinDays = {365, 300, 280, 250, 200, 180, 150, 120};
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int days = checkinDays[i];
            
            for (int j = 0; j < days; j++) {
                CheckinRecord record = new CheckinRecord();
                record.setUserId(user.getId());
                
                LocalDate checkinDate = LocalDate.now().minusDays(days - j - 1);
                record.setCheckinDate(checkinDate);
                record.setCheckinTime(checkinDate.atTime(8 + random.nextInt(12), random.nextInt(60)));
                record.setPointsEarned(10);
                
                checkinRecordRepository.save(record);
            }
        }
    }
}
