package com.gym.service.controller;

import com.gym.service.entity.Comment;
import com.gym.service.entity.Like;
import com.gym.service.entity.Post;
import com.gym.service.entity.Report;
import com.gym.service.entity.User;
import com.gym.service.repository.CommentRepository;
import com.gym.service.repository.LikeRepository;
import com.gym.service.repository.PostRepository;
import com.gym.service.repository.ReportRepository;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ReportRepository reportRepository;

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(auth.getName());
    }

    private ResponseEntity<Map<String, Object>> unauthorized(Map<String, Object> response) {
        response.put("success", false);
        response.put("message", "LOGIN_REQUIRED");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    private Map<String, Object> toPostDto(Post post, Long currentUserId) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", post.getId());
        dto.put("content", post.getContent());
        dto.put("username", post.getUsername());
        dto.put("userId", post.getUserId());
        dto.put("createdAt", post.getCreatedAt());
        dto.put("likes", post.getLikes());
        dto.put("comments", post.getComments());
        dto.put("image", post.getImage());
        dto.put("category", post.getCategory());
        dto.put("active", post.getActive());
        boolean liked = false;
        boolean reported = false;
        if (currentUserId != null && post.getId() != null) {
            liked = likeRepository.findByPostIdAndUserId(post.getId(), currentUserId).isPresent();
            reported = reportRepository.findByPostIdAndReporterId(post.getId(), currentUserId).stream()
                    .anyMatch(r -> r.getCommentId() == null);
        }
        dto.put("liked", liked);
        dto.put("reported", reported);
        return dto;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                return unauthorized(response);
            }
            User user = userOpt.get();
            String content = request.get("content") == null ? "" : request.get("content").trim();
            if (content.isEmpty()) {
                response.put("success", false);
                response.put("message", "CONTENT_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            String category = request.getOrDefault("category", "share");
            String image = request.get("image");

            Post post = postService.createPost(content, user.getUsername(), user.getId(), category, image);
            response.put("success", true);
            response.put("data", toPostDto(post, user.getId()));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error creating post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "all") String category) {
        Map<String, Object> response = new HashMap<>();
        try {
            String sortParam = sortBy == null ? "hot" : sortBy.trim();
            String categoryParam = category == null ? "all" : category.trim();
            Long currentUserId = currentUser().map(User::getId).orElse(null);
            Map<String, Object> bundle = postService.getPostsFeed(sortParam, categoryParam);
            @SuppressWarnings("unchecked")
            List<Post> posts = (List<Post>) bundle.get("data");
            List<Map<String, Object>> rows = posts.stream()
                    .map(p -> toPostDto(p, currentUserId))
                    .collect(Collectors.toList());
            response.put("success", true);
            response.put("data", rows);
            response.put("meta", bundle.get("meta"));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting all posts: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long currentUserId = currentUser().map(User::getId).orElse(null);
            Post post = postService.getPostById(id);
            if (post != null && Boolean.TRUE.equals(post.getActive())) {
                response.put("success", true);
                response.put("data", toPostDto(post, currentUserId));
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "帖子不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Error getting post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                return unauthorized(response);
            }
            User user = userOpt.get();
            Post post = postRepository.findById(id).orElse(null);
            if (post == null || !Boolean.TRUE.equals(post.getActive())) {
                response.put("success", false);
                response.put("message", "帖子不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (likeRepository.findByPostIdAndUserId(id, user.getId()).isPresent()) {
                response.put("success", true);
                response.put("liked", true);
                response.put("alreadyLiked", true);
                response.put("likes", post.getLikes());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            Like like = new Like();
            like.setPostId(id);
            like.setUserId(user.getId());
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);

            post.setLikes(post.getLikes() + 1);
            Post saved = postRepository.save(post);
            response.put("success", true);
            response.put("liked", true);
            response.put("likes", saved.getLikes());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error liking post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/report")
    @Transactional
    public ResponseEntity<Map<String, Object>> reportPost(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                return unauthorized(response);
            }
            User user = userOpt.get();
            Post post = postRepository.findById(id).orElse(null);
            if (post == null || !Boolean.TRUE.equals(post.getActive())) {
                response.put("success", false);
                response.put("message", "帖子不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            boolean alreadyReported = reportRepository.findByPostIdAndReporterId(id, user.getId()).stream()
                    .anyMatch(r -> r.getCommentId() == null);
            if (alreadyReported) {
                response.put("success", true);
                response.put("reported", true);
                response.put("alreadyReported", true);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            Report report = new Report();
            report.setPostId(id);
            report.setReporterId(user.getId());
            report.setReportedUserId(post.getUserId() != null ? post.getUserId() : user.getId());
            report.setType(request.getOrDefault("type", "OTHER").toUpperCase());
            report.setReason(request.getOrDefault("reason", ""));
            report.setStatus("PENDING");
            report.setCreatedAt(LocalDateTime.now());
            reportRepository.save(report);
            response.put("success", true);
            response.put("reported", true);
            response.put("data", report);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error reporting post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Post post = postRepository.findById(id).orElse(null);
            if (post == null || !Boolean.TRUE.equals(post.getActive())) {
                response.put("success", false);
                response.put("message", "帖子不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(id);
            response.put("success", true);
            response.put("data", comments);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting comments: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/comments")
    @Transactional
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> userOpt = currentUser();
            if (!userOpt.isPresent()) {
                return unauthorized(response);
            }
            Post post = postRepository.findById(id).orElse(null);
            if (post == null || !Boolean.TRUE.equals(post.getActive())) {
                response.put("success", false);
                response.put("message", "帖子不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            String content = request.get("content") == null ? "" : request.get("content").trim();
            if (content.isEmpty()) {
                response.put("success", false);
                response.put("message", "CONTENT_REQUIRED");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            User user = userOpt.get();

            Comment comment = new Comment();
            comment.setPostId(id);
            comment.setUserId(user.getId());
            comment.setUsername(user.getUsername());
            comment.setContent(content);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setLikes(0);
            commentRepository.save(comment);

            post.setComments((int) commentRepository.countByPostId(id));
            postRepository.save(post);

            response.put("success", true);
            response.put("data", comment);
            response.put("comments", post.getComments());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error adding comment: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
