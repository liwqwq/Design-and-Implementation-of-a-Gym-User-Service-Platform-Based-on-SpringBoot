package com.gym.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    // 用户端路由
    @GetMapping("/user/**")
    public String userRoutes() {
        return "forward:/index.html";
    }

    // 管理端路由
    @GetMapping("/admin/**")
    public String adminRoutes() {
        return "forward:/index.html";
    }
}