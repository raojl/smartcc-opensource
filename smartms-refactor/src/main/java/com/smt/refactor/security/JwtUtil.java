package com.smt.refactor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具：签发与解析（HS256）
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:smartms-refactor-secret-key-2026}")
    private String secret;

    @Value("${jwt.expire-hours:12}")
    private long expireHours;

    public String generateToken(Integer userId, String loginName) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("loginName", loginName)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    /**
     * 解析 token；非法/过期抛异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
