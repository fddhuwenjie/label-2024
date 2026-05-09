package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.User;
import com.psychology.booking.service.UserService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/me")
    public Result<Void> updateProfile(@RequestBody User user,
                                      @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        user.setId(userId);
        user.setPassword(null);
        user.setRole(null);
        userService.update(user);
        return Result.success();
    }

    // 管理员接口：获取所有用户
    @GetMapping
    public Result<List<User>> listAll() {
        return Result.success(userService.listAll());
    }

    // 管理员接口：更新用户状态（启用/禁用）
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    // 管理员接口：更新用户角色
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam Integer role) {
        userService.updateRole(id, role);
        return Result.success();
    }
}
