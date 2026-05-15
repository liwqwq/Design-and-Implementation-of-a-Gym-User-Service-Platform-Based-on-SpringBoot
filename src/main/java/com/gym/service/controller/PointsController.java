package com.gym.service.controller;

import com.gym.service.entity.Points;
import com.gym.service.entity.PointsExchange;
import com.gym.service.entity.PointsProduct;
import com.gym.service.entity.User;
import com.gym.service.repository.UserRepository;
import com.gym.service.service.PointsService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }
        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPoints(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            Points points = pointsService.getPointsByUserId(userId);
            result.put("success", true);
            result.put("data", points);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/admin/products")
    public ResponseEntity<Map<String, Object>> getAllProductsForAdmin() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PointsProduct> products = pointsService.getAllProductsForAdmin();
            result.put("success", true);
            result.put("data", products);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/products")
    public ResponseEntity<Map<String, Object>> createProductForAdmin(@RequestBody PointsProduct product) {
        Map<String, Object> result = new HashMap<>();
        try {
            PointsProduct saved = pointsService.createProductForAdmin(product);
            result.put("success", true);
            result.put("data", saved);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProductForAdmin(@PathVariable Long id, @RequestBody PointsProduct product) {
        Map<String, Object> result = new HashMap<>();
        try {
            PointsProduct saved = pointsService.updateProductForAdmin(id, product);
            result.put("success", true);
            result.put("data", saved);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProductForAdmin(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            pointsService.deleteProductForAdmin(id);
            result.put("success", true);
            result.put("message", "商品已删除或下架");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PointsProduct> products = pointsService.getAllProducts();
            result.put("success", true);
            result.put("data", products);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/category/{category}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String category) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PointsProduct> products = pointsService.getProductsByCategory(category);
            result.put("success", true);
            result.put("data", products);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/vip")
    public ResponseEntity<Map<String, Object>> getVipProducts() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PointsProduct> products = pointsService.getVipProducts();
            result.put("success", true);
            result.put("data", products);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/exchange/{productId}")
    public ResponseEntity<Map<String, Object>> exchangePoints(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            PointsExchange exchange = pointsService.exchangePoints(userId, productId);
            result.put("success", true);
            result.put("data", exchange);
            result.put("message", "兑换成功");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getExchangeHistory(@RequestHeader("Authorization") String authorizationHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = getUserIdFromToken(authorizationHeader);
            List<PointsExchange> history = pointsService.getUserExchangeHistory(userId);
            result.put("success", true);
            result.put("data", history);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
