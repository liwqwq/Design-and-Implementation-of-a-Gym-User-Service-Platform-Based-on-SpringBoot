package com.gym.service.repository;

import com.gym.service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /** 热门：互动量优先，与「最新」排序维度不同 */
    @Query("SELECT p FROM Post p WHERE p.active = true ORDER BY (p.likes + p.comments) DESC, p.createdAt DESC, p.id DESC")
    List<Post> findAllActiveHot();

    /** 最新：严格按时间，其次 id，避免与热门顺序偶然完全一致 */
    @Query("SELECT p FROM Post p WHERE p.active = true ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findAllActiveLatest();

    @Query("SELECT p FROM Post p WHERE p.active = true AND COALESCE(p.category, 'share') = :category ORDER BY (p.likes + p.comments) DESC, p.createdAt DESC, p.id DESC")
    List<Post> findByCategoryHot(@Param("category") String category);

    @Query("SELECT p FROM Post p WHERE p.active = true AND COALESCE(p.category, 'share') = :category ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findByCategoryLatest(@Param("category") String category);

    /** 管理端：全部帖子（含已下架），按 id 倒序 */
    List<Post> findAllByOrderByIdDesc();
}
