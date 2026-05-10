package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Notification;
import com.psychology.booking.service.NotificationService;
import com.psychology.booking.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "站内通知相关接口")
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    public Result<Long> getUnreadCount(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近通知列表", description = "获取当前用户最近的通知列表，默认返回最近20条")
    public Result<List<Notification>> getRecentNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(notificationService.getRecentNotifications(userId, limit));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    public Result<Void> markAsRead(@PathVariable Long id,
                                   @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有未读通知标记为已读")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}
