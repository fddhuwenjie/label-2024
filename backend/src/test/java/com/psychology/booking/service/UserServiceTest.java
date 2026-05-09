package com.psychology.booking.service;

import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.UserMapper;
import com.psychology.booking.util.JwtUtil;
import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(BCrypt.hashpw("password123"));
        testUser.setNickname("测试用户");
        testUser.setRole(0);
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("登录成功")
    void login_Success() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyInt())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString(), anyInt())).thenReturn("refresh-token");

        Map<String, Object> result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("access-token", result.get("token"));
        assertEquals("refresh-token", result.get("refreshToken"));
        assertEquals(testUser, result.get("user"));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_UserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login("nonexistent", "password"));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.login("testuser", "wrongpassword"));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("注册成功")
    void register_Success() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any())).thenReturn(1);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password123");

        User result = userService.register(newUser);

        assertNotNull(result);
        assertEquals(0, result.getRole());
        assertEquals(1, result.getStatus());
        verify(userMapper).insert(any());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists() {
        when(userMapper.selectOne(any())).thenReturn(testUser);

        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(newUser));

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取用户信息")
    void getById_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("刷新Token成功")
    void refreshToken_Success() {
        when(jwtUtil.isTokenBlacklisted(anyString())).thenReturn(false);
        when(jwtUtil.getUserId(anyString())).thenReturn(1L);
        when(jwtUtil.getUsername(anyString())).thenReturn("testuser");
        when(jwtUtil.getRole(anyString())).thenReturn(0);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyInt())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString(), anyInt())).thenReturn("new-refresh-token");

        Map<String, Object> result = userService.refreshToken("old-refresh-token");

        assertNotNull(result);
        assertEquals("new-access-token", result.get("token"));
        assertEquals("new-refresh-token", result.get("refreshToken"));
        verify(jwtUtil).blacklistToken("old-refresh-token");
    }

    @Test
    @DisplayName("刷新Token失败 - Token已失效")
    void refreshToken_Blacklisted() {
        when(jwtUtil.isTokenBlacklisted(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.refreshToken("blacklisted-token"));

        assertEquals("Token已失效，请重新登录", exception.getMessage());
    }
}
