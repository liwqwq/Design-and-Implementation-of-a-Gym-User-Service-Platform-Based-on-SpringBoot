package com.fitlife.platform.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class VisualStore {
    private static final String STATE_KEY = "fitlife-v49-rich-database-state";

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean mysqlAvailable = false;

    private final AtomicLong idGen = new AtomicLong(1000);
    public final List<Map<String, Object>> users = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> coaches = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> classes = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> products = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> pointProducts = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> posts = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final Map<Long, List<Map<String, Object>>> postComments = new ConcurrentHashMap<Long, List<Map<String, Object>>>();
    public final List<Map<String, Object>> teams = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> ads = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> complaints = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> reviews = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> reports = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> bookings = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> exchanges = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> facilities = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final List<Map<String, Object>> facilityBookings = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
    public final Map<String, Integer> points = new ConcurrentHashMap<String, Integer>();
    public final Map<String, Set<String>> checkinDates = new ConcurrentHashMap<String, Set<String>>();
    public final Map<String, Map<String, Object>> memberships = new ConcurrentHashMap<String, Map<String, Object>>();

    public VisualStore() {
    }

    @PostConstruct
    public synchronized void init() {
        if (tryLoadFromMysql()) return;
        seedAll();
        saveToMysqlQuietly();
    }

    private void seedAll() {
        users.clear();
        coaches.clear();
        classes.clear();
        products.clear();
        pointProducts.clear();
        posts.clear();
        postComments.clear();
        teams.clear();
        ads.clear();
        complaints.clear();
        reviews.clear();
        reports.clear();
        bookings.clear();
        exchanges.clear();
        facilities.clear();
        facilityBookings.clear();
        points.clear();
        checkinDates.clear();
        memberships.clear();
        idGen.set(100);
        seedUsers();
        seedCoaches();
        seedClasses();
        seedBookings();
        seedFacilities();
        seedCheckins();
        seedProducts();
        seedSocial();
        seedMailbox();
        seedMemberships();
        long max = 100;
        for (List<Map<String, Object>> list : Arrays.asList(users, coaches, classes, products, pointProducts, posts, teams, ads, complaints, reviews, reports, bookings, exchanges, facilities, facilityBookings)) {
            for (Map<String, Object> row : list) max = Math.max(max, asLong(row.get("id")));
        }
        idGen.set(max + 10);
    }

    private boolean repairMissingSeedData() {
        boolean changed = false;
        if (users.isEmpty()) { seedUsers(); changed = true; }
        if (coaches.isEmpty()) { seedCoaches(); changed = true; }
        if (classes.isEmpty()) { seedClasses(); changed = true; }
        if (bookings.isEmpty() && !classes.isEmpty()) { seedBookings(); changed = true; }
        if (facilities.isEmpty()) { seedFacilities(); changed = true; }
        if (checkinDates.isEmpty() && !users.isEmpty()) { seedCheckins(); changed = true; }
        if (products.isEmpty() || pointProducts.isEmpty()) {
            if (products.isEmpty() && pointProducts.isEmpty()) seedProducts();
            else { products.clear(); pointProducts.clear(); seedProducts(); }
            changed = true;
        }
        if (pointProducts.size() < 6) {
            pointProducts.clear();
            products.clear();
            seedProducts();
            changed = true;
        }
        if (posts.isEmpty() || teams.isEmpty()) {
            if (posts.isEmpty() && teams.isEmpty()) seedSocial();
            else { posts.clear(); teams.clear(); ads.clear(); reports.clear(); postComments.clear(); seedSocial(); }
            changed = true;
        }
        if (complaints.isEmpty() && reviews.isEmpty()) { seedMailbox(); changed = true; }
        if (memberships.isEmpty() && !users.isEmpty()) { seedMemberships(); changed = true; }
        if (points.isEmpty() && !users.isEmpty()) {
            int value = 1280;
            for (Map<String, Object> u : users) {
                points.put(str(u.get("username"), "user"), value);
                value = Math.max(100, value - 80);
            }
            changed = true;
        }
        long max = 100;
        for (List<Map<String, Object>> list : Arrays.asList(users, coaches, classes, products, pointProducts, posts, teams, ads, complaints, reviews, reports, bookings, exchanges, facilities, facilityBookings)) {
            for (Map<String, Object> row : list) {
                Long id = asLong(row.get("id"));
                if (id != null) max = Math.max(max, id);
            }
        }
        if (idGen.get() <= max) { idGen.set(max + 10); changed = true; }
        return changed;
    }

    private boolean tryLoadFromMysql() {
        try {
            if (jdbcTemplate == null) return false;
            ensureMysqlSchema();
            List<String> rows = jdbcTemplate.queryForList("select state_json from fitlife_state where state_key=?", String.class, STATE_KEY);
            mysqlAvailable = true;
            if (rows.isEmpty()) {
                seedAll();
                saveToMysqlQuietly();
                return true;
            }
            Map<String, Object> state = objectMapper.readValue(rows.get(0), new TypeReference<Map<String, Object>>() {});
            applyState(state);
            boolean repaired = repairMissingSeedData();
            if (repaired) saveToMysqlQuietly();
            else syncReadableMysqlTables();
            return true;
        } catch (Exception ex) {
            mysqlAvailable = false;
            System.err.println("[FitLife] MySQL state load failed; using in-memory fallback: " + ex.getMessage());
            return false;
        }
    }

    private void ensureMysqlSchema() {
        jdbcTemplate.execute("create table if not exists fitlife_state (" +
                "state_key varchar(64) primary key," +
                "state_json longtext not null," +
                "updated_at timestamp default current_timestamp on update current_timestamp" +
                ") engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_users (id bigint primary key, username varchar(80), name varchar(120), email varchar(160), phone varchar(60), role varchar(40), active tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_coaches (id bigint primary key, username varchar(80), name varchar(120), email varchar(160), specialty varchar(255), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_memberships (username varchar(80) primary key, membership_type varchar(80), end_date varchar(40), status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_classes (id bigint primary key, name varchar(180), name_en varchar(180), coach_name varchar(120), type varchar(40), start_time varchar(40), end_time varchar(40), capacity int, booked_count int, status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_products (id bigint primary key, name varchar(180), name_en varchar(180), category varchar(100), price decimal(10,2), stock_quantity int, active tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_point_products (id bigint primary key, name varchar(180), name_en varchar(180), category varchar(100), points_cost int, stock_quantity int, sold_count int, active tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_bookings (id bigint primary key, username varchar(80), class_id bigint, status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_posts (id bigint primary key, username varchar(80), author varchar(120), category varchar(80), content text, likes int, comments int, pinned tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_teams (id bigint primary key, title varchar(180), creator varchar(80), current_members int, max_members int, joined tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_points (username varchar(80) primary key, available_points int) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_exchanges (id bigint primary key, username varchar(80), product_name varchar(180), points_cost int, status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_facilities (id bigint primary key, name varchar(180), name_en varchar(180), available tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_facility_bookings (id bigint primary key, facility_id bigint, facility_name varchar(180), coach_username varchar(80), booking_date varchar(40), time_slot varchar(60), status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_complaints (id bigint primary key, username varchar(80), user_name varchar(120), coach_name varchar(120), title varchar(180), status varchar(40), raw_json longtext) engine=InnoDB default charset=utf8mb4");
        jdbcTemplate.execute("create table if not exists fitlife_reviews (id bigint primary key, coach_name varchar(120), user_name varchar(120), rating int, class_name varchar(180), responded tinyint(1), raw_json longtext) engine=InnoDB default charset=utf8mb4");
    }

    public synchronized void saveToMysqlQuietly() {
        try {
            if (jdbcTemplate == null || !mysqlAvailable) return;
            ensureMysqlSchema();
            String json = objectMapper.writeValueAsString(snapshot());
            jdbcTemplate.update("replace into fitlife_state(state_key, state_json) values(?, ?)", STATE_KEY, json);
            syncReadableMysqlTables();
        } catch (Exception ex) {
            System.err.println("[FitLife] MySQL state save failed: " + ex.getMessage());
        }
    }

    public boolean isMysqlAvailable() {
        return mysqlAvailable;
    }


    private String jsonOf(Map<String, Object> row) {
        try { return objectMapper.writeValueAsString(row); } catch (Exception ex) { return "{}"; }
    }

    private void clearReadableTable(String name) {
        try { jdbcTemplate.execute("delete from " + name); } catch (Exception ignored) {}
    }

    private synchronized void syncReadableMysqlTables() {
        if (jdbcTemplate == null || !mysqlAvailable) return;
        try {
            clearReadableTable("fitlife_users");
            for (Map<String, Object> u : users) jdbcTemplate.update("insert into fitlife_users(id,username,name,email,phone,role,active,raw_json) values(?,?,?,?,?,?,?,?)",
                    asLong(u.get("id")), str(u.get("username"), ""), str(u.get("name"), ""), str(u.get("email"), ""), str(u.get("phone"), ""), str(u.get("role"), "USER"), !Boolean.FALSE.equals(u.get("active")), jsonOf(u));

            clearReadableTable("fitlife_coaches");
            for (Map<String, Object> c : coaches) jdbcTemplate.update("insert into fitlife_coaches(id,username,name,email,specialty,raw_json) values(?,?,?,?,?,?)",
                    asLong(c.get("id")), str(c.get("username"), ""), str(c.get("name"), ""), str(c.get("email"), ""), str(c.get("specialty"), ""), jsonOf(c));

            clearReadableTable("fitlife_memberships");
            for (Map.Entry<String, Map<String, Object>> e : memberships.entrySet()) jdbcTemplate.update("insert into fitlife_memberships(username,membership_type,end_date,status,raw_json) values(?,?,?,?,?)",
                    e.getKey(), str(e.getValue().get("membershipType"), ""), str(e.getValue().get("endDate"), ""), str(e.getValue().get("status"), ""), jsonOf(e.getValue()));

            clearReadableTable("fitlife_classes");
            for (Map<String, Object> c : classes) jdbcTemplate.update("insert into fitlife_classes(id,name,name_en,coach_name,type,start_time,end_time,capacity,booked_count,status,raw_json) values(?,?,?,?,?,?,?,?,?,?,?)",
                    asLong(c.get("id")), str(c.get("name"), ""), str(c.get("nameEn"), ""), str(c.get("coachName"), ""), str(c.get("type"), "GROUP"), str(c.get("startTime"), ""), str(c.get("endTime"), ""), asInt(c.get("capacity"), 0), asInt(c.get("bookedCount"), 0), str(c.get("status"), "ACTIVE"), jsonOf(c));

            clearReadableTable("fitlife_products");
            for (Map<String, Object> p : products) jdbcTemplate.update("insert into fitlife_products(id,name,name_en,category,price,stock_quantity,active,raw_json) values(?,?,?,?,?,?,?,?)",
                    asLong(p.get("id")), str(p.get("name"), ""), str(p.get("nameEn"), ""), str(p.get("category"), ""), String.valueOf(p.get("price") == null ? "0" : p.get("price")), asInt(p.get("stockQuantity"), 0), !Boolean.FALSE.equals(p.get("active")), jsonOf(p));

            clearReadableTable("fitlife_point_products");
            for (Map<String, Object> p : pointProducts) jdbcTemplate.update("insert into fitlife_point_products(id,name,name_en,category,points_cost,stock_quantity,sold_count,active,raw_json) values(?,?,?,?,?,?,?,?,?)",
                    asLong(p.get("id")), str(p.get("name"), ""), str(p.get("nameEn"), ""), str(p.get("category"), ""), asInt(p.get("pointsCost"), 0), asInt(p.get("stockQuantity"), asInt(p.get("stock"), 0)), asInt(p.get("soldCount"), 0), !Boolean.FALSE.equals(p.get("active")), jsonOf(p));

            clearReadableTable("fitlife_bookings");
            for (Map<String, Object> b : bookings) jdbcTemplate.update("insert into fitlife_bookings(id,username,class_id,status,raw_json) values(?,?,?,?,?)",
                    asLong(b.get("id")), str(b.get("username"), ""), asLong(b.get("classId")), str(b.get("status"), "CONFIRMED"), jsonOf(b));

            clearReadableTable("fitlife_posts");
            for (Map<String, Object> p : posts) jdbcTemplate.update("insert into fitlife_posts(id,username,author,category,content,likes,comments,pinned,raw_json) values(?,?,?,?,?,?,?,?,?)",
                    asLong(p.get("id")), str(p.get("username"), ""), str(p.get("author"), ""), str(p.get("category"), ""), str(p.get("content"), ""), asInt(p.get("likes"), 0), asInt(p.get("comments"), 0), Boolean.TRUE.equals(p.get("pinned")), jsonOf(p));

            clearReadableTable("fitlife_teams");
            for (Map<String, Object> t : teams) jdbcTemplate.update("insert into fitlife_teams(id,title,creator,current_members,max_members,joined,raw_json) values(?,?,?,?,?,?,?)",
                    asLong(t.get("id")), str(t.get("title"), ""), str(t.get("creator"), ""), asInt(t.get("currentMembers"), asInt(t.get("members"), 0)), asInt(t.get("maxMembers"), asInt(t.get("capacity"), 0)), Boolean.TRUE.equals(t.get("joined")), jsonOf(t));

            clearReadableTable("fitlife_points");
            for (Map.Entry<String, Integer> e : points.entrySet()) jdbcTemplate.update("insert into fitlife_points(username,available_points) values(?,?)", e.getKey(), e.getValue());

            clearReadableTable("fitlife_exchanges");
            for (Map<String, Object> e : exchanges) jdbcTemplate.update("insert into fitlife_exchanges(id,username,product_name,points_cost,status,raw_json) values(?,?,?,?,?,?)",
                    asLong(e.get("id")), str(e.get("username"), ""), str(e.get("productName"), ""), asInt(e.get("pointsCost"), 0), str(e.get("status"), "COMPLETED"), jsonOf(e));

            clearReadableTable("fitlife_facilities");
            for (Map<String, Object> f : facilities) jdbcTemplate.update("insert into fitlife_facilities(id,name,name_en,available,raw_json) values(?,?,?,?,?)",
                    asLong(f.get("id")), str(f.get("name"), ""), str(f.get("nameEn"), ""), !Boolean.FALSE.equals(f.get("available")), jsonOf(f));

            clearReadableTable("fitlife_facility_bookings");
            for (Map<String, Object> b : facilityBookings) jdbcTemplate.update("insert into fitlife_facility_bookings(id,facility_id,facility_name,coach_username,booking_date,time_slot,status,raw_json) values(?,?,?,?,?,?,?,?)",
                    asLong(b.get("id")), asLong(b.get("facilityId")), str(b.get("facilityName"), ""), str(b.get("coachUsername"), ""), str(b.get("date"), ""), str(b.get("timeSlot"), ""), str(b.get("status"), "RESERVED"), jsonOf(b));

            clearReadableTable("fitlife_complaints");
            for (Map<String, Object> c : complaints) jdbcTemplate.update("insert into fitlife_complaints(id,username,user_name,coach_name,title,status,raw_json) values(?,?,?,?,?,?,?)",
                    asLong(c.get("id")), str(c.get("username"), ""), str(c.get("userName"), ""), str(c.get("coachName"), ""), str(c.get("title"), ""), str(c.get("status"), "PENDING"), jsonOf(c));

            clearReadableTable("fitlife_reviews");
            for (Map<String, Object> r : reviews) jdbcTemplate.update("insert into fitlife_reviews(id,coach_name,user_name,rating,class_name,responded,raw_json) values(?,?,?,?,?,?,?)",
                    asLong(r.get("id")), str(r.get("coachName"), ""), str(r.get("userName"), ""), asInt(r.get("rating"), 0), str(r.get("className"), ""), Boolean.TRUE.equals(r.get("responded")), jsonOf(r));
        } catch (Exception ex) {
            System.err.println("[FitLife] readable MySQL table sync failed: " + ex.getMessage());
        }
    }

    private Map<String, Object> snapshot() {
        Map<String, Object> s = new LinkedHashMap<String, Object>();
        s.put("idGen", idGen.get());
        s.put("users", new ArrayList<Map<String, Object>>(users));
        s.put("coaches", new ArrayList<Map<String, Object>>(coaches));
        s.put("classes", new ArrayList<Map<String, Object>>(classes));
        s.put("products", new ArrayList<Map<String, Object>>(products));
        s.put("pointProducts", new ArrayList<Map<String, Object>>(pointProducts));
        s.put("posts", new ArrayList<Map<String, Object>>(posts));
        s.put("postComments", stringifyLongKeys(postComments));
        s.put("teams", new ArrayList<Map<String, Object>>(teams));
        s.put("ads", new ArrayList<Map<String, Object>>(ads));
        s.put("complaints", new ArrayList<Map<String, Object>>(complaints));
        s.put("reviews", new ArrayList<Map<String, Object>>(reviews));
        s.put("reports", new ArrayList<Map<String, Object>>(reports));
        s.put("bookings", new ArrayList<Map<String, Object>>(bookings));
        s.put("exchanges", new ArrayList<Map<String, Object>>(exchanges));
        s.put("facilities", new ArrayList<Map<String, Object>>(facilities));
        s.put("facilityBookings", new ArrayList<Map<String, Object>>(facilityBookings));
        s.put("points", new LinkedHashMap<String, Integer>(points));
        Map<String, List<String>> dateMap = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, Set<String>> entry : checkinDates.entrySet()) dateMap.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
        s.put("checkinDates", dateMap);
        s.put("memberships", new LinkedHashMap<String, Map<String, Object>>(memberships));
        return s;
    }

    private Map<String, Object> stringifyLongKeys(Map<Long, List<Map<String, Object>>> src) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        for (Map.Entry<Long, List<Map<String, Object>>> entry : src.entrySet()) out.put(String.valueOf(entry.getKey()), entry.getValue());
        return out;
    }

    @SuppressWarnings("unchecked")
    private void applyState(Map<String, Object> state) {
        users.clear(); users.addAll(listOfMaps(state.get("users")));
        coaches.clear(); coaches.addAll(listOfMaps(state.get("coaches")));
        classes.clear(); classes.addAll(listOfMaps(state.get("classes")));
        products.clear(); products.addAll(listOfMaps(state.get("products")));
        pointProducts.clear(); pointProducts.addAll(listOfMaps(state.get("pointProducts")));
        posts.clear(); posts.addAll(listOfMaps(state.get("posts")));
        teams.clear(); teams.addAll(listOfMaps(state.get("teams")));
        ads.clear(); ads.addAll(listOfMaps(state.get("ads")));
        complaints.clear(); complaints.addAll(listOfMaps(state.get("complaints")));
        reviews.clear(); reviews.addAll(listOfMaps(state.get("reviews")));
        reports.clear(); reports.addAll(listOfMaps(state.get("reports")));
        bookings.clear(); bookings.addAll(listOfMaps(state.get("bookings")));
        exchanges.clear(); exchanges.addAll(listOfMaps(state.get("exchanges")));
        facilities.clear(); facilities.addAll(listOfMaps(state.get("facilities")));
        facilityBookings.clear(); facilityBookings.addAll(listOfMaps(state.get("facilityBookings")));
        points.clear();
        Object pointObj = state.get("points");
        if (pointObj instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) pointObj;
            for (Map.Entry<String, Object> e : raw.entrySet()) points.put(e.getKey(), asInt(e.getValue(), 0));
        }
        checkinDates.clear();
        Object datesObj = state.get("checkinDates");
        if (datesObj instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) datesObj;
            for (Map.Entry<String, Object> e : raw.entrySet()) checkinDates.put(e.getKey(), new LinkedHashSet<String>(stringList(e.getValue())));
        }
        memberships.clear();
        Object memObj = state.get("memberships");
        if (memObj instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) memObj;
            for (Map.Entry<String, Object> e : raw.entrySet()) if (e.getValue() instanceof Map) memberships.put(e.getKey(), new LinkedHashMap<String, Object>((Map<String, Object>) e.getValue()));
        }
        postComments.clear();
        Object pcObj = state.get("postComments");
        if (pcObj instanceof Map) {
            Map<String, Object> raw = (Map<String, Object>) pcObj;
            for (Map.Entry<String, Object> e : raw.entrySet()) postComments.put(Long.valueOf(e.getKey()), listOfMaps(e.getValue()));
        }
        Long loadedId = asLong(state.get("idGen"));
        idGen.set(loadedId == null ? 1000L : loadedId);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object obj) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (!(obj instanceof List)) return out;
        for (Object item : (List<Object>) obj) if (item instanceof Map) out.add(new LinkedHashMap<String, Object>((Map<String, Object>) item));
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object obj) {
        List<String> out = new ArrayList<String>();
        if (!(obj instanceof List)) return out;
        for (Object item : (List<Object>) obj) out.add(String.valueOf(item));
        return out;
    }

    public long nextId() {
        return idGen.incrementAndGet();
    }

    public Map<String, Object> m(Object... pairs) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < pairs.length; i += 2) out.put(String.valueOf(pairs[i]), pairs[i + 1]);
        return out;
    }

    public Map<String, Object> ok(Object data) {
        return m("success", true, "message", "OK", "data", data);
    }

    public Map<String, Object> fail(String message) {
        return m("success", false, "message", message, "data", null);
    }

    public String token(String role, String username) {
        return role + ":" + username + ":cleanroom";
    }

    public String[] readToken(String header) {
        if (header == null || header.trim().isEmpty()) return new String[]{"user", "fitness_pro"};
        String value = header.replace("Bearer", "").trim();
        String[] parts = value.split(":");
        if (parts.length >= 2) return new String[]{parts[0], parts[1]};
        return new String[]{"user", "fitness_pro"};
    }

    public Map<String, Object> userByUsername(String username) {
        synchronized (users) {
            for (Map<String, Object> u : users) if (username.equals(u.get("username"))) return u;
        }
        return null;
    }

    public Map<String, Object> coachByUsername(String username) {
        synchronized (coaches) {
            for (Map<String, Object> c : coaches) if (username.equals(c.get("username"))) return c;
        }
        return null;
    }

    public Map<String, Object> coachById(Long id) {
        synchronized (coaches) {
            for (Map<String, Object> c : coaches) if (Objects.equals(asLong(c.get("id")), id)) return c;
        }
        return null;
    }

    public Map<String, Object> classById(Long id) {
        synchronized (classes) {
            for (Map<String, Object> c : classes) if (Objects.equals(asLong(c.get("id")), id)) return c;
        }
        return null;
    }

    public Long asLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(String.valueOf(value)); } catch (Exception e) { return null; }
    }

    public int asInt(Object value, int fallback) {
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }

    public String str(Object value, String fallback) {
        String s = value == null ? null : String.valueOf(value);
        return s == null || s.trim().isEmpty() ? fallback : s;
    }

    private void seedUsers() {
        String[] avatarEmoji = {"💪", "🏋️", "🧘", "🏃", "💃", "🔥", "✨", "🏅", "🏊", "🧎", "🥊", "⚡"};
        String[][] data = {
                {"fitness_pro", "健身狂人", "fitness.pro@fitlife.demo", "13810000000", "male", "1996-03-12", "FitLife金卡会员，偏好力量训练"},
                {"strength_member", "力量小子", "strength.member@fitlife.demo", "13810000001", "male", "1998-07-08", "增肌阶段，关注深蹲和硬拉动作"},
                {"yoga_member", "瑜伽达人", "yoga.member@fitlife.demo", "13810000002", "female", "1997-11-20", "喜欢瑜伽和普拉提，重视柔韧性"},
                {"running_king", "跑步爱好者", "running.king@fitlife.demo", "13810000003", "male", "1995-05-18", "备战半程马拉松，关注心肺训练"},
                {"dance_queen", "动感少女", "dance.queen@fitlife.demo", "13810000004", "female", "1999-09-09", "喜欢舞蹈、有氧和团体课程"},
                {"power_lifter", "举重王者", "power.lifter@fitlife.demo", "13810000005", "male", "1994-12-01", "大重量训练者，需要恢复建议"},
                {"fit_newbie", "健身新星", "fit.newbie@fitlife.demo", "13810000006", "female", "2001-01-22", "新会员，正在适应基础训练"},
                {"exercise_addict", "运动达人", "exercise.addict@fitlife.demo", "13810000007", "male", "1993-10-15", "几乎每天训练，关注积分和排行榜"},
                {"swim_master", "泳池飞鱼", "swim.master@fitlife.demo", "13810000008", "male", "1996-08-27", "喜欢游泳课程和肩背训练"},
                {"pilates_star", "普拉提星", "pilates.star@fitlife.demo", "13810000009", "female", "1998-04-30", "关注核心稳定和体态改善"},
                {"boxing_tiger", "搏击老虎", "boxing.tiger@fitlife.demo", "13810000010", "male", "1997-02-17", "喜欢搏击、HIIT和爆发力训练"},
                {"cardio_hero", "有氧达人", "cardio.hero@fitlife.demo", "13810000011", "female", "2000-06-06", "偏好有氧操、动感单车和燃脂课程"}
        };
        for (int i = 0; i < data.length; i++) {
            users.add(m("id", i + 1L, "username", data[i][0], "name", data[i][1], "email", data[i][2], "phone", data[i][3],
                    "gender", data[i][4], "birthday", data[i][5], "address", "FitLife社区" + (i + 1) + "号楼",
                    "medicalNotes", data[i][6], "role", "USER", "active", true, "avatar", avatarEmoji[i % avatarEmoji.length]));
            points.put(data[i][0], Math.max(520, 2680 - i * 150));
        }
    }

    private void seedCoaches() {
        String[][] data = {
                {"coach_1", "张教练", "力量训练,增肌,功能性训练", "5", "300", "4.8", "560", "126", "国家级健身教练认证、高级私教证书、功能性训练认证", "👨‍🏫"},
                {"coach_2", "李教练", "瑜伽,普拉提,拉伸放松", "6", "350", "4.9", "640", "168", "瑜伽联盟认证、普拉提垫上认证、运动康复基础证书", "🧘"},
                {"coach_3", "王教练", "减脂,动感单车,体态矫正", "7", "400", "5.0", "720", "205", "高级体能训练师、减脂营养指导、动感单车认证", "🚴"},
                {"coach_4", "赵教练", "散打搏击,爆发力,HIIT", "8", "450", "5.0", "810", "238", "搏击训练认证、HIIT训练认证、青少年体能指导", "🥊"},
                {"coach_5", "陈教练", "游泳,康复训练,肩背改善", "4", "320", "4.7", "380", "96", "游泳教练证、运动康复基础认证、急救证书", "🏊"},
                {"coach_6", "刘教练", "舞蹈燃脂,有氧操,体态塑形", "5", "330", "4.8", "430", "112", "有氧团课认证、舞蹈训练认证、体态塑形认证", "💃"}
        };
        for (int i = 0; i < data.length; i++) {
            coaches.add(m("id", i + 1L, "username", data[i][0], "name", data[i][1], "email", "coach" + (i + 1) + "@fitlife.com",
                    "phone", "1380000000" + (i + 1), "specialty", data[i][2], "experienceYears", Integer.parseInt(data[i][3]),
                    "hourlyRate", Double.parseDouble(data[i][4]), "rating", Double.parseDouble(data[i][5]), "totalClasses", Integer.parseInt(data[i][6]),
                    "totalStudents", Integer.parseInt(data[i][7]), "certifications", data[i][8], "qrCode", String.format("COACH-%06d", i + 1),
                    "avatar", data[i][9], "bio", data[i][1] + "擅长" + data[i][2] + "，可提供团课和一对一训练服务。"));
        }
    }

    private void seedClasses() {
        String[][] templates = {
                {"力量训练", "Strength Training", "C区力量区", "Zone C — Strength", "25", "1"},
                {"瑜伽放松", "Yoga Relaxation", "A区瑜伽室", "Yoga Studio", "22", "2"},
                {"动感单车", "Spinning", "B区动感单车房", "Spinning Studio", "25", "3"},
                {"普拉提核心", "Pilates Core", "A区普拉提室", "Pilates Studio", "18", "2"},
                {"搏击燃脂", "Boxing Burn", "D区搏击室", "Zone D — Boxing", "20", "4"},
                {"舞蹈有氧", "Dance Cardio", "E区舞蹈室", "Zone E — Dance", "24", "6"},
                {"游泳基础", "Swimming Basics", "恒温泳池", "Swimming Pool", "16", "5"},
                {"HIIT循环训练", "HIIT Circuit", "F区有氧区", "Zone F — Cardio", "20", "4"}
        };
        LocalDate today = LocalDate.now();
        long id = 1L;
        for (int d = 0; d < 7; d++) {
            int[] hours = {9, 11, 14, 19};
            for (int slot = 0; slot < hours.length; slot++) {
                int idx = (d * hours.length + slot) % templates.length;
                String[] t = templates[idx];
                Map<String, Object> coach = coaches.get(Math.max(0, Math.min(coaches.size() - 1, Integer.parseInt(t[5]) - 1)));
                LocalTime start = LocalTime.of(hours[slot], slot == 3 ? 30 : 0);
                int capacity = Integer.parseInt(t[4]);
                int booked = Math.min(capacity - 1, 6 + ((d + slot) * 3) % 12);
                classes.add(m("id", id++, "name", t[0], "nameEn", t[1], "location", t[2], "locationEn", t[3], "capacity", capacity,
                        "bookedCount", booked, "startTime", today.plusDays(d).atTime(start).toString(), "endTime", today.plusDays(d).atTime(start.plusMinutes(60)).toString(),
                        "status", "ACTIVE", "coachId", coach.get("id"), "coachName", coach.get("name"), "instructor", coach.get("name"), "type", "GROUP", "duration", "60分钟"));
            }
        }
        String[][] privateTemplates = {
                {"一对一力量评估", "Private Strength Assessment", "私教训练室1", "Private Training Room 1", "1", "15"},
                {"普拉提体态改善", "Private Pilates Posture", "私教训练室2", "Private Training Room 2", "2", "16"},
                {"燃脂训练计划", "Private Fat Loss Plan", "私教训练室3", "Private Training Room 3", "3", "17"},
                {"搏击私教体验", "Private Boxing Trial", "私教训练室4", "Private Training Room 4", "4", "18"},
                {"游泳技术纠正", "Private Swim Technique", "恒温泳池私教区", "Private Swim Lane", "5", "10"},
                {"舞蹈塑形私教", "Private Dance Shaping", "舞蹈私教区", "Private Dance Zone", "6", "15"},
                {"核心稳定训练", "Private Core Stability", "私教训练室1", "Private Training Room 1", "1", "16"},
                {"拉伸康复私教", "Private Stretch Recovery", "康复训练区", "Recovery Zone", "2", "18"},
                {"单车体能提升", "Private Cycling Conditioning", "B区动感单车房", "Spinning Studio", "3", "12"},
                {"HIIT爆发力训练", "Private HIIT Power", "F区有氧区", "Zone F — Cardio", "4", "20"},
                {"肩背改善训练", "Private Shoulder Back Rehab", "康复训练区", "Recovery Zone", "5", "14"},
                {"新人入门评估", "Private Beginner Assessment", "私教训练室2", "Private Training Room 2", "6", "11"}
        };
        for (int i = 0; i < privateTemplates.length; i++) {
            String[] t = privateTemplates[i];
            Map<String, Object> coach = coaches.get(Math.max(0, Math.min(coaches.size() - 1, Integer.parseInt(t[4]) - 1)));
            LocalDateTime start = today.plusDays(i % 6).atTime(Integer.parseInt(t[5]), 0);
            classes.add(m("id", 101L + i, "name", t[0], "nameEn", t[1], "location", t[2], "locationEn", t[3],
                    "capacity", 1, "bookedCount", i % 3 == 0 ? 1 : 0, "startTime", start.toString(), "endTime", start.plusMinutes(60).toString(),
                    "status", "ACTIVE", "coachId", coach.get("id"), "coachName", coach.get("name"), "instructor", coach.get("name"), "type", "PRIVATE", "duration", "60分钟"));
        }
    }


    private int classCountByKind(boolean privateKind) {
        int count = 0;
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                String type = str(c.get("type"), "").toUpperCase(Locale.ROOT);
                boolean isPrivate = type.contains("PRIVATE") || asInt(c.get("capacity"), 0) <= 1 || str(c.get("location"), "").contains("私教");
                if (isPrivate == privateKind) count++;
            }
        }
        return count;
    }

    private boolean classNameExists(String name) {
        synchronized (classes) {
            for (Map<String, Object> c : classes) if (name.equals(c.get("name")) || name.equals(c.get("nameEn"))) return true;
        }
        return false;
    }

    private void seedDefaultGroupClassesIfMissing() {
        if (coaches.isEmpty()) seedCoaches();
        String[] names = {"力量训练", "瑜伽放松", "动感单车", "普拉提", "搏击", "舞蹈", "游泳", "有氧操"};
        String[] en = {"Strength Training", "Yoga Relaxation", "Spinning", "Pilates", "Boxing", "Dance", "Swimming", "Aerobics"};
        String[] places = {"C区力量区", "A区教室", "B区动感单车房", "A区教室", "D区搏击室", "E区舞蹈室", "游泳池", "F区有氧区"};
        LocalDate today = LocalDate.now();
        for (int i = 0; i < names.length; i++) {
            if (classNameExists(names[i]) || classNameExists(en[i])) continue;
            LocalDate date = i < 3 ? today : today.plusDays(1 + (i % 4));
            LocalTime start = LocalTime.of(9 + (i % 4) * 2, 0);
            Map<String, Object> coach = coaches.get(i % coaches.size());
            classes.add(m("id", nextId(), "name", names[i], "nameEn", en[i], "location", places[i], "locationEn", places[i], "capacity", 25,
                    "bookedCount", 3 + (i % 3), "startTime", date.atTime(start).toString(), "endTime", date.atTime(start.plusHours(1)).toString(),
                    "status", "ACTIVE", "coachId", coach.get("id"), "coachName", coach.get("name"), "instructor", coach.get("name"), "type", "GROUP"));
        }
    }

    private void seedDefaultPrivateClassesIfMissing() {
        if (coaches.isEmpty()) seedCoaches();
        String[] privateNames = {"一对一力量评估", "普拉提私教", "燃脂训练计划", "搏击私教体验"};
        String[] privateEn = {"Private Coaching 1", "Private Coaching 2", "Private Coaching 3", "Private Coaching 4"};
        for (int i = 0; i < privateNames.length && i < coaches.size(); i++) {
            Map<String, Object> coach = coaches.get(i);
            boolean exists = false;
            synchronized (classes) {
                for (Map<String, Object> c : classes) {
                    String type = str(c.get("type"), "").toUpperCase(Locale.ROOT);
                    if ((type.contains("PRIVATE") || asInt(c.get("capacity"), 0) <= 1) && Objects.equals(asLong(c.get("coachId")), asLong(coach.get("id")))) { exists = true; break; }
                }
            }
            if (exists) continue;
            LocalDateTime start = LocalDate.now().plusDays(1 + i).atTime(16, 0).plusHours(i);
            classes.add(m("id", nextId(), "name", privateNames[i], "nameEn", privateEn[i], "location", "私教区-" + coach.get("name"), "locationEn", "Private Zone - " + coach.get("name"),
                    "capacity", 1, "bookedCount", 0, "startTime", start.toString(), "endTime", start.plusMinutes(60).toString(),
                    "status", "ACTIVE", "coachId", coach.get("id"), "coachName", coach.get("name"), "instructor", coach.get("name"), "type", "PRIVATE", "duration", "60分钟"));
        }
    }

    private Map<String, Object> firstCoach() {
        if (coaches.isEmpty()) seedCoaches();
        return coaches.isEmpty() ? null : coaches.get(0);
    }

    private boolean coachIdExists(Long id) {
        return coachById(id) != null;
    }

    private Map<String, Object> coachByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        synchronized (coaches) {
            for (Map<String, Object> coach : coaches) {
                String coachName = str(coach.get("name"), "");
                if (name.equals(coachName) || name.contains(coachName) || coachName.contains(name)) return coach;
            }
        }
        return null;
    }

    private Map<String, Object> inferCoachForClass(Map<String, Object> c, int index) {
        Long currentId = asLong(c.get("coachId"));
        Map<String, Object> coach = coachIdExists(currentId) ? coachById(currentId) : null;
        if (coach != null) return coach;
        coach = coachByName(str(c.get("coachName"), ""));
        if (coach != null) return coach;
        coach = coachByName(str(c.get("instructor"), ""));
        if (coach != null) return coach;
        coach = coachByName(str(c.get("location"), ""));
        if (coach != null) return coach;
        if (coaches.isEmpty()) seedCoaches();
        return coaches.isEmpty() ? null : coaches.get(Math.abs(index) % coaches.size());
    }

    private boolean bookingRecordExists(String username, Long classId) {
        synchronized (bookings) {
            for (Map<String, Object> b : bookings) {
                if (username.equals(str(b.get("username"), "")) && Objects.equals(asLong(b.get("classId")), classId)) return true;
            }
        }
        return false;
    }

    private boolean addBookingIfMissing(String username, Long classId, int hoursAgo) {
        if (username == null || username.trim().isEmpty() || classId == null || classById(classId) == null) return false;
        // Demo bookings are only seed data. If the user has already cancelled a seeded
        // booking, keep that CANCELLED row and never recreate it during ensureOperationalData.
        if (bookingRecordExists(username, classId)) return false;
        bookings.add(m("id", nextId(), "username", username, "classId", classId, "status", "CONFIRMED", "bookingTime", LocalDateTime.now().minusHours(hoursAgo).toString()));
        return true;
    }

    private boolean hasCoachClass(Long coachId, boolean privateKind) {
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                String type = str(c.get("type"), "").toUpperCase(Locale.ROOT);
                boolean isPrivate = type.contains("PRIVATE") || asInt(c.get("capacity"), 0) <= 1 || str(c.get("location"), "").contains("私教");
                if (Objects.equals(asLong(c.get("coachId")), coachId) && isPrivate == privateKind) return true;
            }
        }
        return false;
    }

    private boolean hasCoachClassToday(Long coachId) {
        String today = LocalDate.now().toString();
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                if (!Objects.equals(asLong(c.get("coachId")), coachId)) continue;
                String start = str(c.get("startTime"), "");
                if (start.startsWith(today)) return true;
            }
        }
        return false;
    }

    private boolean addCoachDemoClass(Map<String, Object> coach, String name, String nameEn, String location, String locationEn, LocalDateTime start, int capacity, String type) {
        Long coachId = coach == null ? null : asLong(coach.get("id"));
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                if (name.equals(c.get("name")) && Objects.equals(asLong(c.get("coachId")), coachId)) return false;
            }
            classes.add(m("id", nextId(), "name", name, "nameEn", nameEn, "location", location, "locationEn", locationEn,
                    "capacity", capacity, "bookedCount", 0, "startTime", start.toString(), "endTime", start.plusMinutes(type.equals("PRIVATE") ? 60 : 75).toString(),
                    "status", "ACTIVE", "coachId", coachId, "coachName", coach == null ? "张教练" : coach.get("name"),
                    "instructor", coach == null ? "张教练" : coach.get("name"), "type", type, "duration", type.equals("PRIVATE") ? "60分钟" : "75分钟"));
            return true;
        }
    }

    private boolean normalizeCoachClassRelationsAndBookings() {
        boolean changed = false;
        if (users.isEmpty()) { seedUsers(); changed = true; }
        if (coaches.isEmpty()) { seedCoaches(); changed = true; }
        if (classes.isEmpty()) { seedClasses(); changed = true; }

        int index = 0;
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                if (str(c.get("type"), "").trim().isEmpty()) {
                    boolean looksPrivate = asInt(c.get("capacity"), 0) <= 1 || str(c.get("location"), "").contains("私教") || str(c.get("name"), "").contains("私教");
                    c.put("type", looksPrivate ? "PRIVATE" : "GROUP");
                    changed = true;
                }
                Map<String, Object> coach = inferCoachForClass(c, index++);
                if (coach != null) {
                    Long expectedId = asLong(coach.get("id"));
                    if (!Objects.equals(asLong(c.get("coachId")), expectedId)) { c.put("coachId", expectedId); changed = true; }
                    String expectedName = str(coach.get("name"), "张教练");
                    if (!expectedName.equals(str(c.get("coachName"), ""))) { c.put("coachName", expectedName); changed = true; }
                    if (!expectedName.equals(str(c.get("instructor"), ""))) { c.put("instructor", expectedName); changed = true; }
                }
                if (str(c.get("status"), "").trim().isEmpty() || "upcoming".equalsIgnoreCase(str(c.get("status"), ""))) { c.put("status", "ACTIVE"); changed = true; }
                if (str(c.get("startTime"), "").trim().isEmpty()) {
                    LocalDateTime start = LocalDate.now().plusDays(index % 3).atTime(9 + (index % 5), 0);
                    c.put("startTime", start.toString());
                    c.put("endTime", start.plusHours(1).toString());
                    changed = true;
                } else if (str(c.get("endTime"), "").trim().isEmpty()) {
                    try { c.put("endTime", LocalDateTime.parse(str(c.get("startTime"), "")).plusHours(1).toString()); changed = true; } catch (Exception ignored) {}
                }
                if (!c.containsKey("capacity")) { c.put("capacity", "PRIVATE".equals(str(c.get("type"), "")) ? 1 : 20); changed = true; }
            }
        }

        Map<String, Object> coachOne = coachByUsername("coach_1");
        if (coachOne == null) coachOne = firstCoach();
        Long coachOneId = coachOne == null ? 1L : asLong(coachOne.get("id"));
        LocalDate today = LocalDate.now();
        if (!hasCoachClass(coachOneId, false)) {
            changed |= addCoachDemoClass(coachOne, "力量训练", "Strength Training", "C区力量区", "Zone C — Strength", today.atTime(10, 0), 25, "GROUP");
            changed |= addCoachDemoClass(coachOne, "功能训练小组课", "Functional Training", "Training Zone A", "Training Zone A", today.plusDays(1).atTime(16, 0), 18, "GROUP");
        }
        if (!hasCoachClass(coachOneId, true)) {
            changed |= addCoachDemoClass(coachOne, "一对一力量评估", "Private Strength Assessment", "私教训练室1", "Private Training Room 1", today.plusDays(1).atTime(14, 0), 1, "PRIVATE");
        }
        if (!hasCoachClassToday(coachOneId)) {
            changed |= addCoachDemoClass(coachOne, "今日力量循环课", "Today Strength Circuit", "C区力量区", "Zone C — Strength", today.atTime(15, 0), 20, "GROUP");
        }

        List<Long> coachOneClassIds = new ArrayList<Long>();
        synchronized (classes) {
            for (Map<String, Object> c : classes) if (Objects.equals(asLong(c.get("coachId")), coachOneId)) coachOneClassIds.add(asLong(c.get("id")));
        }
        String[] demoStudents = {"fitness_pro", "strength_member", "yoga_member", "running_king", "fit_newbie"};
        int h = 2;
        for (int i = 0; i < coachOneClassIds.size() && i < demoStudents.length; i++) changed |= addBookingIfMissing(demoStudents[i], coachOneClassIds.get(i), h++);
        if (!coachOneClassIds.isEmpty()) {
            changed |= addBookingIfMissing("strength_member", coachOneClassIds.get(0), 5);
            changed |= addBookingIfMissing("yoga_member", coachOneClassIds.get(0), 6);
            changed |= addBookingIfMissing("fitness_pro", coachOneClassIds.get(0), 7);
        }

        Map<Long, Integer> confirmedCounts = new LinkedHashMap<Long, Integer>();
        synchronized (bookings) {
            for (Map<String, Object> b : bookings) {
                if (!"CONFIRMED".equalsIgnoreCase(str(b.get("status"), "CONFIRMED"))) continue;
                Long classId = asLong(b.get("classId"));
                if (classId != null) confirmedCounts.put(classId, confirmedCounts.getOrDefault(classId, 0) + 1);
            }
        }
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                Long classId = asLong(c.get("id"));
                int oldCount = asInt(c.get("bookedCount"), 0);
                int newCount = Math.max(oldCount, confirmedCounts.getOrDefault(classId, 0));
                if (newCount != oldCount) { c.put("bookedCount", newCount); changed = true; }
            }
        }
        synchronized (users) {
            for (Map<String, Object> u : users) {
                if (!u.containsKey("active")) { u.put("active", true); changed = true; }
            }
        }
        return changed;
    }

    public synchronized void ensureOperationalData() {
        boolean changed = repairMissingSeedData();
        if (users.isEmpty()) { seedUsers(); changed = true; }
        if (coaches.isEmpty()) { seedCoaches(); changed = true; }
        if (classCountByKind(false) == 0) { seedDefaultGroupClassesIfMissing(); changed = true; }
        if (classCountByKind(true) == 0) { seedDefaultPrivateClassesIfMissing(); changed = true; }
        changed |= normalizeCoachClassRelationsAndBookings();
        if (pointProducts.isEmpty() || pointProducts.size() < 6) { products.clear(); pointProducts.clear(); seedProducts(); changed = true; }
        if (posts.isEmpty() || teams.isEmpty()) { if (posts.isEmpty() && teams.isEmpty()) seedSocial(); else { posts.clear(); teams.clear(); ads.clear(); reports.clear(); postComments.clear(); seedSocial(); } changed = true; }
        if (complaints.isEmpty() && reviews.isEmpty()) { seedMailbox(); changed = true; }
        if (memberships.isEmpty() && !users.isEmpty()) { seedMemberships(); changed = true; }
        if (points.isEmpty() && !users.isEmpty()) {
            int value = 1280;
            for (Map<String, Object> u : users) { points.put(str(u.get("username"), "user"), value); value = Math.max(100, value - 80); }
            changed = true;
        }
        long max = 100;
        for (List<Map<String, Object>> list : Arrays.asList(users, coaches, classes, products, pointProducts, posts, teams, ads, complaints, reviews, reports, bookings, exchanges, facilities, facilityBookings)) {
            for (Map<String, Object> row : list) { Long id = asLong(row.get("id")); if (id != null) max = Math.max(max, id); }
        }
        if (idGen.get() <= max) { idGen.set(max + 10); changed = true; }
        if (changed) saveToMysqlQuietly();
    }

    private void seedBookings() {
        Object[][] rows = {
                {"fitness_pro", 1L, 30}, {"strength_member", 1L, 28}, {"yoga_member", 2L, 26}, {"running_king", 3L, 24},
                {"dance_queen", 4L, 22}, {"power_lifter", 5L, 20}, {"fit_newbie", 6L, 18}, {"exercise_addict", 7L, 16},
                {"swim_master", 8L, 14}, {"pilates_star", 9L, 12}, {"boxing_tiger", 10L, 10}, {"cardio_hero", 11L, 8},
                {"fitness_pro", 12L, 7}, {"strength_member", 13L, 6}, {"yoga_member", 14L, 5}, {"running_king", 15L, 4},
                {"dance_queen", 16L, 3}, {"power_lifter", 17L, 2}, {"fit_newbie", 18L, 1}, {"exercise_addict", 19L, 1},
                {"fitness_pro", 101L, 5}, {"yoga_member", 102L, 6}, {"running_king", 103L, 7}, {"boxing_tiger", 104L, 8},
                {"swim_master", 105L, 9}, {"pilates_star", 106L, 10}, {"strength_member", 107L, 11}, {"fit_newbie", 112L, 12}
        };
        long id = 1L;
        for (int i = 0; i < rows.length; i++) {
            bookings.add(m("id", id++, "username", rows[i][0], "classId", rows[i][1], "status", "CONFIRMED", "bookingTime", LocalDateTime.now().minusHours(((Number) rows[i][2]).longValue()).toString()));
        }
        bookings.add(m("id", id++, "username", "fitness_pro", "classId", 2L, "status", "CANCELLED", "bookingTime", LocalDateTime.now().minusDays(2).toString(), "cancelTime", LocalDateTime.now().minusDays(1).toString()));
    }


    private void seedFacilities() {
        String[][] data = {
                {"Training Zone A", "Training Zone A", "Main strength training zone", "Main strength training zone", "Barbells, dumbbells, cable machine", "Barbells, dumbbells, cable machine"},
                {"Training Zone B", "Training Zone B", "Cardio and functional training zone", "Cardio and functional training zone", "Treadmills, ellipticals, rowing machines", "Treadmills, ellipticals, rowing machines"},
                {"Yoga Studio", "Yoga Studio", "Yoga room for group and private sessions", "Yoga room for group and private sessions", "Yoga mats, yoga balls, blocks", "Yoga mats, yoga balls, blocks"},
                {"Group Class Room", "Group Class Room", "Studio for group classes", "Studio for group classes", "Audio system, mirrors, step platforms", "Audio system, mirrors, step platforms"},
                {"Spinning Studio", "Spinning Studio", "Dedicated spinning studio", "Dedicated spinning studio", "Spin bikes, audio system, fans", "Spin bikes, audio system, fans"},
                {"Zone C — Strength", "Zone C — Strength", "Strength zone for barbells and heavy training", "Strength zone for barbells and heavy training", "Power racks, barbells, plates", "Power racks, barbells, plates"},
                {"Classroom A", "Classroom A", "Multi-purpose classroom for small group sessions", "Multi-purpose classroom for small group sessions", "Projector, mats, light training tools", "Projector, mats, light training tools"},
                {"Zone B — Spin Studio", "Zone B — Spin Studio", "Spin room for cycling classes", "Spin room for cycling classes", "Spin bikes, audio system, fans", "Spin bikes, audio system, fans"},
                {"Zone D — Boxing", "Zone D — Boxing", "Boxing and combat training room", "Boxing and combat training room", "Punching bags, gloves, pads", "Punching bags, gloves, pads"},
                {"Zone E — Dance", "Zone E — Dance", "Dance and stretching studio", "Dance and stretching studio", "Mirrors, ballet barre, audio system", "Mirrors, ballet barre, audio system"},
                {"Swimming Pool", "Swimming Pool", "Indoor swimming pool", "Indoor swimming pool", "Lane ropes, kickboards, rescue equipment", "Lane ropes, kickboards, rescue equipment"},
                {"Zone F — Cardio", "Zone F — Cardio", "Cardio zone for aerobic classes", "Cardio zone for aerobic classes", "Treadmills, ellipticals, rowing machines", "Treadmills, ellipticals, rowing machines"},
                {"Recovery Room", "Recovery Room", "Stretching and rehabilitation room", "Stretching and rehabilitation room", "Foam rollers, massage guns, therapy bands", "Foam rollers, massage guns, therapy bands"},
                {"Private Training Room 1", "Private Training Room 1", "Quiet room for one-to-one coaching", "Quiet room for one-to-one coaching", "Adjustable bench, cable trainer, mats", "Adjustable bench, cable trainer, mats"},
                {"Private Training Room 2", "Private Training Room 2", "Room for assessment and beginner training", "Room for assessment and beginner training", "Body composition scale, mats, dumbbells", "Body composition scale, mats, dumbbells"}
        };
        for (int i = 0; i < data.length; i++) {
            facilities.add(m("id", i + 1L, "name", data[i][0], "nameEn", data[i][1], "description", data[i][2], "descriptionEn", data[i][3],
                    "equipment", data[i][4], "equipmentEn", data[i][5], "available", true,
                    "timeSlots", Arrays.asList("09:00-11:00", "11:00-13:00", "14:00-16:00", "16:00-18:00", "19:00-21:00")));
        }
        LocalDate today = LocalDate.now();
        facilityBookings.add(m("id", 1L, "facilityId", 6L, "facilityName", "Zone C — Strength", "facilityNameEn", "Zone C — Strength", "coachUsername", "coach_1", "date", today.toString(), "startTime", "09:00", "endTime", "11:00", "timeSlot", "09:00-11:00", "status", "RESERVED", "usedByClass", true));
        facilityBookings.add(m("id", 2L, "facilityId", 3L, "facilityName", "Yoga Studio", "facilityNameEn", "Yoga Studio", "coachUsername", "coach_2", "date", today.toString(), "startTime", "11:00", "endTime", "13:00", "timeSlot", "11:00-13:00", "status", "RESERVED", "usedByClass", true));
        facilityBookings.add(m("id", 3L, "facilityId", 5L, "facilityName", "Spinning Studio", "facilityNameEn", "Spinning Studio", "coachUsername", "coach_3", "date", today.plusDays(1).toString(), "startTime", "19:00", "endTime", "21:00", "timeSlot", "19:00-21:00", "status", "RESERVED", "usedByClass", true));
        facilityBookings.add(m("id", 4L, "facilityId", 9L, "facilityName", "Zone D — Boxing", "facilityNameEn", "Zone D — Boxing", "coachUsername", "coach_4", "date", today.plusDays(1).toString(), "startTime", "16:00", "endTime", "18:00", "timeSlot", "16:00-18:00", "status", "RESERVED", "usedByClass", true));
        facilityBookings.add(m("id", 5L, "facilityId", 11L, "facilityName", "Swimming Pool", "facilityNameEn", "Swimming Pool", "coachUsername", "coach_5", "date", today.plusDays(2).toString(), "startTime", "14:00", "endTime", "16:00", "timeSlot", "14:00-16:00", "status", "RESERVED", "usedByClass", false));
        facilityBookings.add(m("id", 6L, "facilityId", 10L, "facilityName", "Zone E — Dance", "facilityNameEn", "Zone E — Dance", "coachUsername", "coach_6", "date", today.plusDays(2).toString(), "startTime", "19:00", "endTime", "21:00", "timeSlot", "19:00-21:00", "status", "RESERVED", "usedByClass", false));
    }

    private void seedCheckins() {
        LocalDate today = LocalDate.now();
        for (int u = 0; u < users.size(); u++) {
            Map<String, Object> user = users.get(u);
            String username = String.valueOf(user.get("username"));
            Set<String> dates = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            int step = (u % 3) + 1;
            int total = 12 + (u % 5) * 3;
            for (int i = 1; i <= total; i++) dates.add(today.minusDays((long) i * step).toString());
            if (u % 2 == 0) dates.add(today.minusDays(1).toString());
            checkinDates.put(username, dates);
        }
    }


    private Object rowValue(Object[] row, int index, Object fallback) {
        return row != null && index >= 0 && index < row.length ? row[index] : fallback;
    }

    private void seedProducts() {
        String[][] legacy = {
                {"专业跑步机", "Premium Treadmill", "高端商用跑步机，带多种运动模式和心率监测功能", "Commercial treadmill with training modes and heart-rate monitoring", "健身器材", "3999.99", "10", "treadmill"},
                {"哑铃套装", "Adjustable Dumbbell Set", "20-50kg可调节哑铃套装，适合家庭健身", "20-50 kg adjustable dumbbell set for home workouts", "健身器材", "1299.00", "20", "dumbbell"},
                {"运动紧身衣", "Compression Training Shirt", "高弹性运动紧身衣，透气排汗，适合各种运动", "Elastic, breathable and sweat-wicking training shirt", "运动服装", "299.00", "30", "shirt"},
                {"蛋白粉", "Protein Powder", "高蛋白营养补剂，辅助增肌和恢复", "High-protein supplement for muscle gain and recovery", "营养补剂", "499.00", "15", "protein"},
                {"智能手环", "Smart Fitness Band", "多功能智能手环，监测心率、步数、睡眠等", "Fitness band for heart rate, steps and sleep tracking", "智能设备", "599.00", "12", "band"},
                {"运动水壶", "Sports Bottle", "大容量运动水壶，保温保冷，适合户外运动", "Large insulated sports bottle for outdoor training", "运动配件", "89.00", "40", "bottle"},
                {"筋膜枪", "Massage Gun", "训练后放松恢复使用", "Recovery massage gun after training", "恢复器材", "699.00", "18", "support"},
                {"拳击手套", "Boxing Gloves", "适合搏击课程的训练手套", "Training gloves for boxing classes", "运动装备", "239.00", "35", "boxing"},
                {"泳镜泳帽套装", "Swim Goggles Set", "泳池训练常用套装", "Goggles and cap set for swimming", "游泳装备", "129.00", "50", "swim"},
                {"瑜伽砖套装", "Yoga Blocks Set", "瑜伽拉伸辅助工具", "Yoga stretching support tools", "瑜伽装备", "99.00", "60", "yoga"}
        };
        for (int i = 0; i < legacy.length; i++) {
            products.add(m("id", i + 1L, "name", legacy[i][0], "nameEn", legacy[i][1], "description", legacy[i][2], "descriptionEn", legacy[i][3], "category", legacy[i][4],
                    "price", Double.parseDouble(legacy[i][5]), "stockQuantity", Integer.parseInt(legacy[i][6]), "imageKey", legacy[i][7], "active", true));
        }
        Object[][] pp = {
                {"5元无门槛券", "¥5 No-threshold Coupon", "满0元可用，直减5元", "Use with no minimum spend, save ¥5", 10, "优惠券", "COUPON", "coupon", 999, false},
                {"10元实付券", "¥10 Payment Coupon", "实付满100元可用", "Save ¥10 when spending over ¥100", 30, "优惠券", "COUPON", "ticket", 888, false},
                {"15元满减券", "¥15 Discount Coupon", "实付满200元可用", "Save ¥15 when spending over ¥200", 50, "优惠券", "COUPON", "discount", 777, false},
                {"30元满减券", "¥30 Discount Coupon", "实付满300元可用", "Save ¥30 when spending over ¥300", 80, "优惠券", "COUPON", "gift", 555, false},
                {"50元私教抵扣券", "¥50 PT Coupon", "购买私教套餐可抵扣50元", "Save ¥50 for personal training packages", 120, "优惠券", "COUPON", "service", 300, false},
                {"运动水壶", "Sports Bottle", "大容量运动水壶，便携耐用", "Large and durable bottle for training", 200, "运动装备", "PRODUCT", "bottle", 100, false},
                {"瑜伽垫", "Yoga Mat", "加厚防滑瑜伽垫", "Thick non-slip yoga mat", 300, "运动装备", "PRODUCT", "yoga", 80, false},
                {"健身护腕", "Fitness Wrist Wraps", "专业健身护腕", "Professional wrist support for workouts", 150, "运动装备", "PRODUCT", "support", 200, false},
                {"速干T恤", "Quick-dry T-shirt", "透气速干运动T恤", "Breathable quick-dry training shirt", 250, "运动装备", "PRODUCT", "shirt", 120, false},
                {"跳绳", "Speed Rope", "燃脂训练跳绳", "Speed rope for cardio training", 180, "运动装备", "PRODUCT", "fitness", 160, false},
                {"训练毛巾", "Training Towel", "吸汗运动毛巾", "Sweat-absorbing training towel", 120, "运动装备", "PRODUCT", "gift", 220, false},
                {"VIP专属-蛋白粉", "VIP Protein Powder", "高蛋白健身蛋白粉", "High-protein powder for VIP members", 500, "VIP专属", "PRODUCT", "protein", 50, true},
                {"VIP专属-智能手环", "VIP Smart Band", "运动智能手环", "Smart fitness band for VIP members", 800, "VIP专属", "PRODUCT", "band", 30, true},
                {"VIP专属-体测报告", "VIP Body Test Report", "一次专业体测报告服务", "One professional body test report", 650, "VIP专属", "SERVICE", "service", 40, true},
                {"免费私教课", "Free Personal Training Session", "价值200元的一对一私教课1节", "One personal training session worth ¥200", 1000, "会员服务", "SERVICE", "service", 20, false},
                {"免费团课体验", "Free Group Class Trial", "任意团课体验1次", "One trial for any group class", 300, "会员服务", "SERVICE", "trial", 50, false},
                {"会员积分翻倍", "Double Points Pass", "购买后7天内签到积分翻倍", "Double check-in points for 7 days", 400, "会员特权", "PRIVILEGE", "privilege", 100, false},
                {"课程优先预约卡", "Priority Booking Card", "未来7天课程优先预约权益", "Priority class booking for the next 7 days", 450, "会员特权", "PRIVILEGE", "ticket", 60, false}
        };
        for (int i = 0; i < pp.length; i++) {
            Object[] row = pp[i];
            pointProducts.add(m("id", i + 1L, "name", rowValue(row, 0, "积分商品"), "nameEn", rowValue(row, 1, "Points Item"), "description", rowValue(row, 2, "积分兑换商品"), "descriptionEn", rowValue(row, 3, "Points reward item"), "pointsCost", rowValue(row, 4, 100), "category", rowValue(row, 5, "全部商品"),
                    "discountType", rowValue(row, 6, "PRODUCT"), "icon", rowValue(row, 7, "gift"), "imageKey", rowValue(row, 7, "gift"), "stockQuantity", rowValue(row, 8, 50), "soldCount", i * 4 + 2, "isVip", rowValue(row, 9, false), "active", true));
        }
        exchanges.add(m("id", 1L, "username", "fitness_pro", "productId", 1L, "productName", "5元无门槛券", "productNameEn", "¥5 No-threshold Coupon", "pointsCost", 10, "exchangeTime", LocalDateTime.now().minusDays(3).toString(), "status", "COMPLETED"));
        exchanges.add(m("id", 2L, "username", "fitness_pro", "productId", 6L, "productName", "运动水壶", "productNameEn", "Sports Bottle", "pointsCost", 200, "exchangeTime", LocalDateTime.now().minusDays(1).toString(), "status", "COMPLETED"));
        exchanges.add(m("id", 3L, "username", "strength_member", "productId", 8L, "productName", "健身护腕", "productNameEn", "Fitness Wrist Wraps", "pointsCost", 150, "exchangeTime", LocalDateTime.now().minusDays(2).toString(), "status", "COMPLETED"));
        exchanges.add(m("id", 4L, "username", "yoga_member", "productId", 7L, "productName", "瑜伽垫", "productNameEn", "Yoga Mat", "pointsCost", 300, "exchangeTime", LocalDateTime.now().minusHours(18).toString(), "status", "COMPLETED"));
        exchanges.add(m("id", 5L, "username", "running_king", "productId", 17L, "productName", "会员积分翻倍", "productNameEn", "Double Points Pass", "pointsCost", 400, "exchangeTime", LocalDateTime.now().minusHours(9).toString(), "status", "COMPLETED"));
    }

    private void seedSocial() {
        ads.add(m("id", 1L, "title", "新会员优惠", "description", "新会员开卡享受课程礼包和积分奖励", "tag", "限时活动", "active", true));
        ads.add(m("id", 2L, "title", "私教课特惠", "description", "购买私教课程包立享额外训练评估", "tag", "热门推荐", "active", true));
        ads.add(m("id", 3L, "title", "五月打卡挑战", "description", "连续打卡7天可获得额外积分奖励", "tag", "打卡活动", "active", true));
        ads.add(m("id", 4L, "title", "夏季燃脂营", "description", "团课、饮食建议和排行榜挑战同步开启", "tag", "训练营", "active", true));
        teams.add(m("id", 1L, "title", "周末晨跑小队", "description", "每周六日早上7点，公园集合，一起晨跑！", "category", "running", "location", "中央公园", "meetTime", "周六周日7:00", "creator", "fitness_pro", "creatorName", "健身狂人", "currentMembers", 16, "maxMembers", 30, "members", 16, "capacity", 30, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("fitness_pro", "running_king", "cardio_hero"))));
        teams.add(m("id", 2L, "title", "瑜伽爱好者群", "description", "每周二、四晚8点，一起练瑜伽，放松身心！", "category", "yoga", "location", "健身中心A区", "meetTime", "周二周四20:00", "creator", "yoga_member", "creatorName", "瑜伽达人", "currentMembers", 12, "maxMembers", 20, "members", 12, "capacity", 20, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("yoga_member", "pilates_star"))));
        teams.add(m("id", 3L, "title", "力量训练打卡", "description", "一起打卡力量训练，互相鼓励，共同进步！", "category", "fitness", "location", "健身房力量区", "meetTime", "每天18:00", "creator", "strength_member", "creatorName", "力量小子", "currentMembers", 18, "maxMembers", 25, "members", 18, "capacity", 25, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("strength_member", "power_lifter", "fitness_pro"))));
        teams.add(m("id", 4L, "title", "游泳健身队", "description", "每周三下午，泳池集合，一起游泳健身！", "category", "swimming", "location", "游泳馆", "meetTime", "每周三15:00", "creator", "swim_master", "creatorName", "泳池飞鱼", "currentMembers", 9, "maxMembers", 18, "members", 9, "capacity", 18, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("swim_master", "running_king"))));
        teams.add(m("id", 5L, "title", "搏击燃脂搭子", "description", "下班后一起上搏击和HIIT，互相监督。", "category", "fitness", "location", "D区搏击室", "meetTime", "周一周三19:30", "creator", "boxing_tiger", "creatorName", "搏击老虎", "currentMembers", 7, "maxMembers", 16, "members", 7, "capacity", 16, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("boxing_tiger", "cardio_hero"))));
        teams.add(m("id", 6L, "title", "新人入门互助群", "description", "新会员互相分享预约、打卡和训练经验。", "category", "fitness", "location", "FitLife大厅", "meetTime", "每天18:30", "creator", "fit_newbie", "creatorName", "健身新星", "currentMembers", 11, "maxMembers", 30, "members", 11, "capacity", 30, "joined", false, "createdByCurrentUser", false, "memberUsernames", new ArrayList<String>(Arrays.asList("fit_newbie", "dance_queen"))));
        posts.add(m("id", 1L, "author", "健身狂人", "username", "fitness_pro", "category", "checkin", "content", "今日打卡完成！连续打卡第30天，继续保持！", "likes", 28, "comments", 3, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(3).toString(), "pinned", true));
        posts.add(m("id", 2L, "author", "瑜伽达人", "username", "yoga_member", "category", "share", "content", "今天的训练感觉特别好，热身15分钟、力量训练45分钟、最后拉伸放松。坚持就是胜利！", "likes", 18, "comments", 2, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(6).toString()));
        posts.add(m("id", 3L, "author", "力量小子", "username", "strength_member", "category", "share", "content", "深蹲前一定要热身髋关节和脚踝，动作会稳定很多。", "likes", 22, "comments", 2, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusDays(1).toString()));
        posts.add(m("id", 4L, "author", "跑步爱好者", "username", "running_king", "category", "help", "content", "半马备赛期间力量课和有氧课怎么搭配比较合理？欢迎大家给建议。", "likes", 15, "comments", 3, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(11).toString()));
        posts.add(m("id", 5L, "author", "动感少女", "username", "dance_queen", "category", "team", "content", "今晚19:30舞蹈有氧还有没有人一起？我已经预约啦。", "likes", 20, "comments", 2, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(9).toString()));
        posts.add(m("id", 6L, "author", "搏击老虎", "username", "boxing_tiger", "category", "share", "content", "搏击课前多做肩部激活，出拳更稳，第二天也不容易酸。", "likes", 17, "comments", 1, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(7).toString()));
        posts.add(m("id", 7L, "author", "健身新星", "username", "fit_newbie", "category", "help", "content", "第一次上私教课需要提前准备什么？需要带训练记录吗？", "likes", 12, "comments", 2, "liked", false, "likedBy", new ArrayList<String>(), "createdAt", LocalDateTime.now().minusHours(5).toString()));
        postComments.put(1L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 11L, "postId", 1L, "username", "yoga_member", "author", "瑜伽达人", "content", "太厉害了，30天真的很有毅力！", "createdAt", LocalDateTime.now().minusHours(2).toString()),
                m("id", 12L, "postId", 1L, "username", "strength_member", "author", "力量小子", "content", "继续保持，一起冲更长的连续打卡！", "createdAt", LocalDateTime.now().minusHours(1).toString()),
                m("id", 13L, "postId", 1L, "username", "running_king", "author", "跑步爱好者", "content", "我也要向你学习，坚持每日打卡。", "createdAt", LocalDateTime.now().minusMinutes(40).toString())
        ))));
        postComments.put(2L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 21L, "postId", 2L, "username", "fitness_pro", "author", "健身狂人", "content", "这个训练计划很完整。", "createdAt", LocalDateTime.now().minusHours(4).toString()),
                m("id", 22L, "postId", 2L, "username", "fit_newbie", "author", "健身新星", "content", "拉伸部分我也需要加强。", "createdAt", LocalDateTime.now().minusHours(3).toString())
        ))));
        postComments.put(3L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 31L, "postId", 3L, "username", "dance_queen", "author", "动感少女", "content", "热身脚踝真的很重要。", "createdAt", LocalDateTime.now().minusHours(8).toString()),
                m("id", 32L, "postId", 3L, "username", "power_lifter", "author", "举重王者", "content", "赞同，稳定性会提升很多。", "createdAt", LocalDateTime.now().minusHours(6).toString())
        ))));
        postComments.put(4L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 41L, "postId", 4L, "username", "cardio_hero", "author", "有氧达人", "content", "可以把高强度有氧放在轻力量日，避免恢复不足。", "createdAt", LocalDateTime.now().minusHours(9).toString()),
                m("id", 42L, "postId", 4L, "username", "coach_3", "author", "王教练", "content", "建议每周至少保留一天低强度恢复。", "createdAt", LocalDateTime.now().minusHours(8).toString()),
                m("id", 43L, "postId", 4L, "username", "fitness_pro", "author", "健身狂人", "content", "核心训练也可以保留，跑姿会更稳定。", "createdAt", LocalDateTime.now().minusHours(7).toString())
        ))));
        postComments.put(5L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 51L, "postId", 5L, "username", "pilates_star", "author", "普拉提星", "content", "我想去，刚好需要放松一下。", "createdAt", LocalDateTime.now().minusHours(7).toString()),
                m("id", 52L, "postId", 5L, "username", "cardio_hero", "author", "有氧达人", "content", "我也预约了，晚上见！", "createdAt", LocalDateTime.now().minusHours(6).toString())
        ))));
        postComments.put(6L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 61L, "postId", 6L, "username", "strength_member", "author", "力量小子", "content", "肩部激活确实有用，下次课前试试。", "createdAt", LocalDateTime.now().minusHours(5).toString())
        ))));
        postComments.put(7L, Collections.synchronizedList(new ArrayList<Map<String, Object>>(Arrays.asList(
                m("id", 71L, "postId", 7L, "username", "fitness_pro", "author", "健身狂人", "content", "带水杯、毛巾和舒适运动鞋就可以。", "createdAt", LocalDateTime.now().minusHours(4).toString()),
                m("id", 72L, "postId", 7L, "username", "coach_1", "author", "张教练", "content", "第一次会先做基础评估，不用紧张。", "createdAt", LocalDateTime.now().minusHours(3).toString())
        ))));
        reports.add(m("id", 1L, "postId", 3L, "type", "spam", "reason", "疑似广告内容", "status", "PENDING", "reporter", "fit_newbie"));
        reports.add(m("id", 2L, "postId", 4L, "type", "content", "reason", "希望教练确认训练建议是否准确", "status", "PENDING", "reporter", "cardio_hero"));
    }

    private void seedMailbox() {
        complaints.add(m("id", 1L, "username", "fitness_pro", "userName", "健身狂人", "coachId", 1L, "coachName", "张教练", "type", "course", "title", "课程节奏建议", "titleEn", "Class pacing suggestion", "content", "希望力量训练课程中增加动作分解时间。", "contentEn", "I hope the strength class can include more time for movement breakdowns.", "status", "PENDING", "createdAt", LocalDateTime.now().minusDays(1).toString()));
        complaints.add(m("id", 2L, "username", "yoga_member", "userName", "瑜伽达人", "coachId", 2L, "coachName", "李教练", "type", "facility", "title", "瑜伽垫数量不足", "titleEn", "Not enough yoga mats", "content", "晚课人数多时瑜伽垫不够。", "contentEn", "There are not enough yoga mats when evening classes are crowded.", "status", "processed", "response", "已经向前台申请补充。", "responseEn", "I have asked the front desk to add more mats.", "createdAt", LocalDateTime.now().minusDays(3).toString()));
        complaints.add(m("id", 3L, "username", "fit_newbie", "userName", "健身新星", "coachId", 1L, "coachName", "张教练", "type", "private", "title", "私教课前准备咨询", "titleEn", "Preparation for private session", "content", "第一次私教课想知道是否需要提前做体测。", "contentEn", "I would like to know whether I need a body assessment before my first private class.", "status", "PENDING", "createdAt", LocalDateTime.now().minusHours(10).toString()));
        complaints.add(m("id", 4L, "username", "boxing_tiger", "userName", "搏击老虎", "coachId", 4L, "coachName", "赵教练", "type", "equipment", "title", "搏击手套尺码", "titleEn", "Boxing glove size", "content", "搏击课的备用手套尺码偏少，希望能补充。", "contentEn", "The spare boxing glove sizes are limited. Please add more options.", "status", "PENDING", "createdAt", LocalDateTime.now().minusHours(16).toString()));
        reviews.add(m("id", 1L, "coachId", 1L, "coachName", "张教练", "userName", "健身狂人", "rating", 5, "className", "力量训练", "classNameEn", "Strength Training", "content", "动作讲解清楚，训练计划很有针对性。", "contentEn", "The movement explanation was clear and the training plan was very targeted.", "responded", true, "response", "感谢认可，继续保持训练。", "responseEn", "Thank you for the recognition. Keep training consistently.", "createdAt", LocalDateTime.now().minusDays(2).toString()));
        reviews.add(m("id", 2L, "coachId", 3L, "coachName", "王教练", "userName", "跑步爱好者", "rating", 5, "className", "动感单车", "classNameEn", "Spinning", "content", "动感单车氛围很好。", "contentEn", "The spinning class atmosphere was great.", "responded", false, "createdAt", LocalDateTime.now().minusDays(4).toString()));
        reviews.add(m("id", 3L, "coachId", 2L, "coachName", "李教练", "userName", "普拉提星", "rating", 5, "className", "普拉提核心", "classNameEn", "Pilates Core", "content", "课程节奏温和，核心发力讲得很清楚。", "contentEn", "The pace was comfortable and core engagement was explained clearly.", "responded", false, "createdAt", LocalDateTime.now().minusHours(14).toString()));
        reviews.add(m("id", 4L, "coachId", 4L, "coachName", "赵教练", "userName", "搏击老虎", "rating", 4, "className", "搏击燃脂", "classNameEn", "Boxing Burn", "content", "课程很燃，希望下次增加拉伸时间。", "contentEn", "The class was energetic. I hope there will be more stretching time next time.", "responded", false, "createdAt", LocalDateTime.now().minusHours(18).toString()));
        reviews.add(m("id", 5L, "coachId", 5L, "coachName", "陈教练", "userName", "泳池飞鱼", "rating", 5, "className", "游泳基础", "classNameEn", "Swimming Basics", "content", "纠正划水动作很细致。", "contentEn", "The stroke correction was detailed.", "responded", true, "response", "下次继续关注换气节奏。", "responseEn", "Next time we will keep improving your breathing rhythm.", "createdAt", LocalDateTime.now().minusDays(1).toString()));
    }

    private void seedMemberships() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < users.size(); i++) {
            String username = String.valueOf(users.get(i).get("username"));
            boolean vip = i % 4 == 0 || "fitness_pro".equals(username) || "exercise_addict".equals(username);
            memberships.put(username, m("membershipType", vip ? "VIP年卡" : "标准会员", "startDate", today.minusMonths(2 + (i % 4)).toString(), "endDate", vip ? today.plusYears(1).plusMonths(i % 3).toString() : today.plusMonths(3 + (i % 4)).toString(), "status", "ACTIVE", "remainingSessions", vip ? 60 - i : 18 - (i % 6), "daysRemaining", vip ? 365 + (i % 3) * 30 : 90 + (i % 4) * 20));
        }
    }

    public List<Map<String, Object>> rankings(String period) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        String[] avatar = {"fitness", "power", "yoga", "running", "dance", "weight", "star", "sports", "swim", "pilates", "boxing", "cardio"};
        int base;
        int dayBase;
        if ("week".equals(period)) { base = 14; dayBase = 7; }
        else if ("month".equals(period)) { base = 46; dayBase = 30; }
        else { base = 268; dayBase = 365; }
        for (int i = 0; i < users.size(); i++) {
            Map<String, Object> u = users.get(i);
            String username = String.valueOf(u.get("username"));
            int classesCount = Math.max(3, base - i * ("week".equals(period) ? 1 : ("month".equals(period) ? 3 : 14)) + (i % 3));
            int days = Math.max(2, dayBase - i * ("week".equals(period) ? 0 : ("month".equals(period) ? 2 : 18)) - (i % 2));
            out.add(m("id", u.get("id"), "userId", u.get("id"), "username", username, "name", u.get("name"), "avatar", avatar[i % avatar.length], "totalClasses", classesCount, "score", classesCount, "classes", classesCount, "checkinDays", days, "days", days, "availablePoints", points.get(username), "points", points.get(username)));
        }
        Collections.sort(out, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> a, Map<String, Object> b) { return asInt(b.get("score"), 0) - asInt(a.get("score"), 0); }
        });
        return out;
    }

    public List<Map<String, Object>> monthlyRecords(String username, int year, int month) {
        Set<String> dates = checkinDates.get(username);
        if (dates == null) dates = Collections.emptySet();
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (String d : dates) {
            LocalDate local = LocalDate.parse(d, fmt);
            if (local.getYear() == year && local.getMonthValue() == month) {
                out.add(m("date", d, "checkinDate", d, "checkinTime", local.atTime(8, 30).toString(), "pointsEarned", 10));
            }
        }
        Collections.sort(out, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> a, Map<String, Object> b) { return String.valueOf(a.get("date")).compareTo(String.valueOf(b.get("date"))); }
        });
        return out;
    }

    public List<Map<String, Object>> myBookings(String username) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (bookings) {
            for (Map<String, Object> b : bookings) {
                if (!username.equals(b.get("username"))) continue;
                if (!"CONFIRMED".equalsIgnoreCase(str(b.get("status"), "CONFIRMED"))) continue;
                Map<String, Object> c = classById(asLong(b.get("classId")));
                Map<String, Object> copy = new LinkedHashMap<String, Object>(b);
                copy.put("classes", c);
                out.add(copy);
            }
        }
        return out;
    }

    public List<Map<String, Object>> visibleClasses(String username, String type) {
        List<Map<String, Object>> userBookings = myBookings(username);
        Set<Long> booked = new HashSet<Long>();
        for (Map<String, Object> b : userBookings) {
            if ("CONFIRMED".equalsIgnoreCase(str(b.get("status"), "CONFIRMED"))) booked.add(asLong(b.get("classId")));
        }
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        synchronized (classes) {
            for (Map<String, Object> c : classes) {
                if (type != null && !type.equals(c.get("type"))) continue;
                Map<String, Object> copy = new LinkedHashMap<String, Object>(c);
                copy.put("isBooked", booked.contains(asLong(c.get("id"))));
                out.add(copy);
            }
        }
        return out;
    }
}
