package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.NotificationType;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.TimeSlotMapper;
import com.psychology.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final CounselorMapper counselorMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final NotificationService notificationService;

    public List<Booking> listByUserId(Long userId) {
        List<Booking> bookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, userId)
                .orderByDesc(Booking::getCreatedAt)
        );
        fillDetails(bookings);
        return bookings;
    }

    public List<Booking> listByCounselorId(Long counselorId) {
        List<Booking> bookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .orderByDesc(Booking::getCreatedAt)
        );
        fillDetails(bookings);
        return bookings;
    }

    public List<Booking> listAll() {
        List<Booking> bookings = bookingMapper.selectList(
            new LambdaQueryWrapper<Booking>().orderByDesc(Booking::getCreatedAt)
        );
        fillDetails(bookings);
        return bookings;
    }

    private void fillDetails(List<Booking> bookings) {
        bookings.forEach(b -> {
            b.setUser(userMapper.selectById(b.getUserId()));
            Counselor c = counselorMapper.selectById(b.getCounselorId());
            if (c != null) {
                c.setUser(userMapper.selectById(c.getUserId()));
            }
            b.setCounselor(c);
        });
    }

    @Transactional
    public Booking create(Booking booking) {
        log.info("Creating booking for user: {}, counselor: {}, date: {}, slot: {}", 
                 booking.getUserId(), booking.getCounselorId(), 
                 booking.getBookingDate(), booking.getTimeSlot());
        
        // 验证预约日期不能是过去
        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new BusinessException("预约日期不能是过去的日期");
        }
        
        // 验证咨询师是否存在且可用
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        if (counselor == null) {
            throw new BusinessException("咨询师不存在");
        }
        if (counselor.getAvailable() != 1) {
            throw new BusinessException("该咨询师暂不可预约");
        }
        
        // 不能预约自己
        if (counselor.getUserId().equals(booking.getUserId())) {
            throw new BusinessException("不能预约自己的咨询服务");
        }
        
        // 检查时间段是否可用
        String[] timeParts = booking.getTimeSlot().split("-");
        TimeSlot timeSlot = timeSlotMapper.selectOne(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, booking.getCounselorId())
                .eq(TimeSlot::getDate, booking.getBookingDate())
                .eq(TimeSlot::getStartTime, timeParts[0])
                .eq(TimeSlot::getStatus, 0) // 可预约
        );
        
        // 检查是否有冲突的预约（同一咨询师、同一日期、同一时间段）
        Booking existing = bookingMapper.selectOne(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, booking.getCounselorId())
                .eq(Booking::getBookingDate, booking.getBookingDate())
                .eq(Booking::getTimeSlot, booking.getTimeSlot())
                .in(Booking::getStatus, 0, 1) // pending或confirmed
        );
        if (existing != null) {
            throw new BusinessException("该时间段已被预约");
        }
        
        // 检查用户是否在同一时间有其他预约
        Booking userConflict = bookingMapper.selectOne(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getUserId, booking.getUserId())
                .eq(Booking::getBookingDate, booking.getBookingDate())
                .eq(Booking::getTimeSlot, booking.getTimeSlot())
                .in(Booking::getStatus, 0, 1)
        );
        if (userConflict != null) {
            throw new BusinessException("您在该时间段已有其他预约");
        }
        
        booking.setStatus(0); // pending
        bookingMapper.insert(booking);
        
        // 更新时间段状态
        if (timeSlot != null) {
            timeSlot.setStatus(1); // 已预约
            timeSlotMapper.updateById(timeSlot);
        }
        
        // 更新咨询师预约数
        counselor.setBookingCount(counselor.getBookingCount() + 1);
        counselorMapper.updateById(counselor);
        
        // 发送预约创建通知
        User counselorUser = userMapper.selectById(counselor.getUserId());
        String counselorName = counselorUser != null && counselorUser.getRealName() != null 
            ? counselorUser.getRealName() : "咨询师";
        String title = "预约创建成功";
        String content = String.format("您已成功预约%s的咨询服务，请在30分钟内完成支付。预约时间：%s %s",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        notificationService.createNotification(booking.getUserId(), NotificationType.BOOKING_CONFIRMED, 
            title, content, booking.getId());
        
        log.info("Booking created successfully with id: {}", booking.getId());
        return booking;
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        log.info("Updating booking status: {} to {}", id, status);
        Booking booking = bookingMapper.selectById(id);
        if (booking != null) {
            booking.setStatus(status);
            bookingMapper.updateById(booking);
            
            // 如果取消，释放时间段
            if (status == 3) {
                releaseTimeSlot(booking);
            }
        }
    }

    @Transactional
    public void cancel(Long id, Long userId) {
        log.info("Cancelling booking: {} by user: {}", id, userId);
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权取消此预约");
        }
        if (booking.getStatus() == 2 || booking.getStatus() == 3) {
            throw new BusinessException("该预约已完成或已取消");
        }
        
        booking.setStatus(3); // cancelled
        bookingMapper.updateById(booking);
        
        // 释放时间段
        releaseTimeSlot(booking);
        
        log.info("Booking cancelled successfully");
    }

    // 咨询师确认预约
    @Transactional
    public void confirmByCounselor(Long id, Long counselorUserId) {
        log.info("Counselor {} confirming booking {}", counselorUserId, id);
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        if (counselor == null || !counselor.getUserId().equals(counselorUserId)) {
            throw new BusinessException("无权操作此预约");
        }
        
        if (booking.getStatus() != 0) {
            throw new BusinessException("只能确认待处理的预约");
        }
        
        booking.setStatus(1); // confirmed
        bookingMapper.updateById(booking);
        
        // 发送预约确认通知
        User counselorUser = userMapper.selectById(counselor.getUserId());
        String counselorName = counselorUser != null && counselorUser.getRealName() != null 
            ? counselorUser.getRealName() : "咨询师";
        String title = "预约已确认";
        String content = String.format("您与%s的咨询预约已被确认，请准时参加。预约时间：%s %s",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        notificationService.createNotification(booking.getUserId(), NotificationType.BOOKING_CONFIRMED, 
            title, content, booking.getId());
        
        log.info("Booking confirmed successfully");
    }

    // 咨询师完成预约
    @Transactional
    public void completeByCounselor(Long id, Long counselorUserId) {
        log.info("Counselor {} completing booking {}", counselorUserId, id);
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        if (counselor == null || !counselor.getUserId().equals(counselorUserId)) {
            throw new BusinessException("无权操作此预约");
        }
        
        if (booking.getStatus() != 1) {
            throw new BusinessException("只能完成已确认的预约");
        }
        
        booking.setStatus(2); // completed
        bookingMapper.updateById(booking);
        log.info("Booking completed successfully");
    }

    // 咨询师拒绝预约
    @Transactional
    public void rejectByCounselor(Long id, Long counselorUserId) {
        log.info("Counselor {} rejecting booking {}", counselorUserId, id);
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        if (counselor == null || !counselor.getUserId().equals(counselorUserId)) {
            throw new BusinessException("无权操作此预约");
        }
        
        if (booking.getStatus() != 0) {
            throw new BusinessException("只能拒绝待处理的预约");
        }
        
        booking.setStatus(3); // cancelled/rejected
        bookingMapper.updateById(booking);
        
        // 释放时间段
        releaseTimeSlot(booking);
        
        // 发送预约拒绝通知
        User counselorUser = userMapper.selectById(counselor.getUserId());
        String counselorName = counselorUser != null && counselorUser.getRealName() != null 
            ? counselorUser.getRealName() : "咨询师";
        String title = "预约已被拒绝";
        String content = String.format("很抱歉，您与%s的咨询预约已被拒绝。预约时间：%s %s",
            counselorName, booking.getBookingDate(), booking.getTimeSlot());
        notificationService.createNotification(booking.getUserId(), NotificationType.BOOKING_REJECTED, 
            title, content, booking.getId());
        
        log.info("Booking rejected successfully");
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
            timeSlot.setStatus(0); // 可预约
            timeSlotMapper.updateById(timeSlot);
        }
    }
}
