package com.psychology.booking.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.TimeSlotMapper;
import com.psychology.booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final BookingMapper bookingMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *")
    public void processBookingReminders() {
        log.debug("Running booking reminder task at {}", LocalDateTime.now());
        
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .in(Booking::getStatus, 0, 1)
        );

        for (Booking booking : bookings) {
            try {
                LocalDateTime bookingDateTime = parseBookingDateTime(booking);
                if (bookingDateTime == null) {
                    continue;
                }

                long minutesToBooking = java.time.Duration.between(now, bookingDateTime).toMinutes();

                if (minutesToBooking >= 59 && minutesToBooking <= 61) {
                    sendReminder(booking, NotificationType.REMINDER_1H, "预约提醒", 
                        "您的心理咨询预约还有1小时开始，请准时参加！");
                }

                if (minutesToBooking >= 14 && minutesToBooking <= 16) {
                    sendReminder(booking, NotificationType.REMINDER_15M, "预约即将开始", 
                        "您的心理咨询预约还有15分钟开始！");
                }
            } catch (Exception e) {
                log.error("Error processing reminder for booking {}", booking.getId(), e);
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelOverdueBookings() {
        log.debug("Running overdue booking cancellation task at {}", LocalDateTime.now());
        
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        
        List<Booking> overdueBookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, 0)
                .lt(Booking::getCreatedAt, thirtyMinutesAgo)
        );

        for (Booking booking : overdueBookings) {
            try {
                log.info("Auto-cancelling booking {} created at {}", booking.getId(), booking.getCreatedAt());
                
                booking.setStatus(3);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingMapper.updateById(booking);
                
                releaseTimeSlot(booking);
                
                notificationService.createNotification(
                    booking.getUserId(),
                    NotificationType.AUTO_CANCEL,
                    "预约已自动取消",
                    "由于您在30分钟内未完成支付，预约已自动取消。",
                    booking.getId()
                );
            } catch (Exception e) {
                log.error("Error auto-cancelling booking {}", booking.getId(), e);
            }
        }
    }

    private void sendReminder(Booking booking, NotificationType type, String title, String content) {
        notificationService.createNotification(
            booking.getUserId(),
            type,
            title,
            content,
            booking.getId()
        );
    }

    private LocalDateTime parseBookingDateTime(Booking booking) {
        LocalDate date = booking.getBookingDate();
        String timeSlot = booking.getTimeSlot();
        if (date == null || timeSlot == null) {
            return null;
        }
        String[] timeParts = timeSlot.split("-");
        if (timeParts.length < 1) {
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(timeParts[0], DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

    private void releaseTimeSlot(Booking booking) {
        String[] timeParts = booking.getTimeSlot().split("-");
        if (timeParts.length < 1) {
            return;
        }
        TimeSlot timeSlot = timeSlotMapper.selectOne(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, booking.getCounselorId())
                .eq(TimeSlot::getDate, booking.getBookingDate())
                .eq(TimeSlot::getStartTime, timeParts[0])
        );
        if (timeSlot != null) {
            timeSlot.setStatus(0);
            timeSlotMapper.updateById(timeSlot);
        }
    }
}
