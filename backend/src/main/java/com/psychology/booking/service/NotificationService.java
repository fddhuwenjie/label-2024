package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Notification;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.NotificationMapper;
import com.psychology.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final BookingMapper bookingMapper;
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;

    public Long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getRead, 0)
        );
    }

    public List<Notification> getRecentNotifications(Long userId, int limit) {
        return notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT " + limit)
        );
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                    .eq(Notification::getId, notificationId)
                    .eq(Notification::getUserId, userId)
                    .set(Notification::getRead, 1)
            );
        }
    }

    public void markAllAsRead(Long userId) {
        notificationMapper.update(null,
            new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getRead, 0)
                .set(Notification::getRead, 1)
        );
    }

    public void sendNotification(Long userId, Long bookingId, NotificationType type, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(0);
        notificationMapper.insert(notification);
        log.info("Notification sent: userId={}, type={}, title={}", userId, type, title);
    }

    public void sendBookingCreatedNotification(Booking booking) {
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        String counselorName = counselor != null
            ? counselorMapper.selectById(booking.getCounselorId()).getSpecialty()
            : "咨询师";
        if (counselor != null) {
            var counselorUser = userMapper.selectById(counselor.getUserId());
            if (counselorUser != null) {
                counselorName = counselorUser.getRealName() != null ? counselorUser.getRealName() : counselorUser.getUsername();
            }
        }
        String content = String.format("您已成功预约 %s 的咨询服务，预约日期：%s，时间段：%s。请及时完成支付。",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        sendNotification(booking.getUserId(), booking.getId(), NotificationType.BOOKING_CONFIRMED, "预约创建成功", content);
    }

    public void sendReminder1H(Booking booking) {
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        String counselorName = "咨询师";
        if (counselor != null) {
            var counselorUser = userMapper.selectById(counselor.getUserId());
            if (counselorUser != null) {
                counselorName = counselorUser.getRealName() != null ? counselorUser.getRealName() : counselorUser.getUsername();
            }
        }
        String content = String.format("您预约的 %s 的咨询服务将于 1 小时后开始，预约日期：%s，时间段：%s，请准时参加。",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        sendNotification(booking.getUserId(), booking.getId(), NotificationType.REMINDER_1H, "预约提醒：1小时后", content);
    }

    public void sendReminder15M(Booking booking) {
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        String counselorName = "咨询师";
        if (counselor != null) {
            var counselorUser = userMapper.selectById(counselor.getUserId());
            if (counselorUser != null) {
                counselorName = counselorUser.getRealName() != null ? counselorUser.getRealName() : counselorUser.getUsername();
            }
        }
        String content = String.format("您预约的 %s 的咨询服务将于 15 分钟后开始，预约日期：%s，时间段：%s，请尽快准备。",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        sendNotification(booking.getUserId(), booking.getId(), NotificationType.REMINDER_15M, "预约提醒：15分钟后", content);
    }

    public void sendAutoCancelNotification(Booking booking) {
        String content = String.format("您的预约（日期：%s，时间段：%s）因超时未支付已被系统自动取消。",
            booking.getBookingDate(), booking.getTimeSlot());
        sendNotification(booking.getUserId(), booking.getId(), NotificationType.AUTO_CANCEL, "预约已自动取消", content);
    }

    public boolean hasReminderSent(Long bookingId, NotificationType type) {
        return notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getBookingId, bookingId)
                .eq(Notification::getType, type)
        ) > 0;
    }
}
