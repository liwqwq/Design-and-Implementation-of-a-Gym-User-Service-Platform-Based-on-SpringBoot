package com.gym.service.service.impl;

import com.gym.service.entity.Post;
import com.gym.service.repository.PostRepository;
import com.gym.service.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PostServiceImpl implements PostService {

    private static final Set<String> POST_CATEGORIES = new HashSet<>(Arrays.asList("share", "help", "team", "chat"));

    @Autowired
    private PostRepository postRepository;

    private String normalizePostCategory(String category) {
        if (category == null) {
            return "share";
        }
        String c = category.trim().toLowerCase();
        return POST_CATEGORIES.contains(c) ? c : "share";
    }

    private String normalizeSort(String sortBy) {
        String sort = sortBy == null ? "hot" : sortBy.trim().toLowerCase();
        if (!"hot".equals(sort) && !"latest".equals(sort)) {
            return "latest";
        }
        return sort;
    }

    /** 列表筛选用：all 或 share/help/team/chat */
    private String normalizeListCategory(String category) {
        String cat = category == null ? "all" : category.trim().toLowerCase();
        if (cat.isEmpty() || "all".equals(cat)) {
            return "all";
        }
        if (!POST_CATEGORIES.contains(cat)) {
            return "all";
        }
        return cat;
    }

    private List<Post> fetchPosts(String sort, String cat) {
        if ("hot".equals(sort)) {
            if (!"all".equals(cat)) {
                return postRepository.findByCategoryHot(cat);
            }
            return postRepository.findAllActiveHot();
        }
        if (!"all".equals(cat)) {
            return postRepository.findByCategoryLatest(cat);
        }
        return postRepository.findAllActiveLatest();
    }

    @Override
    public Post createPost(String content, String username, Long userId, String category, String image) {
        Post post = new Post();
        post.setContent(content);
        post.setUsername(username);
        post.setUserId(userId);
        post.setCategory(normalizePostCategory(category));
        post.setImage(image);
        post.setLikes(0);
        post.setComments(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setActive(true);
        return postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts(String sortBy, String category) {
        return fetchPosts(normalizeSort(sortBy), normalizeListCategory(category));
    }

    @Override
    public Map<String, Object> getPostsFeed(String sortBy, String category) {
        String sort = normalizeSort(sortBy);
        String cat = normalizeListCategory(category);
        List<Post> list = fetchPosts(sort, cat);
        Map<String, Object> meta = new HashMap<>();
        meta.put("sortBy", sort);
        meta.put("category", cat);
        meta.put("total", list.size());
        Map<String, Object> out = new HashMap<>();
        out.put("data", list);
        out.put("meta", meta);
        return out;
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public Post incrementLikes(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
            return postRepository.save(post);
        }
        return null;
    }

    @Override
    public Post incrementComments(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setComments(post.getComments() + 1);
            return postRepository.save(post);
        }
        return null;
    }
}
