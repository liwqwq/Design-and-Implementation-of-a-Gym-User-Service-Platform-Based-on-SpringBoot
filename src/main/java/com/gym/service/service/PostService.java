package com.gym.service.service;

import com.gym.service.entity.Post;

import java.util.List;
import java.util.Map;

public interface PostService {
    Post createPost(String content, String username, Long userId, String category, String image);
    List<Post> getAllPosts(String sortBy, String category);

    /** 列表数据 + meta（排序、分类、条数），供前端展示筛选结果 */
    Map<String, Object> getPostsFeed(String sortBy, String category);

    Post getPostById(Long id);
    Post incrementLikes(Long id);
    Post incrementComments(Long id);
}
