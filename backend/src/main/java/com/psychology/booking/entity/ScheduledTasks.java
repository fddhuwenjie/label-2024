package com.psychology.booking.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.TimeSlotMapper;
import com.psychology.booking.mapper.UserMapper;
import com.psychology.booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final BookingMapper bookingMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processRemindersAndAutoCancel() {
        log.debug("Running scheduled task: processRemindersAndAutoCancel");
        processBookingReminders();
        processAutoCancel();
    }

    private void processBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);
        LocalDateTime fifteenMinutesLater = now.plusMinutes(15);

        List<Booking> upcomingBookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .in(Booking::getStatus, 0, 1)
                .ge(Booking::getBookingDate, LocalDate.now())
        );

        for (Booking booking : upcomingBookings) {
            LocalDateTime bookingDateTime = parseBookingDateTime(booking);
            if (bookingDateTime == null) {
                continue;
            }

            if (isTimeToSendReminder(now, oneHourLater, bookingDateTime) 
                && !notificationService.hasReminderBeenSent(booking.getId(), NotificationType.REMINDER_1H)) {
                sendReminder(booking, NotificationType.REMINDER_1H, "1小时");
            }

            if (isTimeToSendReminder(now, fifteenMinutesLater, bookingDateTime)
                && !notificationService.hasReminderBeenSent(booking.getId(), NotificationType.REMINDER_15M)) {
                sendReminder(booking, NotificationType.REMINDER_15M, "15分钟");
            }
        }
    }

    private boolean isTimeToSendReminder(LocalDateTime now, LocalDateTime targetTime, LocalDateTime bookingTime) {
        return bookingTime.isAfter(now) && bookingTime.isBefore(targetTime.plusSeconds(30));
    }

    private LocalDateTime parseBookingDateTime(Booking booking) {
        try {
            LocalDate date = booking.getBookingDate();
            String timeSlot = booking.getTimeSlot();
            String startTimeStr = timeSlot.split("-")[0];
            LocalTime startTime = LocalTime.parse(startTimeStr);
            return LocalDateTime.of(date, startTime);
        } catch (Exception e) {
            log.error("Failed to parse booking date time for booking: {}", booking.getId(), e);
            return null;
        }
    }

    private void sendReminder(Booking booking, NotificationType type, String timeText) {
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        String counselorName = "咨询师";
        if (counselor != null) {
            var counselorUser = userMapper.selectById(counselor.getUserId());
            if (counselorUser != null) {
                counselorName = counselorUser.getRealName() != null ? counselorUser.getRealName() : counselorUser.getUsername();
            }
        }

        String title = "预约提醒";
        String content = String.format("您与%s的咨询预约还有%s开始，请准时参加。预约时间：%s %s",
            counselorName, timeText, booking.getBookingDate(), booking.getTimeSlot());

        notificationService.createNotification(booking.getUserId(), type, title, content, booking.getId());
    }

    private void processAutoCancel() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        List<Booking> pendingBookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, 0)
                .lt(Booking::getCreatedAt, thirtyMinutesAgo)
        );

        for (Booking booking : pendingBookings) {
            cancelBooking(booking);
        }
    }

    @Transactional
    public void cancelBooking(Booking booking) {
        log.info("Auto cancelling booking: {} due to payment timeout", booking.getId());

        booking.setStatus(3);
        bookingMapper.updateById(booking);

        releaseTimeSlot(booking);

        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        String counselorName = "咨询师";
        if (counselor != null) {
            var counselorUser = userMapper.selectById(counselor.getUserId());
            if (counselorUser != null) {
                counselorName = counselorUser.getRealName() != null ? counselorUser.getRealName() : counselorUser.getUsername();
            }
        }

        String title = "预约已取消";
        String content = String.format("您与%s的预约因超时未支付已自动取消。预约时间：%s %s",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());

        notificationService.createNotification(booking.getUserId(), NotificationType.AUTO_CANCEL, title, content, booking.getId());
    }

    private void releaseTimeSlot(Booking booking) {
        try {
            String[] timeParts = booking.getTimeSlot().split("-");
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
        } catch (Exception e) {
            log.error("Failed to release time slot for booking: {}", booking.getId(), e);
        }
    }
}
