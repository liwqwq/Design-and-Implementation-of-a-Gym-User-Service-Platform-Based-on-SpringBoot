package com.gym.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** JWT 内自定义 role 声明（教练登录为 ROLE_COACH），用于过滤器优先识别教练身份 */
    public String extractRole(String token) {
        try {
            Object r = extractAllClaims(token).get("role");
            return r == null ? null : String.valueOf(r);
        } catch (Exception e) {
            return null;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public String generateCoachToken(String username, Long coachId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_COACH");
        claims.put("coachId", coachId);
        return createToken(claims, username);
    }

    /** 长期有效的到店二维码载荷（由会员端展示，门店扫码后调用 /api/checkin/scan） */
    public String generateCheckinQrToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("typ", "checkin_qr");
        claims.put("uid", userId);
        long tenYearsMs = 10L * 365 * 24 * 60 * 60 * 1000;
        return Jwts.builder().setClaims(claims).setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tenYearsMs))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Long parseCheckinQrUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (!"checkin_qr".equals(String.valueOf(claims.get("typ")))) {
            throw new IllegalArgumentException("Invalid QR token type");
        }
        Object uid = claims.get("uid");
        if (uid == null) {
            throw new IllegalArgumentException("Missing uid");
        }
        if (uid instanceof Number) {
            return ((Number) uid).longValue();
        }
        return Long.parseLong(String.valueOf(uid));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}