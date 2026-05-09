package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.UserMapper;
import com.psychology.booking.util.JwtUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        log.info("User login attempt: {}", username);
        
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            log.warn("Login failed - user not found: {}", username);
            throw new BusinessException("用户名或密码错误");
        }
        
        if (!checkPassword(password, user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", username);
            throw new BusinessException("用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
        
        log.info("User logged in successfully: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);
        return result;
    }

    private boolean checkPassword(String raw, String encoded) {
        try {
            return BCrypt.checkpw(raw, encoded);
        } catch (Exception e) {
            log.error("Password check error", e);
            return false;
        }
    }

    @Transactional
    public User register(User user) {
        log.info("User registration attempt: {}", user.getUsername());
        
        User existing = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())
        );
        if (existing != null) {
            log.warn("Registration failed - username exists: {}", user.getUsername());
            throw new BusinessException("用户名已存在");
        }
        
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
        
        log.info("User registered successfully: {}", user.getUsername());
        return user;
    }

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Transactional
    public void update(User user) {
        log.info("Updating user: {}", user.getId());
        userMapper.updateById(user);
    }
    
    public Map<String, Object> refreshToken(String refreshToken) {
        log.info("Token refresh attempt");
        
        if (jwtUtil.isTokenBlacklisted(refreshToken)) {
            throw new BusinessException("Token已失效，请重新登录");
        }
        
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        Integer role = jwtUtil.getRole(refreshToken);
        
        String newToken = jwtUtil.generateToken(userId, username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, role);
        
        // 将旧的refreshToken加入黑名单
        jwtUtil.blacklistToken(refreshToken);
        
        log.info("Token refreshed for user: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }
    
    public void logout(String token, String refreshToken) {
        log.info("User logout");
        if (token != null) {
            jwtUtil.blacklistToken(token);
        }
        if (refreshToken != null) {
            jwtUtil.blacklistToken(refreshToken);
        }
    }

    public List<User> listAll() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void updateRole(Long id, Integer role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        userMapper.updateById(user);
    }
}
