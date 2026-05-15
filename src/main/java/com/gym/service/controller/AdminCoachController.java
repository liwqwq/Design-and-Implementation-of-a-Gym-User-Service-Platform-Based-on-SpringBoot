package com.gym.service.controller;

import com.gym.service.entity.Coach;
import com.gym.service.repository.CoachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coaches")
public class AdminCoachController {

    @Autowired
    private CoachRepository coachRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listCoaches() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Coach c : coachRepository.findAll()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("username", c.getUsername());
                m.put("name", c.getName());
                m.put("email", c.getEmail());
                m.put("phone", c.getPhone());
                list.add(m);
            }
            response.put("success", true);
            response.put("data", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
