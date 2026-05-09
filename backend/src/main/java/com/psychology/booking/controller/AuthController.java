package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.dto.LoginRequest;
import com.psychology.booking.dto.RegisterRequest;
import com.psychology.booking.entity.User;
import com.psychology.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = userService.login(
            request.getUsername(), 
            request.getPassword()
        );
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return Result.success(userService.register(user));
    }
    
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refreshToken");
        return Result.success(userService.refreshToken(refreshToken));
    }
    
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token,
                               @RequestBody(required = false) Map<String, String> params) {
        String accessToken = token != null ? token.replace("Bearer ", "") : null;
        String refreshToken = params != null ? params.get("refreshToken") : null;
        userService.logout(accessToken, refreshToken);
        return Result.success();
    }
}
