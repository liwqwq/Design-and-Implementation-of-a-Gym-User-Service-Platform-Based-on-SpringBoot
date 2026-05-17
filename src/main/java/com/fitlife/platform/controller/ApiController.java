package com.fitlife.platform.controller;

import com.fitlife.platform.store.VisualStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    @Autowired
    private VisualStore store;

    private String username(HttpServletRequest request) {
        return store.readToken(request.getHeader("Authorization"))[1];
    }

    private String role(HttpServletRequest request) {
        return store.readToken(request.getHeader("Authorization"))[0];
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(store.ok(data));
    }

    private ResponseEntity<Map<String, Object>> fail(String message) {
        return ResponseEntity.ok(store.fail(message));
    }

    private String slot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        return start.toLocalTime().toString().substring(0, 5) + "-" + end.toLocalTime().toString().substring(0, 5);
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) return null;
        try { return LocalDateTime.parse(String.valueOf(value)); } catch (Exception ignored) { return null; }
    }

    private boolean confirmedStatus(Object status) {
        return "CONFIRMED".equalsIgnoreCase(store.str(status, "CONFIRMED"));
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object raw) {
        List<String> out = new ArrayList<String>();
        if (raw instanceof List) {
            for (Object item : (List<Object>) raw) {
                String v = store.str(item, "").trim();
                if (v.length() > 0 && !out.contains(v)) out.add(v);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<String> mutableStringList(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        if (raw instanceof List) {
            List<String> normalized = stringList(raw);
            row.put(key, normalized);
            return normalized;
        }
        List<String> created = new ArrayList<String>();
        row.put(key, created);
        return created;
    }

    private Map<String, Object> teamView(Map<String, Object> team, String currentUser) {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(team);
        List<String> members = stringList(team.get("memberUsernames"));
        String creator = store.str(team.get("creator"), "");
        if (creator.length() > 0 && !members.contains(creator)) members.add(creator);
        boolean createdByCurrentUser = currentUser.equals(creator);
        boolean joined = createdByCurrentUser || members.contains(currentUser);
        copy.put("joined", joined);
        copy.put("createdByCurrentUser", createdByCurrentUser);
        copy.put("memberUsernames", members);
        copy.put("currentMembers", Math.max(store.asInt(copy.get("currentMembers"), store.asInt(copy.get("members"), members.size())), members.size()));
        copy.put("members", copy.get("currentMembers"));
        return copy;
    }

    private Map<String, Object> postView(Map<String, Object> post, String currentUser) {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(post);
        List<String> likedBy = stringList(post.get("likedBy"));
        copy.put("liked", likedBy.contains(currentUser));
        copy.put("likedBy", likedBy);
        return copy;
    }

    private boolean isBookedBy(String username, Long classId) {
        synchronized (store.bookings) {
            for (Map<String, Object> b : store.bookings) {
                if (username.equals(b.get("username")) && Objects.equals(store.asLong(b.get("classId")), classId) && confirmedStatus(b.get("status"))) return true;
            }
        }
        return false;
    }

    private Map<String, Object> classView(Map<String, Object> c, String username) {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(c);
        LocalDateTime start = toDateTime(c.get("startTime"));
        LocalDateTime end = toDateTime(c.get("endTime"));
        if (start != null) {
            copy.put("classDate", start.toLocalDate().toString());
            copy.put("startSlot", start.toLocalTime().toString().substring(0, 5));
        }
        if (end != null) copy.put("endSlot", end.toLocalTime().toString().substring(0, 5));
        copy.put("time", copy.containsKey("time") ? copy.get("time") : slot(start, end));
        copy.put("trainer", copy.get("coachName"));
        copy.put("instructor", copy.get("coachName"));
        copy.put("participants", store.asInt(copy.get("bookedCount"), 0));
        int cap = store.asInt(copy.get("capacity"), 20);
        int booked = store.asInt(copy.get("bookedCount"), 0);
        copy.put("spotsLeft", Math.max(0, cap - booked));
        copy.put("isFull", booked >= cap);
        copy.put("isBooked", isBookedBy(username, store.asLong(c.get("id"))));
        Object rawStatus = copy.get("status");
        if (rawStatus == null || "ACTIVE".equals(rawStatus)) copy.put("status", "upcoming");
        return copy;
    }

    private List<Map<String, Object>> classViews(List<Map<String, Object>> source, String username, String type, String date) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (source) {
            for (Map<String, Object> c : source) {
                if (type != null && !type.equals(c.get("type"))) continue;
                Map<String, Object> v = classView(c, username);
                if (date != null && date.trim().length() > 0 && !date.equals(v.get("classDate"))) continue;
                out.add(v);
            }
        }
        return out;
    }

    private ResponseEntity<Map<String, Object>> rankingResponse(String period) {
        List<Map<String, Object>> rows = store.rankings(period);
        Map<String, Object> out = store.ok(rows);
        int myScore = rows.isEmpty() ? 0 : store.asInt(rows.get(0).get("score"), 0) - 2;
        out.put("myRanking", store.m("rank", 4, "score", Math.max(0, myScore), "nextScore", rows.size() > 2 ? rows.get(2).get("score") : myScore + 1, "prevScore", 0, "neededClassesToNext", 3, "isLeading", false));
        return ResponseEntity.ok(out);
    }

    private Map<String, Object> facilityById(Long id) {
        synchronized (store.facilities) {
            for (Map<String, Object> f : store.facilities) if (Objects.equals(store.asLong(f.get("id")), id)) return f;
        }
        return null;
    }

    private String[] splitSlot(String timeSlot) {
        if (timeSlot == null || !timeSlot.contains("-")) return new String[]{"09:00", "10:00"};
        String[] arr = timeSlot.split("-", 2);
        return new String[]{arr[0].trim(), arr[1].trim()};
    }

    private Map<String, Object> adminUserRow(Map<String, Object> src) {
        Map<String, Object> out = new LinkedHashMap<String, Object>(src);
        String username = store.str(out.get("username"), "");
        Map<String, Object> membership = store.memberships.get(username);
        String membershipType = membership == null ? store.str(out.get("membershipType"), "") : store.str(membership.get("membershipType"), store.str(out.get("membershipType"), ""));
        if (membershipType.length() == 0) membershipType = "标准会员";
        boolean active = !Boolean.FALSE.equals(out.get("active"));
        String typeLower = membershipType.toLowerCase(Locale.ROOT);
        out.put("membershipType", membershipType);
        out.put("levelKey", typeLower.contains("vip") ? "vip" : "normal");
        out.put("statusKey", active ? "active" : "inactive");
        return out;
    }

    private Map<String, Object> adminUsersPayload() {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        int totalMembers = 0;
        int activeMembers = 0;
        synchronized (store.users) {
            for (Map<String, Object> user : store.users) {
                Map<String, Object> row = adminUserRow(user);
                rows.add(row);
                if ("USER".equals(row.get("role"))) {
                    totalMembers++;
                    if ("active".equals(row.get("statusKey"))) activeMembers++;
                }
            }
        }
        Map<String, Object> out = store.ok(rows);
        out.put("stats", store.m(
                "totalMembers", totalMembers,
                "activeMembers", activeMembers,
                "newMembers", Math.min(totalMembers, 3)
        ));
        return out;
    }



    private boolean isVipType(String value) {
        String v = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return v.contains("vip") || v.contains("年卡") || v.contains("vip会员");
    }

    private void syncUserMembership(Map<String, Object> user, Map<String, Object> body) {
        if (user == null) return;
        String username = store.str(user.get("username"), "");
        if (username.length() == 0) return;
        String currentType = "";
        Map<String, Object> old = store.memberships.get(username);
        if (old != null) currentType = store.str(old.get("membershipType"), "");
        String requested = store.str(body.get("membershipType"), store.str(user.get("membershipType"), currentType));
        if (requested.length() == 0) requested = "标准会员";
        boolean vip = isVipType(requested);
        boolean active = !Boolean.FALSE.equals(user.get("active"));
        LocalDate today = LocalDate.now();
        LocalDate end;
        if (old != null && old.get("endDate") != null) {
            try { end = LocalDate.parse(String.valueOf(old.get("endDate"))); } catch (Exception ex) { end = today; }
        } else {
            end = today;
        }
        if (vip && end.isBefore(today.plusYears(1))) end = today.plusYears(1);
        if (!vip) end = today.plusMonths(3);
        String normalized = vip ? "VIP年卡" : "标准会员";
        user.put("membershipType", normalized);
        store.memberships.put(username, store.m(
                "membershipType", normalized,
                "startDate", today.toString(),
                "endDate", end.toString(),
                "status", active ? "ACTIVE" : "INACTIVE",
                "remainingSessions", vip ? 60 : 10,
                "daysRemaining", java.time.temporal.ChronoUnit.DAYS.between(today, end)
        ));
    }

    private ResponseEntity<Map<String, Object>> pointProductOk(Object data) { return ok(data); }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return store.m("status", "UP", "service", "基于SpringBoot的健身房用户服务平台", "storage", store.isMysqlAvailable() ? "MySQL" : "MEMORY_FALLBACK", "time", LocalDateTime.now().toString());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        store.ensureOperationalData();
        String username = store.str(body.get("username"), "");
        String password = store.str(body.get("password"), "");
        Map<String, Object> user = store.userByUsername(username);
        if (user == null || !"123456".equals(password)) return fail("用户名或密码错误");
        String token = store.token("user", username);
        Map<String, Object> out = store.ok(user);
        out.put("token", token);
        out.put("username", username);
        out.put("userType", "user");
        out.put("user", user);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String username = store.str(body.get("username"), "").trim();
        String password = store.str(body.get("password"), "");
        if (username.length() < 3 || password.length() < 3) return fail("请填写用户名和密码");
        if (store.userByUsername(username) != null) return fail("该用户名已存在");
        Map<String, Object> user = store.m("id", store.nextId(), "username", username, "name", store.str(body.get("name"), username), "email", store.str(body.get("email"), username + "@example.com"), "phone", store.str(body.get("phone"), ""), "gender", "male", "birthday", "1998-01-01", "address", "", "medicalNotes", "", "role", "USER", "active", true, "avatar", "👤");
        store.users.add(user);
        store.points.put(username, 500);
        store.checkinDates.put(username, Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>()));
        store.memberships.put(username, store.m("membershipType", "标准会员", "startDate", LocalDate.now().toString(), "endDate", LocalDate.now().plusMonths(3).toString(), "status", "ACTIVE", "remainingSessions", 10, "daysRemaining", 90));
        String token = store.token("user", username);
        Map<String, Object> out = store.ok(user);
        out.put("token", token);
        out.put("username", username);
        out.put("userType", "user");
        out.put("user", user);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Map<String, Object>> forgot(@RequestBody Map<String, Object> body) {
        return ok(store.m("resetToken", "demo-reset-token", "message", "演示环境已生成重置链接"));
    }

    @GetMapping("/auth/reset-password/validate")
    public ResponseEntity<Map<String, Object>> validateReset(@RequestParam(required = false) String token) {
        return ok(store.m("valid", token == null || token.length() > 0));
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, Object>> reset(@RequestBody Map<String, Object> body) {
        return ok(store.m("updated", true));
    }

    @PostMapping("/admin/auth/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, Object> body) {
        store.ensureOperationalData();
        String username = store.str(body.get("username"), "");
        String password = store.str(body.get("password"), "");
        if (!"admin".equals(username) || !"admin123".equals(password)) return fail("管理员账号或密码错误");
        String token = store.token("admin", username);
        Map<String, Object> admin = store.m("username", "admin", "name", "系统管理员", "email", "admin@fitlife.com");
        Map<String, Object> out = store.ok(admin);
        out.put("token", token);
        out.put("username", username);
        out.put("userType", "admin");
        out.put("admin", admin);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/coach/login")
    public ResponseEntity<Map<String, Object>> coachLogin(@RequestBody Map<String, Object> body) {
        store.ensureOperationalData();
        String username = store.str(body.get("username"), "");
        String password = store.str(body.get("password"), "");
        Map<String, Object> coach = store.coachByUsername(username);
        if (coach == null || !"123456".equals(password)) return fail("教练账号或密码错误");
        Map<String, Object> data = store.m("token", store.token("coach", username), "username", username, "userType", "coach", "id", coach.get("id"), "coachId", coach.get("id"), "coach", coach);
        return ok(data);
    }

    @GetMapping("/classes")
    public ResponseEntity<Map<String, Object>> classes(HttpServletRequest request, @RequestParam(required = false) String date) {
        store.ensureOperationalData();
        List<Map<String, Object>> out = classViews(store.classes, username(request), "GROUP", date);
        if (out.isEmpty() && date != null && date.trim().length() > 0) out = classViews(store.classes, username(request), "GROUP", null);
        return ok(out);
    }

    @GetMapping("/classes/{id}")
    public ResponseEntity<Map<String, Object>> classDetail(HttpServletRequest request, @PathVariable Long id) {
        Map<String, Object> c = store.classById(id);
        return c == null ? fail("课程不存在") : ok(classView(c, username(request)));
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<Map<String, Object>> myBookings(HttpServletRequest request) {
        store.ensureOperationalData();
        return ok(store.myBookings(username(request)));
    }

    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> allBookings() {
        store.ensureOperationalData();
        return ok(store.bookings);
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> book(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        store.ensureOperationalData();
        String username = username(request);
        Long classId = store.asLong(body.get("classId"));
        Map<String, Object> c = classId == null ? null : store.classById(classId);
        if (c == null) return fail("课程不存在");
        synchronized (store.bookings) {
            for (Map<String, Object> b : store.bookings) {
                if (username.equals(b.get("username")) && Objects.equals(store.asLong(b.get("classId")), classId)
                        && "CONFIRMED".equalsIgnoreCase(store.str(b.get("status"), "CONFIRMED"))) {
                    return fail("你已经预约过该课程");
                }
            }
            int capacity = store.asInt(c.get("capacity"), 20);
            int bookedCount = store.asInt(c.get("bookedCount"), 0);
            if (capacity > 0 && bookedCount >= capacity) return fail("课程已满");
            Map<String, Object> booking = store.m("id", store.nextId(), "username", username, "classId", classId, "status", "CONFIRMED", "bookingTime", LocalDateTime.now().toString());
            store.bookings.add(booking);
            c.put("bookedCount", bookedCount + 1);
            store.saveToMysqlQuietly();
            return ok(booking);
        }
    }

    @PutMapping("/bookings/class/{classId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelByClass(HttpServletRequest request, @PathVariable Long classId) {
        store.ensureOperationalData();
        String username = username(request);
        synchronized (store.bookings) {
            for (Map<String, Object> b : store.bookings) {
                if (username.equals(b.get("username")) && Objects.equals(store.asLong(b.get("classId")), classId) && "CONFIRMED".equalsIgnoreCase(store.str(b.get("status"), "CONFIRMED"))) {
                    b.put("status", "CANCELLED");
                    Map<String, Object> c = store.classById(classId);
                    if (c != null) c.put("bookedCount", Math.max(0, store.asInt(c.get("bookedCount"), 0) - 1));
                    store.saveToMysqlQuietly();
                    return ok(b);
                }
            }
        }
        return fail("未找到有效预约");
    }

    @GetMapping("/trainers")
    public ResponseEntity<Map<String, Object>> trainers() { store.ensureOperationalData(); return ok(store.coaches); }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<Map<String, Object>> trainer(@PathVariable Long id) {
        Map<String, Object> coach = store.coachById(id);
        return coach == null ? fail("教练不存在") : ok(coach);
    }

    @GetMapping("/trainers/{id}/courses")
    public ResponseEntity<Map<String, Object>> trainerCourses(HttpServletRequest request, @PathVariable Long id) {
        store.ensureOperationalData();
        Map<String, Object> coach = store.coachById(id);
        String coachName = coach == null ? "" : store.str(coach.get("name"), "");
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> c : classViews(store.classes, username(request), "PRIVATE", null)) {
            boolean byId = Objects.equals(store.asLong(c.get("coachId")), id);
            boolean byName = coachName.length() > 0 && (coachName.equals(c.get("coachName")) || String.valueOf(c.get("location")).contains(coachName));
            if (byId || byName) out.add(c);
        }
        if (out.isEmpty()) {
            for (Map<String, Object> c : classViews(store.classes, username(request), "PRIVATE", null)) out.add(c);
        }
        return ok(out);
    }

    @GetMapping("/checkin/status")
    public ResponseEntity<Map<String, Object>> checkinStatus(HttpServletRequest request) {
        String username = username(request);
        Set<String> dates = store.checkinDates.get(username);
        if (dates == null) dates = Collections.emptySet();
        boolean today = dates.contains(LocalDate.now().toString());
        int streak = 0;
        LocalDate d = LocalDate.now();
        while (dates.contains(d.toString())) { streak++; d = d.minusDays(1); }
        int balance = store.points.containsKey(username) ? store.points.get(username) : 0;
        return ok(store.m("hasCheckedInToday", today, "checkedIn", today, "consecutiveDays", streak, "streakDays", streak, "totalCheckins", dates.size(), "todayPoints", today ? 10 : 0, "availablePoints", balance, "points", balance));
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkin(HttpServletRequest request) {
        String username = username(request);
        Set<String> dates = store.checkinDates.get(username);
        if (dates == null) {
            dates = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());
            store.checkinDates.put(username, dates);
        }
        String today = LocalDate.now().toString();
        if (dates.contains(today)) return fail("今日已打卡");
        dates.add(today);
        int next = store.points.containsKey(username) ? store.points.get(username) + 10 : 10;
        store.points.put(username, next);
        return ok(store.m("pointsEarned", 10, "availablePoints", next, "date", today));
    }

    @PostMapping("/checkin/scan")
    public ResponseEntity<Map<String, Object>> scan(HttpServletRequest request, @RequestBody Map<String, Object> body) { return checkin(request); }

    @GetMapping("/checkin/records/month")
    public ResponseEntity<Map<String, Object>> monthRecords(HttpServletRequest request, @RequestParam int year, @RequestParam int month) {
        return ok(store.monthlyRecords(username(request), year, month));
    }

    @GetMapping("/checkin/records")
    public ResponseEntity<Map<String, Object>> records(HttpServletRequest request) {
        LocalDate now = LocalDate.now();
        return ok(store.monthlyRecords(username(request), now.getYear(), now.getMonthValue()));
    }

    @GetMapping("/points/my")
    public ResponseEntity<Map<String, Object>> myPoints(HttpServletRequest request) {
        String username = username(request);
        int available = store.points.containsKey(username) ? store.points.get(username) : 0;
        return ok(store.m("availablePoints", available, "totalPoints", 2680, "totalEarned", 2680, "totalUsed", 1400, "level", "Gold"));
    }

    @GetMapping("/points/products")
    public ResponseEntity<Map<String, Object>> pointProducts() {
        store.ensureOperationalData();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.pointProducts) {
            for (Map<String, Object> p : store.pointProducts) if (!Boolean.FALSE.equals(p.get("active"))) out.add(new LinkedHashMap<String, Object>(p));
        }
        return ok(out);
    }

    @GetMapping("/points/products/vip")
    public ResponseEntity<Map<String, Object>> vipProducts() {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> p : store.pointProducts) if (Boolean.TRUE.equals(p.get("isVip"))) out.add(p);
        return ok(out);
    }

    @PostMapping("/points/exchange/{productId}")
    public ResponseEntity<Map<String, Object>> exchange(HttpServletRequest request, @PathVariable Long productId) {
        store.ensureOperationalData();
        String username = username(request);
        synchronized (store.pointProducts) {
            Map<String, Object> product = null;
            for (Map<String, Object> p : store.pointProducts) if (Objects.equals(store.asLong(p.get("id")), productId)) { product = p; break; }
            if (product == null) return fail("商品不存在");
            if (Boolean.FALSE.equals(product.get("active"))) return fail("商品已下架");
            int cost = store.asInt(product.get("pointsCost"), store.asInt(product.get("points"), 0));
            int balance = store.points.containsKey(username) ? store.points.get(username) : 0;
            if (balance < cost) return fail("积分不足");
            int stock = store.asInt(product.get("stockQuantity"), store.asInt(product.get("stock"), 999));
            if (stock <= 0) return fail("库存不足");
            int nextStock = Math.max(0, stock - 1);
            int nextSold = store.asInt(product.get("soldCount"), 0) + 1;
            store.points.put(username, balance - cost);
            product.put("stockQuantity", nextStock);
            product.put("stock", nextStock);
            product.put("soldCount", nextSold);
            Map<String, Object> item = store.m("id", store.nextId(), "username", username, "productId", productId, "productName", product.get("name"), "productNameEn", product.get("nameEn"), "pointsCost", cost, "exchangeTime", LocalDateTime.now().toString(), "status", "COMPLETED");
            store.exchanges.add(0, item);
            store.saveToMysqlQuietly();
            return ok(store.m("balance", balance - cost, "exchange", item, "product", new LinkedHashMap<String, Object>(product)));
        }
    }

    @GetMapping("/points/history")
    public ResponseEntity<Map<String, Object>> history(HttpServletRequest request) {
        String username = username(request);
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.exchanges) {
            for (Map<String, Object> e : store.exchanges) if (username.equals(e.get("username"))) out.add(new LinkedHashMap<String, Object>(e));
        }
        return ok(out);
    }

    @GetMapping("/ranking") public ResponseEntity<Map<String, Object>> rankDefault() { return rankingResponse("total"); }
    @GetMapping("/ranking/total") public ResponseEntity<Map<String, Object>> rankTotal() { return rankingResponse("total"); }
    @GetMapping("/ranking/week") public ResponseEntity<Map<String, Object>> rankWeek() { return rankingResponse("week"); }
    @GetMapping("/ranking/month") public ResponseEntity<Map<String, Object>> rankMonth() { return rankingResponse("month"); }

    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> profile(HttpServletRequest request) {
        Map<String, Object> user = store.userByUsername(username(request));
        return user == null ? fail("用户不存在") : ok(user);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String, Object> user = store.userByUsername(username(request));
        if (user == null) return fail("用户不存在");
        for (String key : Arrays.asList("name", "email", "phone", "gender", "birthday", "address", "medicalNotes")) if (body.containsKey(key)) user.put(key, body.get(key));
        return ok(user);
    }

    @GetMapping("/user/checkin-qr-token")
    public ResponseEntity<Map<String, Object>> qr(HttpServletRequest request) {
        return ok(store.m("token", "GYM-CHECKIN-" + username(request) + "-" + System.currentTimeMillis(), "expiresIn", 300));
    }

    @GetMapping("/user/membership")
    public ResponseEntity<Map<String, Object>> membership(HttpServletRequest request) {
        return ok(store.memberships.get(username(request)));
    }

    @PostMapping("/user/membership/purchase-vip")
    public ResponseEntity<Map<String, Object>> buyVip(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        String user = username(request);
        Map<String, Object> current = store.memberships.get(user);
        LocalDate base = LocalDate.now();
        if (current != null) {
            try {
                LocalDate existing = LocalDate.parse(String.valueOf(current.get("endDate")));
                if (existing.isAfter(base)) base = existing;
            } catch (Exception ignored) { }
        }
        String plan = store.str(body.get("plan"), "year");
        LocalDate end = "month".equals(plan) ? base.plusMonths(1) : ("family".equals(plan) ? base.plusYears(1) : base.plusYears(1));
        Map<String, Object> vip = store.m("membershipType", "VIP年卡", "startDate", LocalDate.now().toString(), "endDate", end.toString(), "status", "ACTIVE", "remainingSessions", 60, "daysRemaining", java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), end));
        store.memberships.put(user, vip);
        return ok(vip);
    }

    @GetMapping("/advertisements") public ResponseEntity<Map<String, Object>> ads() { return ok(store.ads); }
    @GetMapping("/teams")
    public ResponseEntity<Map<String, Object>> teams(HttpServletRequest request) {
        store.ensureOperationalData();
        String current = username(request);
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.teams) {
            for (Map<String, Object> t : store.teams) out.add(teamView(t, current));
        }
        return ok(out);
    }

    @PostMapping("/teams")
    public ResponseEntity<Map<String, Object>> createTeam(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        String creator = username(request);
        Map<String, Object> user = store.userByUsername(creator);
        int maxMembers = store.asInt(body.get("maxMembers"), store.asInt(body.get("capacity"), 10));
        Map<String, Object> team = store.m(
                "id", store.nextId(),
                "title", store.str(body.get("title"), "新健身小队"),
                "description", store.str(body.get("description"), "一起训练，一起进步。"),
                "category", store.str(body.get("category"), "running"),
                "location", store.str(body.get("location"), "FitLife健身房"),
                "meetTime", store.str(body.get("meetTime"), "周末 09:00"),
                "creator", creator,
                "creatorName", user == null ? creator : user.get("name"),
                "currentMembers", 1,
                "maxMembers", maxMembers,
                "members", 1,
                "capacity", maxMembers,
                "joined", true,
                "createdByCurrentUser", true,
                "memberUsernames", new ArrayList<String>(Arrays.asList(creator)));
        store.teams.add(0, team);
        return ok(team);
    }

    @PostMapping("/teams/{id}/join")
    public ResponseEntity<Map<String, Object>> joinTeam(HttpServletRequest request, @PathVariable Long id) {
        String current = username(request);
        synchronized (store.teams) {
            for (Map<String, Object> t : store.teams) {
                if (!Objects.equals(store.asLong(t.get("id")), id)) continue;
                String creator = store.str(t.get("creator"), "");
                List<String> members = mutableStringList(t, "memberUsernames");
                if (creator.length() > 0 && !members.contains(creator)) members.add(creator);
                int currentMembers = store.asInt(t.get("currentMembers"), store.asInt(t.get("members"), members.size()));
                int maxMembers = store.asInt(t.get("maxMembers"), store.asInt(t.get("capacity"), 10));
                if (!members.contains(current)) {
                    if (currentMembers >= maxMembers) return fail("小队人数已满");
                    members.add(current);
                    currentMembers += 1;
                }
                t.put("currentMembers", currentMembers);
                t.put("members", currentMembers);
                t.put("maxMembers", maxMembers);
                t.put("capacity", maxMembers);
                return ok(teamView(t, current));
            }
        }
        return fail("小队不存在");
    }

    @PostMapping("/teams/{id}/leave")
    public ResponseEntity<Map<String, Object>> leaveTeam(HttpServletRequest request, @PathVariable Long id) {
        String current = username(request);
        synchronized (store.teams) {
            for (Map<String, Object> t : store.teams) {
                if (!Objects.equals(store.asLong(t.get("id")), id)) continue;
                String creator = store.str(t.get("creator"), "");
                if (current.equals(creator)) return fail("创建者不能退出自己创建的小队");
                List<String> members = mutableStringList(t, "memberUsernames");
                int currentMembers = store.asInt(t.get("currentMembers"), store.asInt(t.get("members"), members.size()));
                if (members.remove(current)) currentMembers = Math.max(0, currentMembers - 1);
                int maxMembers = store.asInt(t.get("maxMembers"), store.asInt(t.get("capacity"), 10));
                t.put("currentMembers", currentMembers);
                t.put("members", currentMembers);
                t.put("maxMembers", maxMembers);
                t.put("capacity", maxMembers);
                return ok(teamView(t, current));
            }
        }
        return fail("小队不存在");
    }

    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> posts(HttpServletRequest request) {
        store.ensureOperationalData();
        String current = username(request);
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.posts) {
            for (Map<String, Object> p : store.posts) out.add(postView(p, current));
        }
        return ok(out);
    }

    @PostMapping("/posts")
    public ResponseEntity<Map<String, Object>> createPost(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String, Object> user = store.userByUsername(username(request));
        Long id = store.nextId();
        Map<String, Object> post = store.m("id", id, "author", user == null ? username(request) : user.get("name"), "username", username(request), "category", store.str(body.get("category"), "分享"), "content", store.str(body.get("content"), "新的健身动态"), "likes", 0, "comments", 0, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().toString());
        store.posts.add(0, post);
        store.postComments.put(id, Collections.synchronizedList(new ArrayList<Map<String, Object>>()));
        return ok(post);
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, Object>> like(HttpServletRequest request, @PathVariable Long id) {
        String current = username(request);
        synchronized (store.posts) {
            for (Map<String, Object> p : store.posts) {
                if (!Objects.equals(store.asLong(p.get("id")), id)) continue;
                List<String> likedBy = mutableStringList(p, "likedBy");
                boolean alreadyLiked = likedBy.contains(current);
                if (alreadyLiked) likedBy.remove(current);
                else likedBy.add(current);
                int likes = Math.max(0, store.asInt(p.get("likes"), 0) + (alreadyLiked ? -1 : 1));
                p.put("likes", likes);
                p.put("liked", !alreadyLiked);
                if (!alreadyLiked) store.points.put(current, store.points.getOrDefault(current, 0) + 2);
                Map<String, Object> data = postView(p, current);
                Map<String, Object> out = store.ok(data);
                out.put("likes", likes);
                out.put("alreadyLiked", alreadyLiked);
                return ResponseEntity.ok(out);
            }
        }
        return fail("帖子不存在");
    }

    @PostMapping("/posts/{id}/report")
    public ResponseEntity<Map<String, Object>> report(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> r = store.m("id", store.nextId(), "postId", id, "reason", store.str(body.get("reason"), "用户举报"), "status", "PENDING", "reporter", username(request));
        store.reports.add(0, r);
        return ok(r);
    }

    @PostMapping("/complaints")
    public ResponseEntity<Map<String, Object>> complaint(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String, Object> user = store.userByUsername(username(request));
        String type = store.str(body.get("type"), "other");
        String content = store.str(body.get("content"), "");
        Map<String, Object> c = store.m("id", store.nextId(), "username", username(request), "userName", user == null ? username(request) : user.get("name"), "type", type, "title", store.str(body.get("title"), "会员反馈"), "titleEn", "Member feedback", "content", content, "contentEn", content, "coachName", store.str(body.get("coachName"), "客服中心"), "status", "PENDING", "createdAt", LocalDateTime.now().toString());
        store.complaints.add(0, c);
        return ok(c);
    }

    @GetMapping("/complaints/user")
    public ResponseEntity<Map<String, Object>> userComplaints(HttpServletRequest request) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> c : store.complaints) if (username(request).equals(c.get("username"))) out.add(c);
        return ok(out);
    }

    @GetMapping("/products") public ResponseEntity<Map<String, Object>> products() { store.ensureOperationalData(); return ok(store.products); }

    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody Map<String, Object> body) {
        Map<String, Object> p = new LinkedHashMap<String, Object>(body);
        p.put("id", store.nextId());
        if (!p.containsKey("stockQuantity")) p.put("stockQuantity", 10);
        if (!p.containsKey("imageKey")) p.put("imageKey", "equipment");
        store.products.add(0, p);
        return ok(p);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> editProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> p : store.products) if (Objects.equals(store.asLong(p.get("id")), id)) { p.putAll(body); p.put("id", id); return ok(p); }
        return fail("商品不存在");
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Iterator<Map<String, Object>> it = store.products.iterator();
        while (it.hasNext()) if (Objects.equals(store.asLong(it.next().get("id")), id)) { it.remove(); store.saveToMysqlQuietly(); return ok(store.m("deleted", true)); }
        return fail("商品不存在");
    }

    @GetMapping("/users") public ResponseEntity<Map<String, Object>> allUsers() { return ok(store.users); }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody Map<String, Object> body) {
        Map<String, Object> user = new LinkedHashMap<String, Object>(body);
        String username = store.str(user.get("username"), "member" + store.nextId());
        user.put("id", store.nextId()); user.put("username", username); user.put("role", store.str(user.get("role"), "USER")); user.put("active", !Boolean.FALSE.equals(user.get("active"))); user.put("avatar", "avatar2");
        store.users.add(0, user); store.points.put(username, store.points.getOrDefault(username, 500));
        syncUserMembership(user, body);
        return ok(adminUserRow(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> editUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> u : store.users) if (Objects.equals(store.asLong(u.get("id")), id)) { u.putAll(body); u.put("id", id); syncUserMembership(u, body); return ok(adminUserRow(u)); }
        return fail("用户不存在");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Iterator<Map<String, Object>> it = store.users.iterator();
        while (it.hasNext()) {
            Map<String, Object> u = it.next();
            if (Objects.equals(store.asLong(u.get("id")), id)) {
                String uname = store.str(u.get("username"), "");
                it.remove(); store.memberships.remove(uname); store.points.remove(uname);
                return ok(store.m("deleted", true));
            }
        }
        return fail("用户不存在");
    }

    @GetMapping("/admin/users")
    public ResponseEntity<Map<String, Object>> adminUsers() { store.ensureOperationalData(); return ResponseEntity.ok(adminUsersPayload()); }
    @PostMapping("/admin/users") public ResponseEntity<Map<String, Object>> adminAddUser(@RequestBody Map<String, Object> body) { return addUser(body); }
    @PutMapping("/admin/users/{id}") public ResponseEntity<Map<String, Object>> adminEditUser(@PathVariable Long id, @RequestBody Map<String, Object> body) { return editUser(id, body); }
    @DeleteMapping("/admin/users/{id}") public ResponseEntity<Map<String, Object>> adminDeleteUser(@PathVariable Long id) { return deleteUser(id); }

    @GetMapping("/admin/classes") public ResponseEntity<Map<String, Object>> adminClasses() { store.ensureOperationalData(); return ok(store.classes); }

    @PostMapping("/admin/classes")
    public ResponseEntity<Map<String, Object>> adminAddClass(@RequestBody Map<String, Object> body) {
        Map<String, Object> c = new LinkedHashMap<String, Object>(body);
        c.put("id", store.nextId());
        if (!c.containsKey("type")) c.put("type", "GROUP");
        if (!c.containsKey("status")) c.put("status", "ACTIVE");
        if (!c.containsKey("bookedCount")) c.put("bookedCount", 0);
        store.classes.add(0, c);
        store.saveToMysqlQuietly();
        return ok(c);
    }

    @PutMapping("/admin/classes/{id}")
    public ResponseEntity<Map<String, Object>> adminEditClass(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> c = store.classById(id);
        if (c == null) return fail("课程不存在");
        c.putAll(body); c.put("id", id); store.saveToMysqlQuietly(); return ok(c);
    }

    @DeleteMapping("/admin/classes/{id}")
    public ResponseEntity<Map<String, Object>> adminDeleteClass(@PathVariable Long id) {
        Iterator<Map<String, Object>> it = store.classes.iterator();
        while (it.hasNext()) if (Objects.equals(store.asLong(it.next().get("id")), id)) { it.remove(); store.saveToMysqlQuietly(); return ok(store.m("deleted", true)); }
        return fail("课程不存在");
    }

    @GetMapping("/admin/products") public ResponseEntity<Map<String, Object>> adminProducts() { store.ensureOperationalData(); return ok(store.pointProducts); }
    @PostMapping("/admin/products") public ResponseEntity<Map<String, Object>> adminAddProduct(@RequestBody Map<String, Object> body) { return adminAddPointProduct(body); }
    @PutMapping("/admin/products/{id}") public ResponseEntity<Map<String, Object>> adminEditProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) { return adminEditPointProduct(id, body); }
    @DeleteMapping("/admin/products/{id}") public ResponseEntity<Map<String, Object>> adminDeleteProduct(@PathVariable Long id) { return adminDeletePointProduct(id); }

    @GetMapping("/admin/reports") public ResponseEntity<Map<String, Object>> adminReports() {
        store.ensureOperationalData();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.reports) {
            for (Map<String, Object> r : store.reports) {
                Map<String, Object> row = new LinkedHashMap<String, Object>(r);
                Long postId = store.asLong(row.get("postId"));
                if (postId != null) {
                    synchronized (store.posts) {
                        for (Map<String, Object> p : store.posts) {
                            if (Objects.equals(store.asLong(p.get("id")), postId)) {
                                row.put("postContent", p.get("content"));
                                row.put("reportedContent", p.get("content"));
                                break;
                            }
                        }
                    }
                }
                if (!row.containsKey("createdAt")) row.put("createdAt", LocalDateTime.now().minusDays(1).toString());
                out.add(row);
            }
        }
        return ok(out);
    }
    @PostMapping("/admin/reports/{id}/process") public ResponseEntity<Map<String, Object>> processReport(@PathVariable Long id) { for (Map<String, Object> r : store.reports) if (Objects.equals(store.asLong(r.get("id")), id)) r.put("status", "PROCESSED"); return ok(store.reports); }
    @PostMapping("/admin/reports/{id}/ignore") public ResponseEntity<Map<String, Object>> ignoreReport(@PathVariable Long id) { for (Map<String, Object> r : store.reports) if (Objects.equals(store.asLong(r.get("id")), id)) r.put("status", "IGNORED"); return ok(store.reports); }
    @GetMapping("/admin/posts") public ResponseEntity<Map<String, Object>> adminPosts() { store.ensureOperationalData(); return ok(store.posts); }
    @DeleteMapping("/admin/posts/{id}") public ResponseEntity<Map<String, Object>> adminDeletePost(@PathVariable Long id) { Iterator<Map<String, Object>> it = store.posts.iterator(); while (it.hasNext()) if (Objects.equals(store.asLong(it.next().get("id")), id)) { it.remove(); return ok(store.m("deleted", true)); } return fail("帖子不存在"); }

    @GetMapping("/admin/coaches") public ResponseEntity<Map<String, Object>> adminCoaches() { store.ensureOperationalData(); return ok(store.coaches); }

    @GetMapping("/coach/profile")
    public ResponseEntity<Map<String, Object>> coachProfile(HttpServletRequest request) {
        store.ensureOperationalData();
        Map<String, Object> coach = store.coachByUsername(username(request));
        return coach == null ? ok(store.coaches.isEmpty() ? store.m() : store.coaches.get(0)) : ok(coach);
    }

    @GetMapping("/coach/active-users")
    public ResponseEntity<Map<String, Object>> activeUsers() {
        store.ensureOperationalData();
        int count = 0;
        synchronized (store.users) {
            for (Map<String, Object> user : store.users) if (Boolean.TRUE.equals(user.get("active"))) count++;
        }
        return ok(count);
    }

    @GetMapping("/coach/today-schedule")
    public ResponseEntity<Map<String, Object>> todaySchedule(HttpServletRequest request) {
        store.ensureOperationalData();
        Map<String, Object> coach = store.coachByUsername(username(request));
        Long id = coach == null ? 1L : store.asLong(coach.get("id"));
        String today = LocalDate.now().toString();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> c : classViews(store.classes, username(request), null, null)) {
            if (Objects.equals(store.asLong(c.get("coachId")), id) && today.equals(c.get("classDate"))) out.add(c);
        }
        return ok(out);
    }
    @GetMapping("/coach/classes")
    public ResponseEntity<Map<String, Object>> coachClasses(HttpServletRequest request) {
        store.ensureOperationalData();
        Map<String, Object> coach = store.coachByUsername(username(request));
        Long id = coach == null ? 1L : store.asLong(coach.get("id"));
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> c : classViews(store.classes, username(request), null, null)) if (Objects.equals(store.asLong(c.get("coachId")), id)) out.add(c);
        return ok(out);
    }

    @PostMapping("/coach/classes")
    public ResponseEntity<Map<String, Object>> coachAddClass(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Map<String,Object> coach = store.coachByUsername(username(request));
        String date = store.str(body.get("date"), LocalDate.now().toString());
        String start = store.str(body.get("startTime"), "09:00");
        String end = store.str(body.get("endTime"), "10:00");
        body.put("coachId", coach == null ? 1L : coach.get("id"));
        body.put("coachName", coach == null ? "张教练" : coach.get("name"));
        body.put("instructor", coach == null ? "张教练" : coach.get("name"));
        body.put("startTime", date + "T" + start);
        body.put("endTime", date + "T" + end);
        body.put("bookedCount", 0);
        body.put("status", "upcoming");
        ResponseEntity<Map<String, Object>> resp = adminAddClass(body);
        Long bookingId = store.asLong(body.get("reservedBookingId"));
        if (bookingId != null) for (Map<String, Object> b : store.facilityBookings) if (Objects.equals(store.asLong(b.get("id")), bookingId)) b.put("usedByClass", true);
        store.saveToMysqlQuietly();
        return resp;
    }
    @PutMapping("/coach/classes/{id}") public ResponseEntity<Map<String, Object>> coachEditClass(@PathVariable Long id, @RequestBody Map<String, Object> body) { return adminEditClass(id, body); }
    @DeleteMapping("/coach/classes/{id}") public ResponseEntity<Map<String, Object>> coachDeleteClass(@PathVariable Long id) { return adminDeleteClass(id); }
    @GetMapping("/coach/students")
    public ResponseEntity<Map<String, Object>> students(HttpServletRequest request) {
        store.ensureOperationalData();
        Map<String,Object> coach = store.coachByUsername(username(request));
        Long coachId = coach == null ? 1L : store.asLong(coach.get("id"));
        Set<String> usernames = new LinkedHashSet<String>();
        synchronized (store.bookings) {
            for (Map<String,Object> b : store.bookings) {
                if (!confirmedStatus(b.get("status"))) continue;
                Map<String,Object> c = store.classById(store.asLong(b.get("classId")));
                if (c != null && Objects.equals(store.asLong(c.get("coachId")), coachId)) usernames.add(store.str(b.get("username"), ""));
            }
        }
        List<Map<String,Object>> out = new ArrayList<Map<String,Object>>();
        synchronized (store.users) {
            for (Map<String,Object> u : store.users) {
                if (usernames.isEmpty() || usernames.contains(store.str(u.get("username"), ""))) {
                    Map<String,Object> copy = new LinkedHashMap<String,Object>(u);
                    String uname = store.str(copy.get("username"), "");
                    Map<String, Object> membership = store.memberships.get(uname);
                    copy.put("membershipType", membership == null ? String.valueOf(copy.getOrDefault("membershipType", copy.getOrDefault("level", "Regular Member"))) : store.str(membership.get("membershipType"), "Regular Member"));
                    copy.put("attended", true);
                    out.add(copy);
                }
            }
        }
        return ok(out);
    }
    @GetMapping("/coach/complaints") public ResponseEntity<Map<String, Object>> coachComplaints(HttpServletRequest request) { store.ensureOperationalData(); return ok(store.complaints); }
    @PostMapping("/coach/complaints/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondComplaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> c : store.complaints) {
            if (!Objects.equals(store.asLong(c.get("id")), id)) continue;
            c.put("status", "PROCESSED");
            c.put("response", store.str(body.get("response"), store.str(body.get("reply"), "已收到反馈并处理。")));
            c.put("responseEn", store.str(body.get("responseEn"), "Feedback received and handled."));
            c.put("handledAt", LocalDateTime.now().toString());
            store.saveToMysqlQuietly();
            return ok(c);
        }
        return fail("反馈不存在");
    }

    @GetMapping("/coach/reviews") public ResponseEntity<Map<String, Object>> coachReviews() { store.ensureOperationalData(); return ok(store.reviews); }

    @PostMapping("/coach/reviews/{id}/respond")
    public ResponseEntity<Map<String, Object>> respondReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> r : store.reviews) {
            if (!Objects.equals(store.asLong(r.get("id")), id)) continue;
            r.put("responded", true);
            r.put("response", store.str(body.get("response"), store.str(body.get("reply"), "感谢评价。")));
            r.put("responseEn", store.str(body.get("responseEn"), "Thank you for your feedback."));
            r.put("respondedAt", LocalDateTime.now().toString());
            store.saveToMysqlQuietly();
            return ok(r);
        }
        return fail("评价不存在");
    }

    @GetMapping("/coach/notifications")
    public ResponseEntity<Map<String, Object>> notifications() {
        return ok(Arrays.asList(
                store.m("id", 1, "icon", "reminder", "sk", "1", "title", "力量训练课程将在30分钟后开始", "titleEn", "Strength class starts in 30 minutes", "time", "10分钟前", "timeEn", "10 minutes ago", "type", "classReminder", "read", false),
                store.m("id", 2, "icon", "mail", "sk", "2", "title", "收到新的课程预约请求", "titleEn", "New class booking request", "time", "1小时前", "timeEn", "1 hour ago", "type", "bookingRequest", "read", false)
        ));
    }

    @GetMapping("/users/profile")
    public ResponseEntity<Map<String, Object>> usersProfile(HttpServletRequest request) { return profile(request); }

    @PutMapping("/users/profile")
    public ResponseEntity<Map<String, Object>> usersProfileUpdate(HttpServletRequest request, @RequestBody Map<String, Object> body) { return updateProfile(request, body); }

    @PostMapping("/membership/buy")
    public ResponseEntity<Map<String, Object>> buyMembership(HttpServletRequest request, @RequestBody Map<String, Object> body) { return buyVip(request, body); }

    @PostMapping("/cart")
    public ResponseEntity<Map<String, Object>> addCart(@RequestBody Map<String, Object> body) { return ok(store.m("added", true, "item", body)); }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> order(@RequestBody Map<String, Object> body) { return ok(store.m("orderNo", "FL" + System.currentTimeMillis(), "status", "PAID", "item", body)); }

    @PostMapping("/orders/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Object body) { return ok(store.m("orderNo", "FL" + System.currentTimeMillis(), "status", "PAID", "items", body)); }

    @GetMapping("/classes/group")
    public ResponseEntity<Map<String, Object>> groupClasses(HttpServletRequest request) { return classes(request, null); }

    @PostMapping("/bookings/group")
    public ResponseEntity<Map<String, Object>> groupBook(HttpServletRequest request, @RequestBody Map<String, Object> body) { return book(request, body); }

    @GetMapping("/points/admin/products")
    public ResponseEntity<Map<String, Object>> adminPointProducts() { store.ensureOperationalData(); return ok(store.pointProducts); }

    @PostMapping("/points/admin/products")
    public ResponseEntity<Map<String, Object>> adminAddPointProduct(@RequestBody Map<String, Object> body) {
        Map<String, Object> p = new LinkedHashMap<String, Object>(body);
        p.put("id", store.nextId());
        if (!p.containsKey("pointsCost")) p.put("pointsCost", store.asInt(p.get("points"), 100));
        if (!p.containsKey("stock")) p.put("stock", store.asInt(p.get("stockQuantity"), 100));
        if (!p.containsKey("stockQuantity")) p.put("stockQuantity", store.asInt(p.get("stock"), 100));
        if (!p.containsKey("soldCount")) p.put("soldCount", 0);
        if (!p.containsKey("imageKey")) p.put("imageKey", "gift");
        if (!p.containsKey("isVip")) p.put("isVip", false);
        store.pointProducts.add(0, p);
        store.saveToMysqlQuietly();
        return ok(p);
    }

    @PutMapping("/points/admin/products/{id}")
    public ResponseEntity<Map<String, Object>> adminEditPointProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> p : store.pointProducts) if (Objects.equals(store.asLong(p.get("id")), id)) { p.putAll(body); p.put("id", id); store.saveToMysqlQuietly(); return ok(p); }
        return fail("商品不存在");
    }

    @DeleteMapping("/points/admin/products/{id}")
    public ResponseEntity<Map<String, Object>> adminDeletePointProduct(@PathVariable Long id) {
        Iterator<Map<String, Object>> it = store.pointProducts.iterator();
        while (it.hasNext()) if (Objects.equals(store.asLong(it.next().get("id")), id)) { it.remove(); store.saveToMysqlQuietly(); return ok(store.m("deleted", true)); }
        return fail("商品不存在");
    }

    @PutMapping("/admin/posts/{id}")
    public ResponseEntity<Map<String, Object>> adminUpdatePost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        for (Map<String, Object> p : store.posts) if (Objects.equals(store.asLong(p.get("id")), id)) { p.putAll(body); p.put("id", id); return ok(p); }
        return fail("帖子不存在");
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Map<String, Object>> postComments(@PathVariable Long id) {
        List<Map<String, Object>> comments = store.postComments.get(id);
        if (comments == null) comments = Collections.emptyList();
        return ok(comments);
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<Map<String, Object>> addComment(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        String current = username(request);
        Map<String, Object> user = store.userByUsername(current);
        Map<String, Object> comment = store.m("id", store.nextId(), "postId", id, "username", current, "author", user == null ? current : user.get("name"), "content", store.str(body.get("content"), "不错"), "createdAt", LocalDateTime.now().toString());
        store.points.put(current, store.points.getOrDefault(current, 0) + 5);
        if (!store.postComments.containsKey(id)) store.postComments.put(id, Collections.synchronizedList(new ArrayList<Map<String, Object>>()));
        store.postComments.get(id).add(comment);
        int comments = 0;
        for (Map<String, Object> p : store.posts) {
            if (!Objects.equals(store.asLong(p.get("id")), id)) continue;
            comments = store.asInt(p.get("comments"), 0) + 1;
            p.put("comments", comments);
            break;
        }
        Map<String, Object> out = store.ok(comment);
        out.put("comments", comments);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/coach/facilities")
    public ResponseEntity<Map<String, Object>> facilities() { return ok(store.facilities); }

    @GetMapping("/coach/facilities/my-bookings")
    public ResponseEntity<Map<String, Object>> myFacilityBookings(HttpServletRequest request) {
        String user = username(request);
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (store.facilityBookings) {
            for (Map<String, Object> b : store.facilityBookings) if (user.equals(b.get("coachUsername"))) out.add(b);
        }
        return ok(out);
    }

    @PostMapping("/coach/facilities/book")
    public ResponseEntity<Map<String, Object>> bookFacility(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long facilityId = store.asLong(body.get("facilityId"));
        Map<String, Object> f = facilityById(facilityId);
        if (f == null) return fail("场地不存在");
        String timeSlot = store.str(body.get("timeSlot"), "09:00-10:00");
        String[] parts = splitSlot(timeSlot);
        LocalDate date;
        try { date = LocalDate.parse(store.str(body.get("date"), LocalDate.now().plusDays(1).toString())); }
        catch (Exception ex) { date = LocalDate.now().plusDays(1); }
        synchronized (store.facilityBookings) {
            for (Map<String,Object> b : store.facilityBookings) {
                if (Objects.equals(store.asLong(b.get("facilityId")), facilityId)
                        && store.str(b.get("timeSlot"), "").replace(" ", "").equals(timeSlot.replace(" ", ""))
                        && date.toString().equals(store.str(b.get("date"), ""))) return fail("该场地时段已被预约");
            }
        }
        Map<String, Object> booking = store.m("id", store.nextId(), "facilityId", facilityId, "facilityName", f.get("name"), "facilityNameEn", f.get("nameEn"),
                "coachUsername", username(request), "date", date.toString(), "startTime", parts[0], "endTime", parts[1], "timeSlot", timeSlot, "status", "RESERVED", "usedByClass", false);
        store.facilityBookings.add(0, booking);
        return ok(booking);
    }

    @DeleteMapping("/coach/facilities/bookings/{id}")
    public ResponseEntity<Map<String, Object>> cancelFacility(@PathVariable Long id) {
        Iterator<Map<String, Object>> it = store.facilityBookings.iterator();
        while (it.hasNext()) if (Objects.equals(store.asLong(it.next().get("id")), id)) { it.remove(); store.saveToMysqlQuietly(); return ok(store.m("deleted", true)); }
        return fail("预约不存在");
    }

    @PostMapping("/coach/notification-settings")
    public ResponseEntity<Map<String, Object>> saveNotificationSettings(@RequestBody Map<String, Object> body) { return ok(body); }

}
