package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Notification;
import com.psychology.booking.service.NotificationService;
import com.psychology.booking.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "获取用户通知列表")
    @GetMapping("/my")
    public Result<List<Notification>> myNotifications(@RequestHeader("Authorization") String token,
                                                      @RequestParam(defaultValue = "20") int limit) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(notificationService.listByUserId(userId, limit));
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(notificationService.countUnread(userId));
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/mark-all-read")
    public Result<Void> markAllRead(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        notificationService.markAllRead(userId);
        return Result.success();
    }

    @Operation(summary = "标记单条通知为已读")
    @PutMapping("/{id}/mark-read")
    public Result<Void> markRead(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        notificationService.markRead(id, userId);
        return Result.success();
    }
}
