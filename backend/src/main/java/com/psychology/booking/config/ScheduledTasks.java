package com.psychology.booking.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.entity.Payment;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.PaymentMapper;
import com.psychology.booking.mapper.TimeSlotMapper;
import com.psychology.booking.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasks {
    private final BookingMapper bookingMapper;
    private final PaymentMapper paymentMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders() {
        log.debug("Checking booking reminders...");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> activeBookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .in(Booking::getStatus, 0, 1)
        );

        for (Booking booking : activeBookings) {
            LocalDateTime bookingTime = calculateBookingDateTime(booking);
            if (bookingTime == null) {
                continue;
            }

            long minutesUntilBooking = java.time.Duration.between(now, bookingTime).toMinutes();

            if (minutesUntilBooking > 0 && minutesUntilBooking <= 60
                && !notificationService.hasReminderSent(booking.getId(), NotificationType.REMINDER_1H)) {
                notificationService.sendReminder1H(booking);
                log.info("Sent 1h reminder for booking {}", booking.getId());
            }

            if (minutesUntilBooking > 0 && minutesUntilBooking <= 15
                && !notificationService.hasReminderSent(booking.getId(), NotificationType.REMINDER_15M)) {
                notificationService.sendReminder15M(booking);
                log.info("Sent 15m reminder for booking {}", booking.getId());
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndAutoCancelUnpaidBookings() {
        log.debug("Checking unpaid bookings for auto-cancellation...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        List<Booking> unpaidBookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, 0)
                .le(Booking::getCreatedAt, threshold)
        );

        for (Booking booking : unpaidBookings) {
            Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getBookingId, booking.getId())
                    .eq(Payment::getStatus, 1)
            );

            if (payment != null) {
                continue;
            }

            log.info("Auto-cancelling unpaid booking {}", booking.getId());
            booking.setStatus(3);
            bookingMapper.updateById(booking);

            releaseTimeSlot(booking);

            notificationService.sendAutoCancelNotification(booking);
        }
    }

    private LocalDateTime calculateBookingDateTime(Booking booking) {
        String timeSlot = booking.getTimeSlot();
        if (timeSlot == null || !timeSlot.contains("-")) {
            return null;
        }
        String startTime = timeSlot.split("-")[0];
        String[] parts = startTime.split(":");
        if (parts.length < 2) {
            return null;
        }
        try {
            LocalTime start = LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            return LocalDateTime.of(booking.getBookingDate(), start);
        } catch (Exception e) {
            log.warn("Failed to parse booking time slot: {}", timeSlot);
            return null;
        }
    }

    private void releaseTimeSlot(Booking booking) {
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
    }
}
