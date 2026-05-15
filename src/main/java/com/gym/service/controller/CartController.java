package com.gym.service.controller;

import com.gym.service.entity.Cart;
import com.gym.service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查是否为匿名用户
            if ("anonymousUser".equals(username)) {
                response.put("success", false);
                response.put("message", "请先登录");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.valueOf(request.get("quantity").toString());
            
            cartService.addToCart(username, productId, quantity);
            response.put("success", true);
            response.put("message", "商品已成功加入购物车");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error adding to cart: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCartItems() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查是否为匿名用户
            if ("anonymousUser".equals(username)) {
                response.put("success", true);
                response.put("data", new java.util.ArrayList<>());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            List<Cart> cartItems = cartService.getCartItems(username);
            response.put("success", true);
            response.put("data", cartItems);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting cart items: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long id) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查是否为匿名用户
            if ("anonymousUser".equals(username)) {
                response.put("success", false);
                response.put("message", "请先登录");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            cartService.removeFromCart(id);
            response.put("success", true);
            response.put("message", "商品已成功从购物车移除");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error removing from cart: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart() {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // 检查是否为匿名用户
            if ("anonymousUser".equals(username)) {
                response.put("success", false);
                response.put("message", "请先登录");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            cartService.clearCart(username);
            response.put("success", true);
            response.put("message", "购物车已清空");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error clearing cart: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
