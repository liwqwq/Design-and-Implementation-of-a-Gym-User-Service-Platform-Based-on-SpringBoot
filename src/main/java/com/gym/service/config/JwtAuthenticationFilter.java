package com.gym.service.config;

import com.gym.service.entity.Admin;
import com.gym.service.repository.AdminRepository;
import com.gym.service.repository.CoachRepository;
import com.gym.service.service.UserService;
import com.gym.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApplicationContext applicationContext;

    private UserService userService;
    private AdminRepository adminRepository;
    private CoachRepository coachRepository;

    private UserService getUserService() {
        if (userService == null) {
            userService = applicationContext.getBean(UserService.class);
        }
        return userService;
    }

    private AdminRepository getAdminRepository() {
        if (adminRepository == null) {
            adminRepository = applicationContext.getBean(AdminRepository.class);
        }
        return adminRepository;
    }

    private CoachRepository getCoachRepository() {
        if (coachRepository == null) {
            coachRepository = applicationContext.getBean(CoachRepository.class);
        }
        return coachRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // JWT解析失败，继续执行过滤器链
            }
        }

        // 仅当「无认证」或「仍为匿名认证」时处理 Bearer：若匿名过滤器先于本过滤器执行，
        // 上下文中已是 AnonymousAuthenticationToken（非 null），原先 == null 判断会跳过 JWT，导致 POST 等请求 403。
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean mayApplyBearerJwt = username != null && jwt != null
                && (existingAuth == null || existingAuth instanceof AnonymousAuthenticationToken);

        if (mayApplyBearerJwt) {
            try {
                // 教练 JWT（含 role=ROLE_COACH）优先于会员 loadUserByUsername，避免同名会员账号抢走身份导致接口 401
                if ("ROLE_COACH".equals(jwtUtil.extractRole(jwt))
                        && getCoachRepository().findByUsername(username).isPresent()
                        && jwtUtil.validateToken(jwt, username)) {
                    UsernamePasswordAuthenticationToken coachAuth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_COACH")));
                    coachAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(coachAuth);
                } else {
                    UserDetails userDetails = null;
                    try {
                        userDetails = getUserService().loadUserByUsername(username);
                    } catch (UsernameNotFoundException ex) {
                        java.util.Optional<Admin> adminOpt = getAdminRepository().findByUsername(username);
                        if (adminOpt.isPresent()) {
                            Admin admin = adminOpt.get();
                            userDetails = new org.springframework.security.core.userdetails.User(
                                    admin.getUsername(),
                                    admin.getPassword(),
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                            );
                        } else if (getCoachRepository().findByUsername(username).isPresent()
                                && jwtUtil.validateToken(jwt, username)) {
                            UsernamePasswordAuthenticationToken coachAuth = new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_COACH")));
                            coachAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(coachAuth);
                        }
                    }

                    if (userDetails != null && jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            } catch (Exception e) {
                // 验证失败，继续执行过滤器链
            }
        }
        filterChain.doFilter(request, response);
    }
}