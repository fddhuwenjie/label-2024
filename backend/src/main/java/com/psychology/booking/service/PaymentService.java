package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Payment;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMapper paymentMapper;
    private final BookingMapper bookingMapper;
    private final CounselorMapper counselorMapper;

    @Transactional
    public Payment createPayment(Long bookingId, Long userId, String paymentMethod) {
        log.info("Creating payment for booking: {}", bookingId);
        
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权支付此预约");
        }
        
        // 检查是否已有支付记录
        Payment existing = paymentMapper.selectOne(
            new LambdaQueryWrapper<Payment>()
                .eq(Payment::getBookingId, bookingId)
                .ne(Payment::getStatus, 3) // 排除支付失败的
        );
        if (existing != null && existing.getStatus() == 1) {
            throw new BusinessException("该预约已支付");
        }
        
        Counselor counselor = counselorMapper.selectById(booking.getCounselorId());
        
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setOrderNo(generateOrderNo());
        payment.setAmount(counselor.getPrice());
        payment.setStatus(0); // 待支付
        payment.setPaymentMethod(paymentMethod);
        paymentMapper.insert(payment);
        
        return payment;
    }

    /**
     * 模拟支付回调（实际项目中应由支付平台回调）
     */
    @Transactional
    public Payment mockPaymentCallback(String orderNo, boolean success) {
        log.info("Mock payment callback for order: {}, success: {}", orderNo, success);
        
        Payment payment = paymentMapper.selectOne(
            new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (success) {
            payment.setStatus(1); // 已支付
            payment.setTransactionId("MOCK_" + UUID.randomUUID().toString().substring(0, 16));
            payment.setPaidAt(LocalDateTime.now());
            
            // 更新预约状态为已确认
            Booking booking = bookingMapper.selectById(payment.getBookingId());
            if (booking != null) {
                booking.setStatus(1); // confirmed
                bookingMapper.updateById(booking);
            }
        } else {
            payment.setStatus(3); // 支付失败
        }
        
        paymentMapper.updateById(payment);
        return payment;
    }

    @Transactional
    public Payment refund(Long paymentId, Long userId) {
        log.info("Processing refund for payment: {}", paymentId);
        
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此支付");
        }
        if (payment.getStatus() != 1) {
            throw new BusinessException("只能退款已支付的订单");
        }
        
        payment.setStatus(2); // 已退款
        paymentMapper.updateById(payment);
        
        // 取消预约
        Booking booking = bookingMapper.selectById(payment.getBookingId());
        if (booking != null) {
            booking.setStatus(3); // cancelled
            bookingMapper.updateById(booking);
        }
        
        log.info("Refund processed successfully");
        return payment;
    }

    public Payment getByOrderNo(String orderNo) {
        return paymentMapper.selectOne(
            new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
        );
    }

    private String generateOrderNo() {
        return "PAY" + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
