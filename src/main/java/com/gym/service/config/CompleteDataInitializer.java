package com.gym.service.config;

import com.gym.service.entity.*;
import com.gym.service.repository.*;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class CompleteDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckinRecordRepository checkinRecordRepository;

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private PointsProductRepository pointsProductRepository;

    @Autowired
    private PointsExchangeRepository pointsExchangeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private PersonalTrainerRepository personalTrainerRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        boolean resetDemoData = "true".equalsIgnoreCase(System.getenv().getOrDefault("RESET_DEMO_DATA", "false"));
        // 场地是课程联动的基础数据。即使不重置演示数据，也要补齐缺失场地，
        // 避免旧数据库里只有“训练区A/B”等少量场地，导致课程地点无法预约。
        ensureFacilities();
        ensurePointsProductTranslations();
        syncLegacyProductsIntoPointsMall();
        // 即使不重置演示数据，也要检查演示登录账号。
        // 这样可以避免管理员测试编辑后，把 fitness_pro 等测试账号的 role/password 改乱，导致用户端登录失败。
        ensureDemoLoginAccounts();
        ensureAdminDemoAccount();
        ensureCoachDemoAccounts();
        if (!resetDemoData && adminRepository.count() > 0 && userRepository.count() > 0) {
            System.out.println("检测到已有管理员和用户数据，已检查场地和演示登录账号，跳过演示数据重置。需要重置时可在启动前设置 RESET_DEMO_DATA=true。");
            return;
        }

        if (resetDemoData) {
            System.out.println("RESET_DEMO_DATA=true，开始重置演示数据...");
        } else {
            System.out.println("数据库为空或核心数据不完整，开始初始化演示数据...");
        }

        // 只在首次初始化或显式重置时清空旧数据，避免管理员编辑、删除、新增的数据每次重启都丢失。
        reviewRepository.deleteAll();
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        blacklistRepository.deleteAll();
        advertisementRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        reportRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        pointsExchangeRepository.deleteAll();
        checkinRecordRepository.deleteAll();
        bookingRepository.deleteAll();
        pointsProductRepository.deleteAll();
        pointsRepository.deleteAll();
        productRepository.deleteAll();
        classesRepository.deleteAll();
        userRepository.deleteAll();
        adminRepository.deleteAll();
        System.out.println("旧数据清空完成！");
        
        System.out.println("开始初始化完整数据库数据...");
        
        // 创建管理员
        createAdmin();

        // 教练账号需在课程之前创建，以便课程关联 Coach 实体
        createCoaches();

        ensureFacilities();
        
        // 创建课程
        List<Classes> classes = createClasses();
        
        // 创建用户
        List<User> users = createUsers();

        createSampleMemberships(users);
        
        // 创建预约记录
        createBookings(users, classes);
        
        // 创建打卡记录
        createCheckinRecords(users);
        
        // 创建积分商品
        createPointsProducts();
        
        // 创建普通商品
        createProducts();
        syncLegacyProductsIntoPointsMall();
        
        // 创建社区帖子和评论
        createCommunityContent(users);
        
        // 创建组队
        createTeams(users);
        
        // 创建广告
        createAdvertisements();
        
        // 创建私教教练
        createPersonalTrainers(users);
        
            // 创建徽章
            createBadges(users);

            // 教练信箱：投诉与课程评价（关联真实教练/用户/课程）
            createCoachMailboxData(users, classes);
            
            System.out.println("数据库初始化完成！");
        }

    private void createAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setEmail("admin@fitlife.com");
            adminRepository.save(admin);
            System.out.println("创建管理员账号: admin / admin123, email: admin@fitlife.com");
        }
    }

    /**
     * 修复/补齐演示登录账号。
     * 这些账号会显示在登录页，用于答辩和本地测试，因此每次启动都保持可登录状态。
     * 不会清空业务数据，只修正测试账号的 active、role 和默认密码。
     */
    private void ensureDemoLoginAccounts() {
        ensureUserDemoAccount("fitness_pro", "健身狂人", "fitness_pro@example.com", "13810000000");
        ensureUserDemoAccount("gym_master", "力量小子", "gym_master@example.com", "13810000001");
        ensureUserDemoAccount("yoga_lover", "瑜伽达人", "yoga_lover@example.com", "13810000002");
    }

    private void ensureUserDemoAccount(String username, String name, String email, String phone) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        User user = userOpt.orElseGet(User::new);
        user.setUsername(username);
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(name);
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            user.setEmail(email);
        }
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            user.setPhone(phone);
        }
        user.setActive(true);
        user.setRole("USER");
        if (user.getPassword() == null || !passwordEncoder.matches("123456", user.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        userRepository.save(user);
    }

    /**
     * 修复/补齐管理员演示账号，避免管理员端登录页显示的 admin / admin123 因旧数据而无法登录。
     */
    private void ensureAdminDemoAccount() {
        Admin admin = adminRepository.findByUsername("admin").orElseGet(Admin::new);
        admin.setUsername("admin");
        if (admin.getName() == null || admin.getName().trim().isEmpty()) {
            admin.setName("系统管理员");
        }
        if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
            admin.setEmail("admin@fitlife.com");
        }
        if (admin.getPassword() == null || !passwordEncoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
        }
        adminRepository.save(admin);
    }

    /**
     * 修复/补齐教练演示账号，确保 coach_1 ~ coach_4 / 123456 在旧数据库中也可登录。
     */
    private void ensureCoachDemoAccounts() {
        String[] names = {"张教练", "李教练", "王教练", "赵教练"};
        String[] specialties = {
                "力量训练、增肌塑形、功能性训练",
                "瑜伽放松、普拉提、柔韧性训练",
                "动感单车、有氧训练、减脂",
                "搏击训练、体能训练、爆发力"
        };
        for (int i = 0; i < names.length; i++) {
            String username = "coach_" + (i + 1);
            Coach coach = coachRepository.findByUsername(username).orElseGet(Coach::new);
            coach.setUsername(username);
            if (coach.getName() == null || coach.getName().trim().isEmpty()) {
                coach.setName(names[i]);
            }
            if (coach.getEmail() == null || coach.getEmail().trim().isEmpty()) {
                coach.setEmail("coach" + (i + 1) + "@fitlife.com");
            }
            if (coach.getPhone() == null || coach.getPhone().trim().isEmpty()) {
                coach.setPhone("1380000000" + (i + 1));
            }
            if (coach.getExperienceYears() == null) {
                coach.setExperienceYears(5 + i);
            }
            if (coach.getCertifications() == null || coach.getCertifications().trim().isEmpty()) {
                coach.setCertifications("国家级健身教练认证、高级私教证书");
            }
            if (coach.getSpecialty() == null || coach.getSpecialty().trim().isEmpty()) {
                coach.setSpecialty(specialties[i]);
            }
            if (coach.getHourlyRate() == null) {
                coach.setHourlyRate(300.0 + i * 50);
            }
            if (coach.getRating() == null) {
                coach.setRating(4.8 + i * 0.1);
            }
            if (coach.getTotalClasses() == null) {
                coach.setTotalClasses(500 + i * 100);
            }
            if (coach.getTotalStudents() == null) {
                coach.setTotalStudents(100 + i * 50);
            }
            if (coach.getQrCode() == null || coach.getQrCode().trim().isEmpty()) {
                coach.setQrCode("COACH-" + String.format("%06d", i + 1));
            }
            if (coach.getPassword() == null || !passwordEncoder.matches("123456", coach.getPassword())) {
                coach.setPassword(passwordEncoder.encode("123456"));
            }
            coachRepository.save(coach);
        }
    }

    private List<Classes> createClasses() {
        List<Classes> classList = new ArrayList<>();
        String[] classNames = {"力量训练", "瑜伽放松", "动感单车", "普拉提", "搏击", "舞蹈", "游泳", "有氧操"};
        String[] classNamesEn = {"Strength Training", "Yoga Relaxation", "Spinning", "Pilates", "Boxing", "Dance", "Swimming", "Aerobics"};
        String[] locations = {"C区力量区", "A区教室", "B区动感单车房", "A区教室", "D区搏击室", "E区舞蹈室", "游泳池", "F区有氧区"};
        List<Coach> coaches = coachRepository.findAll();

        for (int i = 0; i < 8; i++) {
            Classes c = new Classes();
            c.setName(classNames[i]);
            c.setNameEn(classNamesEn[i]);
            // 这里初始化的是用户端“团体课”课程，统一使用大班容量。
            // 私教课由 TrainerController 按真实课程记录单独创建，避免团课和私教课混在一起。
            c.setCapacity(25);
            c.setLocation(locations[i]);
            LocalDateTime startTime;
            if (i < 3) {
                startTime = LocalDateTime.now().withHour(18 + i).withMinute(0).withSecond(0);
            } else {
                startTime = LocalDateTime.now().plusDays(1).withHour(9 + (i - 3) % 3 * 3).withMinute(0).withSecond(0);
            }
            c.setStartTime(startTime);
            c.setEndTime(startTime.plusHours(1));
            c.setStatus("ACTIVE");
            c.setCreatedAt(LocalDateTime.now());
            if (!coaches.isEmpty()) {
                c.setCoach(coaches.get(i % coaches.size()));
            }
            classList.add(classesRepository.save(c));
        }
        System.out.println("创建了 " + classList.size() + " 个课程");
        return classList;
    }

    private void createSampleMemberships(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            Membership m = new Membership();
            m.setUserId(u.getId());
            m.setMembershipType(i % 3 == 0 ? "VIP年卡" : "标准会员");
            LocalDate start = (i == 0) ? firstOfMonth : today.minusMonths(1 + (i % 3));
            m.setStartDate(start);
            m.setEndDate(today.plusYears(1));
            m.setStatus("ACTIVE");
            m.setTotalSessions(30);
            m.setUsedSessions(5 + i);
            m.setRemainingSessions(Math.max(0, 30 - m.getUsedSessions()));
            m.setDaysRemaining(365);
            membershipRepository.save(m);
        }
        System.out.println("创建了 " + users.size() + " 条会员资格记录");
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        String[] usernames = {"fitness_pro", "gym_master", "yoga_lover", "running_king", 
                            "dance_queen", "power_lifter", "fit_newbie", "exercise_addict"};
        String[] names = {"健身狂人", "力量小子", "瑜伽达人", "跑步爱好者", 
                        "动感少女", "举重王者", "健身新星", "运动达人"};
        
        for (int i = 0; i < 8; i++) {
            User user = new User();
            user.setUsername(usernames[i]);
            user.setName(names[i]);
            user.setEmail(usernames[i] + "@example.com");
            user.setPhone("138" + String.format("%08d", i + 10000000));
            user.setPassword(passwordEncoder.encode("123456"));
            user.setActive(true);
            user.setRole("USER");
            user.setGender(i % 2 == 0 ? "male" : "female");
            user.setBirthday(LocalDate.of(1990 + i, 6, 15));
            user.setAddress("健身市健身区健身路" + (i + 1) + "号");
            if (i == 1) {
                user.setMedicalNotes("高血压，需注意有氧强度");
            } else if (i == 4) {
                user.setMedicalNotes("膝盖旧伤，避免大重量深蹲");
            }
            users.add(userRepository.save(user));
        }
        System.out.println("创建了 " + users.size() + " 个用户（密码均为 123456）");
        return users;
    }

    private void createBookings(List<User> users, List<Classes> classes) {
        int totalBookings = 0;

        if (users == null || users.isEmpty() || classes == null || classes.isEmpty()) {
            System.out.println("创建了 0 条预约记录");
            return;
        }

        // 演示数据保留为少量真实预约，但让不同用户的预约节数不同，
        // 这样排行榜不是“人人 1 节”的静态效果。
        // fitness_pro 仍只默认预约 1 节，方便测试“取消预约”和“再次预约”。
        int[][] demoBookings = {
                {0, 0},
                {1, 0}, {1, 1}, {1, 2},
                {2, 1}, {2, 3}, {2, 4}, {2, 5},
                {3, 2}, {3, 6},
                {4, 3}, {4, 5},
                {5, 0}, {5, 2}, {5, 4}, {5, 6}, {5, 7},
                {6, 5},
                {7, 6}, {7, 7}
        };

        for (int[] pair : demoBookings) {
            int userIndex = pair[0];
            int classIndex = pair[1];
            if (userIndex >= users.size() || classIndex >= classes.size()) {
                continue;
            }

            User user = users.get(userIndex);
            Classes cls = classes.get(classIndex);

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setUserId(user.getId());
            booking.setClasses(cls);
            booking.setClassId(cls.getId());
            booking.setStatus("CONFIRMED");
            booking.setBookingTime(LocalDateTime.now().minusMinutes(10 + totalBookings));

            bookingRepository.save(booking);
            totalBookings++;
        }

        System.out.println("创建了 " + totalBookings + " 条预约记录");
    }

    private void createCheckinRecords(List<User> users) {
        // 这里只初始化少量、可验证的历史打卡数据。
        // 不再给测试账号写入 365 天打卡，否则页面会误以为所有数据都是真实用户行为。
        int[] historyDays = {14, 6, 29, 12, 3, 0, 8, 1};
        int[] basePoints = {1200, 900, 1100, 800, 650, 500, 720, 600};
        int totalCheckins = 0;

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int days = i < historyDays.length ? historyDays[i] : 0;
            int base = i < basePoints.length ? basePoints[i] : 500;
            int checkinPoints = days * 10;
            int usedPoints = Math.min(80 + i * 5, base + checkinPoints);

            // 创建积分记录：保留一定演示积分，但连续打卡天数来自真实打卡记录。
            Points points = new Points();
            points.setUserId(user.getId());
            points.setTotalPoints(base + checkinPoints);
            points.setAvailablePoints(Math.max(0, base + checkinPoints - usedPoints));
            points.setUsedPoints(usedPoints);
            points.setConsecutiveDays(days);
            points.setLastCheckinDate(days > 0 ? LocalDateTime.now().minusDays(1) : null);
            points.setCreatedAt(LocalDateTime.now().minusDays(Math.max(days, 1)));
            points.setUpdatedAt(LocalDateTime.now());
            pointsRepository.save(points);

            // 只生成“今天之前”的连续历史记录。这样用户登录后可以真实点击“今日打卡”，
            // 点击后日历、连续天数、积分都会通过接口动态更新。
            for (int j = days; j >= 1; j--) {
                LocalDate checkinDate = LocalDate.now().minusDays(j);
                CheckinRecord record = new CheckinRecord();
                record.setUserId(user.getId());
                record.setCheckinDate(checkinDate);
                record.setCheckinTime(checkinDate.atTime(8 + random.nextInt(12), random.nextInt(60)));
                record.setPointsEarned(10);

                checkinRecordRepository.save(record);
                totalCheckins++;
            }
        }
        System.out.println("创建了 " + totalCheckins + " 条历史打卡记录（不含今日，便于演示实时打卡）");
    }



    private void syncLegacyProductsIntoPointsMall() {
        if (productRepository.count() == 0) {
            return;
        }
        int created = 0;
        for (Product legacy : productRepository.findAll()) {
            if (legacy.getName() == null || legacy.getName().trim().isEmpty()) {
                continue;
            }
            if (pointsProductRepository.findFirstByName(legacy.getName()).isPresent()) {
                continue;
            }
            String[] translation = productTranslation(legacy.getName());
            PointsProduct product = new PointsProduct();
            product.setName(legacy.getName());
            product.setNameEn(translation == null ? null : translation[0]);
            product.setDescription(legacy.getDescription() == null ? "" : legacy.getDescription());
            product.setDescriptionEn(translation == null ? null : translation[1]);
            product.setPointsCost(Math.max(0, (int) Math.round(legacy.getPrice())));
            product.setCategory(legacy.getCategory() == null ? "运动装备" : legacy.getCategory());
            product.setDiscountType("PRODUCT");
            product.setIcon(defaultPointsIcon(legacy.getName(), legacy.getCategory()));
            product.setStockQuantity(Math.max(0, legacy.getStockQuantity()));
            product.setSoldCount(0);
            product.setIsActive(legacy.getIsActive() == null ? true : legacy.getIsActive());
            product.setIsVip(false);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            pointsProductRepository.save(product);
            created++;
        }
        if (created > 0) {
            System.out.println("已将 " + created + " 个旧商品同步到积分商城商品表");
        }
    }

    private String defaultPointsIcon(String name, String category) {
        String text = ((name == null ? "" : name) + " " + (category == null ? "" : category)).toLowerCase(Locale.ROOT);
        if (text.contains("券") || text.contains("coupon")) return "🎟️";
        if (text.contains("瑜伽") || text.contains("yoga")) return "🧘";
        if (text.contains("蛋白") || text.contains("protein")) return "💪";
        if (text.contains("衣") || text.contains("shirt") || text.contains("t恤")) return "👕";
        if (text.contains("水") || text.contains("bottle")) return "🥤";
        if (text.contains("手环") || text.contains("watch") || text.contains("band")) return "⌚";
        if (text.contains("跳绳") || text.contains("rope")) return "🪢";
        return "🎁";
    }

    private String[] productTranslation(String name) {
        if (name == null) return null;
        Map<String, String[]> translations = new HashMap<>();
        translations.put("专业瑜伽垫", new String[]{"Professional Yoga Mat", "Thick non-slip eco-friendly yoga mat"});
        translations.put("健身手套", new String[]{"Fitness Gloves", "Breathable anti-slip training gloves"});
        translations.put("运动水杯", new String[]{"Sports Water Cup", "Large-capacity portable water cup"});
        translations.put("弹力带套装", new String[]{"Resistance Band Set", "Multi-resistance full-body training bands"});
        translations.put("瑜伽球", new String[]{"Yoga Ball", "Thick anti-burst yoga ball"});
        translations.put("健身护膝", new String[]{"Fitness Knee Support", "Stable knee protection for workouts"});
        translations.put("跳绳", new String[]{"Speed Jump Rope", "Professional speed skipping rope"});
        translations.put("泡沫轴", new String[]{"Foam Roller", "Useful tool for muscle relaxation"});
        return translations.get(name);
    }

    private void ensurePointsProductTranslations() {
        if (pointsProductRepository.count() == 0) {
            return;
        }
        Map<String, String[]> translations = new HashMap<>();
        translations.put("5元无门槛券", new String[]{"No-threshold ¥5 coupon", "Save ¥5 with no minimum spend"});
        translations.put("10元实付券", new String[]{"¥10 payment coupon", "Use when actual payment reaches ¥100"});
        translations.put("15元满减券", new String[]{"¥15 discount coupon", "Use when actual payment reaches ¥200"});
        translations.put("30元满减券", new String[]{"¥30 discount coupon", "Use when actual payment reaches ¥300"});
        translations.put("运动水壶", new String[]{"Sports Water Bottle", "Large-capacity portable sports bottle"});
        translations.put("瑜伽垫", new String[]{"Yoga Mat", "Thick non-slip yoga mat"});
        translations.put("健身护腕", new String[]{"Fitness Wrist Wraps", "Professional wrist support for training"});
        translations.put("速干T恤", new String[]{"Quick-dry T-shirt", "Breathable quick-dry workout T-shirt"});
        translations.put("VIP专属-蛋白粉", new String[]{"VIP Protein Powder", "High-protein fitness supplement"});
        translations.put("VIP专属-智能手环", new String[]{"VIP Smart Fitness Band", "Smart wearable band for fitness tracking"});
        translations.put("免费私教课", new String[]{"Free Personal Training Session", "One private coaching session worth ¥200"});
        translations.put("免费团课体验", new String[]{"Free Group Class Trial", "One trial for any group class"});
        translations.put("会员积分翻倍", new String[]{"Double Check-in Points", "Double check-in points for 7 days after redemption"});
        translations.put("专业瑜伽垫", new String[]{"Professional Yoga Mat", "Thick non-slip eco-friendly yoga mat"});
        translations.put("健身手套", new String[]{"Fitness Gloves", "Breathable anti-slip training gloves"});
        translations.put("运动水杯", new String[]{"Sports Water Cup", "Large-capacity portable water cup"});
        translations.put("弹力带套装", new String[]{"Resistance Band Set", "Multi-resistance full-body training bands"});
        translations.put("瑜伽球", new String[]{"Yoga Ball", "Thick anti-burst yoga ball"});
        translations.put("健身护膝", new String[]{"Fitness Knee Support", "Stable knee protection for workouts"});
        translations.put("跳绳", new String[]{"Speed Jump Rope", "Professional speed skipping rope"});
        translations.put("泡沫轴", new String[]{"Foam Roller", "Useful tool for muscle relaxation"});

        int updated = 0;
        for (PointsProduct product : pointsProductRepository.findAll()) {
            String[] value = translations.get(product.getName());
            if (value == null) {
                continue;
            }
            boolean changed = false;
            if (product.getNameEn() == null || product.getNameEn().trim().isEmpty()) {
                product.setNameEn(value[0]);
                changed = true;
            }
            if (product.getDescriptionEn() == null || product.getDescriptionEn().trim().isEmpty()) {
                product.setDescriptionEn(value[1]);
                changed = true;
            }
            if (changed) {
                product.setUpdatedAt(LocalDateTime.now());
                pointsProductRepository.save(product);
                updated++;
            }
        }
        if (updated > 0) {
            System.out.println("已补齐 " + updated + " 个积分商城商品的英文名称/描述");
        }
    }

    private void createPointsProducts() {
        List<PointsProduct> products = Arrays.asList(
            createPointsProduct("5元无门槛券", "No-threshold ¥5 coupon", "满0元可用，直减5元", "Save ¥5 with no minimum spend", 10, 0, 5, "COUPON", "🎟️", "优惠券", 999, 56, false, 30),
            createPointsProduct("10元实付券", "¥10 payment coupon", "实付满100元可用", "Use when actual payment reaches ¥100", 30, 100, 10, "COUPON", "🎫", "优惠券", 888, 134, false, 30),
            createPointsProduct("15元满减券", "¥15 discount coupon", "实付满200元可用", "Use when actual payment reaches ¥200", 50, 200, 15, "COUPON", "🎀", "优惠券", 777, 89, false, 30),
            createPointsProduct("30元满减券", "¥30 discount coupon", "实付满300元可用", "Use when actual payment reaches ¥300", 80, 300, 30, "COUPON", "🎁", "优惠券", 555, 45, false, 30),
            createPointsProduct("运动水壶", "Sports Water Bottle", "大容量运动水壶，便携耐用", "Large-capacity portable sports bottle", 200, null, null, "PRODUCT", "🏋️", "运动装备", 100, 78, false, null),
            createPointsProduct("瑜伽垫", "Yoga Mat", "加厚防滑瑜伽垫", "Thick non-slip yoga mat", 300, null, null, "PRODUCT", "🧘", "运动装备", 80, 56, false, null),
            createPointsProduct("健身护腕", "Fitness Wrist Wraps", "专业健身护腕", "Professional wrist support for training", 150, null, null, "PRODUCT", "🤝", "运动装备", 200, 123, false, null),
            createPointsProduct("速干T恤", "Quick-dry T-shirt", "透气速干运动T恤", "Breathable quick-dry workout T-shirt", 250, null, null, "PRODUCT", "👕", "运动装备", 120, 89, false, null),
            createPointsProduct("VIP专属-蛋白粉", "VIP Protein Powder", "高蛋白健身蛋白粉", "High-protein fitness supplement", 500, null, null, "PRODUCT", "💪", "VIP专属", 50, 34, true, null),
            createPointsProduct("VIP专属-智能手环", "VIP Smart Fitness Band", "运动智能手环", "Smart wearable band for fitness tracking", 800, null, null, "PRODUCT", "⌚", "VIP专属", 30, 23, true, null),
            createPointsProduct("免费私教课", "Free Personal Training Session", "价值200元的一对一私教课1节", "One private coaching session worth ¥200", 1000, null, null, "SERVICE", "🎯", "会员服务", 20, 12, false, 7),
            createPointsProduct("免费团课体验", "Free Group Class Trial", "任意团课体验1次", "One trial for any group class", 300, null, null, "SERVICE", "📝", "会员服务", 50, 45, false, 14),
            createPointsProduct("会员积分翻倍", "Double Check-in Points", "购买后7天内签到积分翻倍", "Double check-in points for 7 days after redemption", 400, null, null, "PRIVILEGE", "✨", "会员特权", 100, 67, false, 7)
        );

        pointsProductRepository.saveAll(products);
        System.out.println("创建了 " + products.size() + " 个积分商品");
    }

    private PointsProduct createPointsProduct(String name, String nameEn, String description, String descriptionEn, int pointsCost, 
                                           Integer minSpend, Integer discountAmount, String discountType,
                                           String icon, String category, int stock, int sold, boolean isVip, Integer expireDays) {
        PointsProduct product = new PointsProduct();
        product.setName(name);
        product.setNameEn(nameEn);
        product.setDescription(description);
        product.setDescriptionEn(descriptionEn);
        product.setPointsCost(pointsCost);
        product.setMinSpend(minSpend);
        product.setDiscountAmount(discountAmount);
        product.setDiscountType(discountType);
        product.setIcon(icon);
        product.setCategory(category);
        product.setStockQuantity(stock);
        product.setSoldCount(sold);
        product.setIsVip(isVip);
        product.setIsActive(true);
        product.setExpireDays(expireDays);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private void createProducts() {
        List<Product> products = Arrays.asList(
            createProduct("专业瑜伽垫", "加厚防滑，环保材质", 129.00, "运动装备", 156),
            createProduct("健身手套", "透气防滑，保护双手", 59.00, "运动装备", 234),
            createProduct("运动水杯", "大容量，便携耐用", 49.00, "运动装备", 567),
            createProduct("弹力带套装", "多种阻力，全身训练", 79.00, "训练辅助", 345),
            createProduct("瑜伽球", "加厚防爆，瑜伽必备", 89.00, "运动装备", 234),
            createProduct("健身护膝", "保护膝盖，稳定支撑", 99.00, "运动装备", 189),
            createProduct("跳绳", "专业竞速跳绳", 45.00, "训练辅助", 456),
            createProduct("泡沫轴", "肌肉放松必备", 69.00, "训练辅助", 289)
        );

        productRepository.saveAll(products);
        System.out.println("创建了 " + products.size() + " 个普通商品");
    }

    private Product createProduct(String name, String description, double price, String category, int sales) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setStockQuantity(100);
        product.setImageUrl("");
        return product;
    }

    private void createCommunityContent(List<User> users) {
        String[] postContents = {
            "今天的训练感觉特别好，状态满满！给大家分享一下我今天的训练计划。首先是热身15分钟，然后是力量训练45分钟，最后是拉伸放松。坚持就是胜利！💪",
            "这是我坚持了半年的健身计划，每周三练，每次一小时。效果真的很明显，分享给大家！希望能帮助到想要健身的朋友。",
            "刚开始健身不久，很多东西都不太懂，希望大佬们多多指教！有什么好的建议吗？",
            "太开心了！今天终于能举起以前不敢想的重量了！感谢教练的指导，也感谢自己的坚持。继续加油！🎉",
            "健身一年，从最初的跑两步都累，到现在可以跑5公里不喘气。变化真的很大，分享一下我的心得。",
            "用了很多健身装备，给大家推荐几款性价比很高的！首先是...",
            "健身餐总是吃不下，有没有什么好吃又健康的做法推荐？",
            "今日打卡完成！连续打卡第30天，继续保持！",
            "周末晨跑缺人，有没有伙伴一起？集合地点：中央公园东门，配速友好～",
            "想组一个小班练核心，每天晚上7点健身房见，上限6人。",
            "大家今天练了什么？来评论区互相打卡鼓励一下！",
            "新人报道，求推荐适合入门的团课～"
        };

        String[] categories = {"share", "share", "help", "share", "share", "share", "help", "share", "team", "team", "chat", "chat"};

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < postContents.length; i++) {
            User user = users.get(i % users.size());
            Post post = new Post();
            post.setUserId(user.getId());
            post.setUsername(user.getName());
            post.setContent(postContents[i]);
            post.setLikes(random.nextInt(100) + 20);
            post.setComments(0);
            post.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(12)));
            post.setCategory(categories[i]);
            post.setActive(true);
            posts.add(postRepository.save(post));
        }

        for (Post post : posts) {
            int commentCount = random.nextInt(5) + 2;
            for (int i = 0; i < commentCount; i++) {
                User commentUser = users.get(random.nextInt(users.size()));
                Comment comment = new Comment();
                comment.setPostId(post.getId());
                comment.setUserId(commentUser.getId());
                comment.setUsername(commentUser.getName());
                comment.setContent(generateRandomComment());
                comment.setCreatedAt(post.getCreatedAt().plusHours(random.nextInt(24)));
                comment.setLikes(random.nextInt(10));
                commentRepository.save(comment);
            }
            int savedComments = (int) commentRepository.countByPostId(post.getId());
            post.setComments(savedComments);
            postRepository.save(post);
        }

        System.out.println("创建了 " + posts.size() + " 个帖子");
        seedReports(users, posts);
    }

    private void seedReports(List<User> users, List<Post> posts) {
        if (posts.isEmpty() || users.size() < 2) {
            return;
        }
        Post p0 = posts.get(0);
        Post p1 = posts.get(1);
        User uRep = users.get(1);
        User uRep2 = users.get(2 % users.size());

        Report r1 = new Report();
        r1.setPostId(p0.getId());
        r1.setReporterId(uRep.getId());
        r1.setReportedUserId(p0.getUserId());
        r1.setType("SPAM");
        r1.setReason("垃圾广告");
        r1.setStatus("PENDING");
        r1.setCreatedAt(LocalDateTime.now().minusHours(1));
        reportRepository.save(r1);

        Report r2 = new Report();
        r2.setPostId(p1.getId());
        r2.setReporterId(uRep2.getId());
        r2.setReportedUserId(p1.getUserId());
        r2.setType("HARASSMENT");
        r2.setReason("人身攻击");
        r2.setStatus("PROCESSED");
        r2.setCreatedAt(LocalDateTime.now().minusHours(3));
        r2.setProcessedAt(LocalDateTime.now().minusHours(2));
        reportRepository.save(r2);

        System.out.println("创建了示例举报记录");
    }
    
    private String generateRandomComment() {
        String[] comments = {
            "说得太好了！",
            "学习了，感谢分享！",
            "太有帮助了！",
            "我也这么觉得！",
            "加油！一起努力！",
            "666！",
            "学到了！",
            "太强了！"
        };
        return comments[random.nextInt(comments.length)];
    }
    
    private void createTeams(List<User> users) {
        String[] teamTitles = {
            "周末晨跑小队",
            "瑜伽爱好者群",
            "力量训练打卡",
            "游泳健身队"
        };
        
        String[] teamDescriptions = {
            "每周六日早上7点，公园集合，一起晨跑！",
            "每周二、四晚8点，一起练瑜伽，放松身心！",
            "一起打卡力量训练，互相鼓励，共同进步！",
            "每周三下午，泳池集合，一起游泳健身！"
        };
        
        String[] locations = {"中央公园", "健身中心A区", "健身房力量区", "游泳馆"};
        String[] meetTimes = {"周六周日7:00", "周二周四20:00", "每天18:00", "每周三15:00"};
        String[] categories = {"跑步", "瑜伽", "力量", "游泳"};
        
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < teamTitles.length; i++) {
            User creator = users.get(i);
            Team team = new Team();
            team.setTitle(teamTitles[i]);
            team.setDescription(teamDescriptions[i]);
            team.setCategory(categories[i]);
            team.setCreatorId(creator.getId());
            team.setCreatorName(creator.getName());
            team.setMaxMembers(10);
            team.setCurrentMembers(1);
            team.setLocation(locations[i]);
            team.setMeetTime(meetTimes[i]);
            team.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(10)));
            team.setActive(true);
            teams.add(teamRepository.save(team));
            
            // 创建创建者为成员
            TeamMember member = new TeamMember();
            member.setTeamId(team.getId());
            member.setUserId(creator.getId());
            member.setUsername(creator.getName());
            member.setJoinedAt(team.getCreatedAt());
            teamMemberRepository.save(member);
            
            // 随机添加一些成员
            int memberCount = random.nextInt(5) + 1;
            for (int j = 0; j < memberCount; j++) {
                User joinUser = users.get((i + j + 1) % users.size());
                if (!joinUser.getId().equals(creator.getId())) {
                    TeamMember newMember = new TeamMember();
                    newMember.setTeamId(team.getId());
                    newMember.setUserId(joinUser.getId());
                    newMember.setUsername(joinUser.getName());
                    newMember.setJoinedAt(team.getCreatedAt().plusDays(j + 1));
                    teamMemberRepository.save(newMember);
                    team.setCurrentMembers(team.getCurrentMembers() + 1);
                    teamRepository.save(team);
                }
            }
        }
        
        System.out.println("创建了 " + teams.size() + " 个组队");
    }
    
    private void createAdvertisements() {
        List<Advertisement> ads = new ArrayList<>();
        
        Advertisement ad1 = new Advertisement();
        ad1.setTitle("新会员优惠");
        ad1.setDescription("新会员首月半价，快来加入吧！");
        ad1.setImageUrl("");
        ad1.setLinkUrl("");
        ad1.setPosition(1);
        ad1.setIsActive(true);
        ad1.setCreatedAt(LocalDateTime.now());
        ad1.setExpiresAt(LocalDateTime.now().plusMonths(1));
        ads.add(ad1);
        
        Advertisement ad2 = new Advertisement();
        ad2.setTitle("私教课特惠");
        ad2.setDescription("私教课买10送3，限时优惠！");
        ad2.setImageUrl("");
        ad2.setLinkUrl("");
        ad2.setPosition(2);
        ad2.setIsActive(true);
        ad2.setCreatedAt(LocalDateTime.now());
        ad2.setExpiresAt(LocalDateTime.now().plusMonths(1));
        ads.add(ad2);
        
        advertisementRepository.saveAll(ads);
        System.out.println("创建了 " + ads.size() + " 个广告");
    }
    
    private void createPersonalTrainers(List<User> users) {
        String[] names = {"罗冬冬Dora", "何秋圆Nina", "韦斌Jason", "蒲雪萍Angel", "赵千雯Carwen", "林炜坤Dino"};
        String[] specialties = {
            "少儿体适能,普拉提,产后恢复",
            "瑜伽,拉伸,冥想",
            "力量训练,增肌,功能性训练",
            "少儿体适能,普拉提,产后恢复,美臀,翘臀,康复,体态矫正,减脂",
            "瑜伽,普拉提,康复",
            "少儿体适能,散打搏击,爆发力,体态矫正"
        };
        String[] bios = {
            "专注少儿体适能训练5年，帮助上千儿童提升体能",
            "资深瑜伽教练，擅长各种瑜伽流派",
            "力量训练专家，帮助学员快速增肌",
            "全能私教，涵盖多种训练领域",
            "康复训练专家，帮助学员恢复身体机能",
            "搏击教练，培养出多名优秀学员"
        };
        Double[] ratings = {4.9, 4.8, 4.9, 4.7, 4.8, 4.9};
        Integer[] experienceYears = {5, 4, 6, 3, 4, 7};
        Double[] hourlyRates = {200.0, 180.0, 220.0, 190.0, 180.0, 250.0};
        
        List<PersonalTrainer> trainers = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            PersonalTrainer trainer = new PersonalTrainer();
            trainer.setUserId(users.get(i).getId());
            trainer.setName(names[i]);
            trainer.setSpecialty(specialties[i]);
            trainer.setBio(bios[i]);
            trainer.setCertifications("国家级健身教练认证");
            trainer.setRating(ratings[i]);
            trainer.setExperienceYears(experienceYears[i]);
            trainer.setHourlyRate(hourlyRates[i]);
            trainers.add(personalTrainerRepository.save(trainer));
        }
        
        System.out.println("创建了 " + trainers.size() + " 个私教教练");
    }
    
    private void createBadges(List<User> users) {
        // 定义徽章模板
        Object[][] badgeTemplates = {
            {"健身新手", "完成第一次课程预约", "🏆", 1},
            {"坚持达人", "连续打卡7天", "🔥", 7},
            {"健身狂人", "连续打卡30天", "💪", 30},
            {"课程达人", "完成10次课程", "📚", 10},
            {"社交达人", "发布10篇帖子", "📝", 10},
            {"团队领袖", "创建一个组队", "👑", 1},
            {"VIP会员", "成为VIP会员", "⭐", 0},
            {"积分达人", "累计获得1000积分", "💰", 1000}
        };
        
        List<Badge> badges = new ArrayList<>();
        
        for (User user : users) {
            for (int i = 0; i < badgeTemplates.length; i++) {
                Badge badge = new Badge();
                badge.setUserId(user.getId());
                badge.setName((String) badgeTemplates[i][0]);
                badge.setDescription((String) badgeTemplates[i][1]);
                badge.setIcon((String) badgeTemplates[i][2]);
                badge.setRequiredDays((Integer) badgeTemplates[i][3]);
                
                // 根据用户和徽章类型决定是否解锁
                boolean unlocked = false;
                LocalDateTime unlockedAt = null;
                
                if (i == 0) {
                    // 健身新手徽章给所有用户解锁
                    unlocked = true;
                    unlockedAt = LocalDateTime.now().minusDays(random.nextInt(30));
                } else if (i == 6 && user.getRole() != null && user.getRole().equals("VIP")) {
                    // VIP会员徽章给VIP用户解锁
                    unlocked = true;
                    unlockedAt = LocalDateTime.now().minusDays(random.nextInt(10));
                }
                
                badge.setUnlocked(unlocked);
                badge.setUnlockedAt(unlockedAt);
                
                badges.add(badge);
            }
        }
        
        badgeRepository.saveAll(badges);
        System.out.println("创建了 " + badges.size() + " 个徽章记录");
    }

    private void ensureFacilities() {
        Object[][] rows = {
                {"训练区A", "力量训练主区", "杠铃、哑铃、龙门架", true},
                {"训练区B", "有氧与综合训练", "跑步机、椭圆机、划船机", true},
                {"瑜伽室", "团课/私教瑜伽", "瑜伽垫、瑜伽球", true},
                {"操课室", "团体课程教室", "音响、踏板、镜子", true},
                {"动感单车室", "单车团课", "动感单车、音响", true},
                {"C区力量区", "大重量力量训练区", "深蹲架、卧推架、杠铃片、哑铃", true},
                {"A区教室", "小班团课与理论讲解教室", "投影仪、瑜伽垫、轻器械", true},
                {"B区动感单车房", "动感单车专项教室", "动感单车、音响、风扇", true},
                {"D区搏击室", "搏击与拳击训练空间", "拳袋、拳套、护具、脚靶", true},
                {"E区舞蹈室", "舞蹈、拉伸与形体训练空间", "镜墙、把杆、音响", true},
                {"游泳池", "室内恒温泳池", "泳道线、浮板、救生设备", true},
                {"F区有氧区", "有氧操与心肺训练区", "跑步机、椭圆机、划船机", true}
        };
        int created = 0;
        List<Facility> existing = facilityRepository.findAll();
        for (Object[] row : rows) {
            String name = (String) row[0];
            boolean exists = existing.stream().anyMatch(f -> name.equals(f.getName()));
            if (exists) {
                continue;
            }
            Facility f = new Facility();
            f.setName(name);
            f.setDescription((String) row[1]);
            f.setEquipment((String) row[2]);
            f.setAvailable((Boolean) row[3]);
            f.setCreatedAt(LocalDateTime.now());
            f.setUpdatedAt(LocalDateTime.now());
            facilityRepository.save(f);
            created++;
        }
        System.out.println("已检查场地基础数据，新增 " + created + " 个缺失场地");
    }
    
    private void createCoaches() {
        if (coachRepository.count() > 0) {
            System.out.println("教练账号已存在，跳过初始化");
            // 更新密码为加密版本
            List<Coach> coaches = coachRepository.findAll();
            for (Coach coach : coaches) {
                if (!coach.getPassword().startsWith("$2a$")) {
                    coach.setPassword(passwordEncoder.encode("123456"));
                    coachRepository.save(coach);
                }
            }
            System.out.println("教练密码已更新为加密版本");
            return;
        }
        
        String[] names = {"张教练", "李教练", "王教练", "赵教练"};
        String[] specialties = {
            "力量训练、增肌塑形、功能性训练",
            "瑜伽放松、普拉提、柔韧性训练",
            "动感单车、有氧训练、减脂",
            "搏击训练、体能训练、爆发力"
        };
        
        for (int i = 0; i < names.length; i++) {
            Coach coach = new Coach();
            coach.setUsername("coach_" + (i + 1));
            coach.setPassword(passwordEncoder.encode("123456"));
            coach.setName(names[i]);
            coach.setEmail("coach" + (i + 1) + "@fitlife.com");
            coach.setPhone("1380000000" + (i + 1));
            coach.setExperienceYears(5 + i);
            coach.setCertifications("国家级健身教练认证、高级私教证书");
            coach.setSpecialty(specialties[i]);
            coach.setHourlyRate(300.0 + i * 50);
            coach.setRating(4.8 + i * 0.1);
            coach.setTotalClasses(500 + i * 100);
            coach.setTotalStudents(100 + i * 50);
            coach.setQrCode("COACH-" + String.format("%06d", i + 1));
            
            coachRepository.save(coach);
        }
        System.out.println("创建了 " + names.length + " 个教练账号（用户名: coach_1~4，密码: 123456）");
    }

    /**
     * 为教练端「教练信箱」写入与 init 用户/课程一致的投诉与评价数据（以 coach_1 为主）。
     */
    private void createCoachMailboxData(List<User> users, List<Classes> classes) {
        if (users == null || users.isEmpty() || classes == null || classes.isEmpty()) {
            return;
        }
        Coach coach1 = coachRepository.findByUsername("coach_1").orElse(null);
        Coach coach2 = coachRepository.findByUsername("coach_2").orElse(null);
        if (coach1 == null) {
            System.out.println("未找到 coach_1，跳过教练信箱演示数据");
            return;
        }

        List<Classes> classesForCoach1 = new ArrayList<>();
        for (Classes c : classes) {
            if (c.getCoach() != null && coach1.getId().equals(c.getCoach().getId())) {
                classesForCoach1.add(c);
            }
        }
        if (classesForCoach1.isEmpty()) {
            classesForCoach1.addAll(classes.subList(0, Math.min(3, classes.size())));
        }

        User u0 = users.get(0);
        User u1 = users.size() > 1 ? users.get(1) : u0;
        User u2 = users.size() > 2 ? users.get(2) : u0;
        User u3 = users.size() > 3 ? users.get(3) : u0;
        User u4 = users.size() > 4 ? users.get(4) : u0;

        LocalDateTime now = LocalDateTime.now();

        Complaint cpl1 = new Complaint();
        cpl1.setUser(u0);
        cpl1.setCoach(coach1);
        cpl1.setComplaintType("course");
        cpl1.setTitle("周三晚力量课排队过久");
        cpl1.setTitleEn("Long wait to enter Wednesday evening strength class");
        cpl1.setContent("我是会员 fitness_pro，本周三 18:30 力量区团课提前 20 分钟到场仍未能签到入场，现场人数与系统显示容量不一致，请核实是否超售并优化入场流程。");
        cpl1.setContentEn("Member fitness_pro: I arrived 20 minutes early for the 18:30 strength class on Wednesday but still could not check in. Headcount on site did not match capacity in the app. Please check for overselling and streamline entry.");
        cpl1.setStatus("pending");
        cpl1.setCreatedAt(now.minusDays(1).withHour(21).withMinute(6));
        complaintRepository.save(cpl1);

        Complaint cpl2 = new Complaint();
        cpl2.setUser(u1);
        cpl2.setCoach(coach1);
        cpl2.setComplaintType("facility");
        cpl2.setTitle("希望课前说明器械归位要求");
        cpl2.setTitleEn("Please remind members to re-rack equipment before class");
        cpl2.setContent("课程质量不错，但下课后哑铃片散落较多，建议张教练在课前用 1 分钟统一说明器械归位与片重选择，避免新手误拿。");
        cpl2.setContentEn("Great class, but plates are often left scattered. Please spend one minute before class on re-racking rules and plate selection for beginners.");
        cpl2.setStatus("processed");
        cpl2.setResponse("感谢反馈。已在班级群推送课前须知，并在课后预留 3 分钟带学员一起收片。");
        cpl2.setResponseEn("Thanks for the feedback. We posted pre-class notes in the group chat and reserved 3 minutes after class to re-rack together.");
        cpl2.setProcessedAt(now.minusHours(30));
        cpl2.setCreatedAt(now.minusDays(3).withHour(16).withMinute(40));
        complaintRepository.save(cpl2);

        Complaint cpl3 = new Complaint();
        cpl3.setUser(u2);
        cpl3.setCoach(coach1);
        cpl3.setComplaintType("facility");
        cpl3.setTitle("音乐音量偏大");
        cpl3.setTitleEn("Music volume is too loud");
        cpl3.setContent("动感单车教室音响低音较重，长时间训练耳朵不适，能否将峰值音量下调约 10% 或提供耳塞提示？");
        cpl3.setContentEn("The spin room bass is heavy and uncomfortable during long sessions. Could peak volume be reduced by ~10% or could you remind members to bring earplugs?");
        cpl3.setStatus("pending");
        cpl3.setCreatedAt(now.minusHours(8).withMinute(12));
        complaintRepository.save(cpl3);

        Complaint cpl4 = new Complaint();
        cpl4.setUser(u3);
        cpl4.setCoach(coach1);
        cpl4.setComplaintType("course");
        cpl4.setTitle("课程结束时间与课表不符");
        cpl4.setTitleEn("Class end time did not match schedule");
        cpl4.setContent("系统课表显示 19:00 结束，实际拖堂到 19:18，导致我后续私教衔接迟到，请尽量按表控时或提前通知拖堂。");
        cpl4.setContentEn("The schedule said 19:00 end but we finished at 19:18, which made me late for my next PT session. Please keep to time or announce overruns in advance.");
        cpl4.setStatus("pending");
        cpl4.setCreatedAt(now.minusDays(2).withHour(19).withMinute(25));
        complaintRepository.save(cpl4);

        if (coach2 != null) {
            Complaint cplOther = new Complaint();
            cplOther.setUser(u4);
            cplOther.setCoach(coach2);
            cplOther.setComplaintType("course");
            cplOther.setTitle("希望增加肩颈放松小节");
            cplOther.setTitleEn("Please add a short neck and shoulder stretch segment");
            cplOther.setContent("瑜伽课节奏很好，若能增加 5 分钟肩颈拉伸会更适合久坐办公族。");
            cplOther.setContentEn("Love the yoga pacing. A 5-minute neck and shoulder stretch would help desk workers.");
            cplOther.setStatus("pending");
            cplOther.setCreatedAt(now.minusDays(5).withHour(10).withMinute(0));
            complaintRepository.save(cplOther);
        }

        Classes clsA = classesForCoach1.get(0);
        Classes clsB = classesForCoach1.size() > 1 ? classesForCoach1.get(1) : clsA;
        Classes clsC = classesForCoach1.size() > 2 ? classesForCoach1.get(2) : clsA;

        Review rv1 = new Review();
        rv1.setUser(u0);
        rv1.setCoach(coach1);
        rv1.setClasses(clsA);
        rv1.setRating(5);
        rv1.setContent("张教练讲解动作要领非常清楚，会逐个纠正深蹲膝盖轨迹，一节课下来膝盖没有不适。已推荐给同事。");
        rv1.setContentEn("Coach Zhang explains cues very clearly and corrects squat knee tracking one by one. No knee discomfort after class. Already recommended to colleagues.");
        rv1.setCreatedAt(now.minusDays(1).withHour(20).withMinute(15));
        rv1.setResponded(false);
        reviewRepository.save(rv1);

        Review rv2 = new Review();
        rv2.setUser(u1);
        rv2.setCoach(coach1);
        rv2.setClasses(clsB);
        rv2.setRating(4);
        rv2.setContent("整体节奏紧凑，组间休息略短，希望大重量组能多给 15 秒调整呼吸。");
        rv2.setContentEn("Good pace overall; rest periods feel a bit short on heavy sets. 15 more seconds to breathe would help.");
        rv2.setCreatedAt(now.minusDays(2).withHour(12).withMinute(50));
        rv2.setResponded(true);
        rv2.setResponse("感谢建议，下周起会在主项大重量组把间歇统一调到 90–120 秒，并在白板标注。");
        rv2.setResponseEn("Thanks for the suggestion. Next week we will standardize main-lift rest to 90–120 seconds and note it on the whiteboard.");
        rv2.setRespondedAt(now.minusDays(2).withHour(14));
        reviewRepository.save(rv2);

        Review rv3 = new Review();
        rv3.setUser(u2);
        rv3.setCoach(coach1);
        rv3.setClasses(clsC);
        rv3.setRating(5);
        rv3.setContent("第一次上团课，教练会照顾到后排学员，镜面示范很清楚。");
        rv3.setContentEn("First group class — the coach checks on people in the back row and mirror demos are very clear.");
        rv3.setCreatedAt(now.minusDays(4).withHour(9).withMinute(5));
        rv3.setResponded(false);
        reviewRepository.save(rv3);

        Review rv4 = new Review();
        rv4.setUser(u3);
        rv4.setCoach(coach1);
        rv4.setClasses(clsA);
        rv4.setRating(5);
        rv4.setContent("力量区设备齐全，课程编排有进退阶，适合不同水平。");
        rv4.setContentEn("Well-equipped strength zone; programming has progressions and regressions for all levels.");
        rv4.setCreatedAt(now.minusDays(6).withHour(18).withMinute(40));
        rv4.setResponded(false);
        reviewRepository.save(rv4);

        Review rv5 = new Review();
        rv5.setUser(u4);
        rv5.setCoach(coach1);
        rv5.setClasses(clsB);
        rv5.setRating(4);
        rv5.setContent("氛围很好，若能在课前发一份当日训练重点到 App 会更方便预习。");
        rv5.setContentEn("Great vibe. Sending the session focus to the app before class would make prep easier.");
        rv5.setCreatedAt(now.minusDays(7).withHour(7).withMinute(30));
        rv5.setResponded(false);
        reviewRepository.save(rv5);

        System.out.println("已写入教练信箱演示数据（投诉 " + complaintRepository.count() + " 条，评价 " + reviewRepository.count() + " 条，coach_1 为主）");
    }
}
