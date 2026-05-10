package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Notification;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public List<Notification> listByUserId(Long userId, int limit) {
        return notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT " + limit)
        );
    }

    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
        );
    }

    @Transactional
    public void markAllRead(Long userId) {
        Notification notification = new Notification();
        notification.setReadStatus(1);
        notificationMapper.update(notification,
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
        );
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setReadStatus(1);
            notificationMapper.updateById(notification);
        }
    }

    @Transactional
    public void createNotification(Long userId, NotificationType type, String title, String content, Long bookingId) {
        Notification existing = notificationMapper.selectOne(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getBookingId, bookingId)
                .eq(Notification::getType, type)
        );
        if (existing != null) {
            log.info("Notification already exists for user {} booking {} type {}", userId, bookingId, type);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBookingId(bookingId);
        notification.setReadStatus(0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("Created notification for user {}: {}", userId, title);
    }
}
