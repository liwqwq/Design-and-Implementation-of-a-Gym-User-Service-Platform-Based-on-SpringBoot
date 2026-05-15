package com.gym.service.repository;

import com.gym.service.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUsername(String username);
    Cart findByUsernameAndProductId(String username, Long productId);
}
