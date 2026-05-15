package com.gym.service.controller;

import com.gym.service.entity.Post;
import com.gym.service.entity.Report;
import com.gym.service.entity.User;
import com.gym.service.repository.CommentRepository;
import com.gym.service.repository.LikeRepository;
import com.gym.service.repository.PostRepository;
import com.gym.service.repository.ReportRepository;
import com.gym.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

    private static final Set<String> CATEGORIES = new HashSet<>(Arrays.asList("share", "help", "team", "chat"));

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    private static Map<String, Object> toRow(Report r, Optional<User> reporterOpt, Optional<Post> postOpt) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("status", r.getStatus());
        m.put("type", r.getType());
        m.put("reason", r.getReason());
        m.put("postId", r.getPostId());
        m.put("commentId", r.getCommentId());
        m.put("reporterId", r.getReporterId());
        m.put("reportedUserId", r.getReportedUserId());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        m.put("processedAt", r.getProcessedAt() != null ? r.getProcessedAt().toString() : null);
        m.put("reporter", reporterOpt.map(User::getUsername).orElse("—"));
        m.put("postContent", postOpt.map(Post::getContent).orElse("—"));
        return m;
    }

    private static Map<String, Object> postToDto(Post p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("content", p.getContent());
        m.put("username", p.getUsername());
        m.put("userId", p.getUserId());
        m.put("category", p.getCategory());
        m.put("likes", p.getLikes());
        m.put("comments", p.getComments());
        m.put("active", p.getActive());
        m.put("image", p.getImage());
        m.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
        return m;
    }

    private static String normalizeCategory(Object raw) {
        if (raw == null) {
            return null;
        }
        String c = String.valueOf(raw).trim().toLowerCase();
        return CATEGORIES.contains(c) ? c : "share";
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> listReports() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Report> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Report r : reports) {
                Optional<User> reporterOpt = userRepository.findById(r.getReporterId());
                Optional<Post> postOpt = r.getPostId() != null ? postRepository.findById(r.getPostId()) : Optional.empty();
                rows.add(toRow(r, reporterOpt, postOpt));
            }
            response.put("success", true);
            response.put("data", rows);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reports/{id}/process")
    @Transactional
    public ResponseEntity<Map<String, Object>> process(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Report r = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
            if (!"PENDING".equalsIgnoreCase(r.getStatus())) {
                response.put("success", false);
                response.put("message", "NOT_PENDING");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            r.setStatus("PROCESSED");
            r.setProcessedAt(LocalDateTime.now());
            reportRepository.save(r);
            if (r.getPostId() != null) {
                postRepository.findById(r.getPostId()).ifPresent(p -> {
                    p.setActive(false);
                    postRepository.save(p);
                });
            }
            response.put("success", true);
            response.put("data", toRow(r,
                    userRepository.findById(r.getReporterId()),
                    r.getPostId() != null ? postRepository.findById(r.getPostId()) : Optional.empty()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reports/{id}/ignore")
    @Transactional
    public ResponseEntity<Map<String, Object>> ignore(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Report r = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Report not found"));
            if (!"PENDING".equalsIgnoreCase(r.getStatus())) {
                response.put("success", false);
                response.put("message", "NOT_PENDING");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            r.setStatus("IGNORED");
            r.setProcessedAt(LocalDateTime.now());
            reportRepository.save(r);
            response.put("success", true);
            response.put("data", toRow(r,
                    userRepository.findById(r.getReporterId()),
                    r.getPostId() != null ? postRepository.findById(r.getPostId()) : Optional.empty()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> listPosts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Post> posts = postRepository.findAllByOrderByIdDesc();
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Post p : posts) {
                rows.add(postToDto(p));
            }
            response.put("success", true);
            response.put("data", rows);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/posts/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
            if (body.containsKey("content") && body.get("content") != null) {
                String content = String.valueOf(body.get("content")).trim();
                if (!content.isEmpty()) {
                    post.setContent(content);
                }
            }
            if (body.containsKey("category") && body.get("category") != null) {
                post.setCategory(normalizeCategory(body.get("category")));
            }
            if (body.containsKey("active")) {
                Object a = body.get("active");
                if (a instanceof Boolean) {
                    post.setActive((Boolean) a);
                } else {
                    post.setActive(Boolean.parseBoolean(String.valueOf(a)));
                }
            }
            Post saved = postRepository.save(post);
            response.put("success", true);
            response.put("data", postToDto(saved));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/posts/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!postRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Post not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            likeRepository.deleteByPostId(id);
            commentRepository.deleteByPostId(id);
            reportRepository.deleteByPostId(id);
            postRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "deleted");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
