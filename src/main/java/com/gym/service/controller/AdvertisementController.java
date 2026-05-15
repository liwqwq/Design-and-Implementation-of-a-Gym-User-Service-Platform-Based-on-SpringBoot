package com.gym.service.controller;

import com.gym.service.entity.Advertisement;
import com.gym.service.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getActiveAdvertisements() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Advertisement> ads = advertisementRepository
                    .findByIsActiveTrueAndExpiresAtAfterOrderByPositionAsc(LocalDateTime.now());
            response.put("success", true);
            response.put("data", ads);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error getting advertisements: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
