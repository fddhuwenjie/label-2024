package com.psychology.booking.service;

import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Payment;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 单元测试")
class PaymentServiceTest {

    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CounselorMapper counselorMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Booking testBooking;
    private Counselor testCounselor;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(1L);
        testBooking.setCounselorId(1L);
        testBooking.setBookingDate(LocalDate.now().plusDays(1));
        testBooking.setTimeSlot("09:00-10:00");
        testBooking.setStatus(0);

        testCounselor = new Counselor();
        testCounselor.setId(1L);
        testCounselor.setPrice(new BigDecimal("500"));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setBookingId(1L);
        testPayment.setUserId(1L);
        testPayment.setOrderNo("PAY1234567890");
        testPayment.setAmount(new BigDecimal("500"));
        testPayment.setStatus(0);
        testPayment.setPaymentMethod("mock");
    }

    @Test
    @DisplayName("创建支付订单成功")
    void createPayment_Success() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(paymentMapper.selectOne(any())).thenReturn(null);
        when(counselorMapper.selectById(1L)).thenReturn(testCounselor);
        when(paymentMapper.insert(any())).thenReturn(1);

        Payment result = paymentService.createPayment(1L, 1L, "mock");

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        assertEquals(new BigDecimal("500"), result.getAmount());
        verify(paymentMapper).insert(any());
    }

    @Test
    @DisplayName("创建支付订单失败 - 预约不存在")
    void createPayment_BookingNotFound() {
        when(bookingMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(999L, 1L, "mock"));

        assertEquals("预约不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建支付订单失败 - 无权支付")
    void createPayment_NoPermission() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(1L, 999L, "mock"));

        assertEquals("无权支付此预约", exception.getMessage());
    }

    @Test
    @DisplayName("创建支付订单失败 - 已支付")
    void createPayment_AlreadyPaid() {
        testPayment.setStatus(1);
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(paymentMapper.selectOne(any())).thenReturn(testPayment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(1L, 1L, "mock"));

        assertEquals("该预约已支付", exception.getMessage());
    }

    @Test
    @DisplayName("模拟支付回调成功")
    void mockPaymentCallback_Success() {
        when(paymentMapper.selectOne(any())).thenReturn(testPayment);
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        Payment result = paymentService.mockPaymentCallback("PAY1234567890", true);

        assertEquals(1, result.getStatus());
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getPaidAt());
        verify(paymentMapper).updateById(any());
        verify(bookingMapper).updateById(any());
    }

    @Test
    @DisplayName("模拟支付回调失败")
    void mockPaymentCallback_Failed() {
        when(paymentMapper.selectOne(any())).thenReturn(testPayment);

        Payment result = paymentService.mockPaymentCallback("PAY1234567890", false);

        assertEquals(3, result.getStatus());
        verify(paymentMapper).updateById(any());
    }

    @Test
    @DisplayName("模拟支付回调 - 订单不存在")
    void mockPaymentCallback_OrderNotFound() {
        when(paymentMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.mockPaymentCallback("INVALID", true));

        assertEquals("订单不存在", exception.getMessage());
    }

    @Test
    @DisplayName("退款成功")
    void refund_Success() {
        testPayment.setStatus(1);
        when(paymentMapper.selectById(1L)).thenReturn(testPayment);
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        Payment result = paymentService.refund(1L, 1L);

        assertEquals(2, result.getStatus());
        verify(paymentMapper).updateById(any());
        verify(bookingMapper).updateById(any());
    }

    @Test
    @DisplayName("退款失败 - 支付记录不存在")
    void refund_PaymentNotFound() {
        when(paymentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.refund(999L, 1L));

        assertEquals("支付记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("退款失败 - 无权操作")
    void refund_NoPermission() {
        testPayment.setStatus(1);
        when(paymentMapper.selectById(1L)).thenReturn(testPayment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.refund(1L, 999L));

        assertEquals("无权操作此支付", exception.getMessage());
    }

    @Test
    @DisplayName("退款失败 - 订单未支付")
    void refund_NotPaid() {
        when(paymentMapper.selectById(1L)).thenReturn(testPayment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.refund(1L, 1L));

        assertEquals("只能退款已支付的订单", exception.getMessage());
    }

    @Test
    @DisplayName("根据订单号查询支付")
    void getByOrderNo_Success() {
        when(paymentMapper.selectOne(any())).thenReturn(testPayment);

        Payment result = paymentService.getByOrderNo("PAY1234567890");

        assertNotNull(result);
        assertEquals("PAY1234567890", result.getOrderNo());
    }
}
