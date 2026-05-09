package com.psychology.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psychology.booking.dto.LoginRequest;
import com.psychology.booking.dto.RegisterRequest;
import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 单元测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setRole(0);
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("登录接口 - 成功")
    void login_Success() throws Exception {
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "access-token");
        loginResult.put("refreshToken", "refresh-token");
        loginResult.put("user", testUser);

        when(userService.login(anyString(), anyString())).thenReturn(loginResult);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("登录接口 - 失败")
    void login_Failure() throws Exception {
        when(userService.login(anyString(), anyString()))
                .thenThrow(new BusinessException("用户名或密码错误"));

        LoginRequest request = new LoginRequest();
        request.setUsername("wronguser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("注册接口 - 成功")
    void register_Success() throws Exception {
        when(userService.register(any(User.class))).thenReturn(testUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setNickname("新用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("注册接口 - 用户名已存在")
    void register_UsernameExists() throws Exception {
        when(userService.register(any(User.class)))
                .thenThrow(new BusinessException("用户名已存在"));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setNickname("已存在用户");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("刷新Token接口 - 成功")
    void refreshToken_Success() throws Exception {
        Map<String, Object> refreshResult = new HashMap<>();
        refreshResult.put("token", "new-access-token");
        refreshResult.put("refreshToken", "new-refresh-token");

        when(userService.refreshToken(anyString())).thenReturn(refreshResult);

        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "old-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("new-access-token"));
    }

    @Test
    @DisplayName("登出接口 - 成功")
    void logout_Success() throws Exception {
        doNothing().when(userService).logout(anyString(), anyString());

        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
