package com.psychology.booking.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.psychology.booking.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration; // 默认7天
    
    // Token黑名单（生产环境应使用Redis）
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // 定期清理过期的黑名单token
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3600000); // 每小时清理一次
                    long now = System.currentTimeMillis();
                    tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() < now);
                    log.debug("Cleaned up expired blacklisted tokens");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public String generateToken(Long userId, String username, Integer role) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }
    
    public String generateRefreshToken(Long userId, String username, Integer role) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verifyToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                throw new BusinessException(401, "Token已失效");
            }
            return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            throw new BusinessException(401, "Token无效或已过期");
        }
    }

    public Long getUserId(String token) {
        return verifyToken(token).getClaim("userId").asLong();
    }
    
    public String getUsername(String token) {
        return verifyToken(token).getClaim("username").asString();
    }

    public Integer getRole(String token) {
        return verifyToken(token).getClaim("role").asInt();
    }
    
    public void blacklistToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            long expiresAt = jwt.getExpiresAt().getTime();
            tokenBlacklist.put(token, expiresAt);
            log.debug("Token blacklisted");
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.containsKey(token);
    }
}
