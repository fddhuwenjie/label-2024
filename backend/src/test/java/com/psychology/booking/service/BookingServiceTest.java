package com.psychology.booking.service;

import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.TimeSlotMapper;
import com.psychology.booking.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService 单元测试")
class BookingServiceTest {

    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CounselorMapper counselorMapper;
    @Mock
    private TimeSlotMapper timeSlotMapper;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Counselor testCounselor;
    private Booking testBooking;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testCounselor = new Counselor();
        testCounselor.setId(1L);
        testCounselor.setUserId(2L);
        testCounselor.setAvailable(1);
        testCounselor.setBookingCount(0);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(1L);
        testBooking.setCounselorId(1L);
        testBooking.setBookingDate(LocalDate.now().plusDays(1));
        testBooking.setTimeSlot("09:00-10:00");
        testBooking.setStatus(0);

        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setCounselorId(1L);
        testTimeSlot.setDate(LocalDate.now().plusDays(1));
        testTimeSlot.setStartTime("09:00");
        testTimeSlot.setEndTime("10:00");
        testTimeSlot.setStatus(0);
    }

    @Test
    @DisplayName("查询用户预约列表")
    void listByUserId_Success() {
        when(bookingMapper.selectList(any())).thenReturn(Arrays.asList(testBooking));
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(counselorMapper.selectById(anyLong())).thenReturn(testCounselor);

        List<Booking> result = bookingService.listByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    @DisplayName("创建预约成功")
    void create_Success() {
        when(counselorMapper.selectById(anyLong())).thenReturn(testCounselor);
        when(timeSlotMapper.selectOne(any())).thenReturn(testTimeSlot);
        when(bookingMapper.selectOne(any())).thenReturn(null);
        when(bookingMapper.insert(any())).thenReturn(1);

        Booking newBooking = new Booking();
        newBooking.setUserId(1L);
        newBooking.setCounselorId(1L);
        newBooking.setBookingDate(LocalDate.now().plusDays(1));
        newBooking.setTimeSlot("09:00-10:00");

        Booking result = bookingService.create(newBooking);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(bookingMapper).insert(any());
        verify(counselorMapper).updateById(any());
    }

    @Test
    @DisplayName("创建预约失败 - 过去的日期")
    void create_PastDate() {
        Booking newBooking = new Booking();
        newBooking.setUserId(1L);
        newBooking.setCounselorId(1L);
        newBooking.setBookingDate(LocalDate.now().minusDays(1));
        newBooking.setTimeSlot("09:00-10:00");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.create(newBooking));

        assertEquals("预约日期不能是过去的日期", exception.getMessage());
    }

    @Test
    @DisplayName("创建预约失败 - 咨询师不存在")
    void create_CounselorNotFound() {
        when(counselorMapper.selectById(anyLong())).thenReturn(null);

        Booking newBooking = new Booking();
        newBooking.setUserId(1L);
        newBooking.setCounselorId(999L);
        newBooking.setBookingDate(LocalDate.now().plusDays(1));
        newBooking.setTimeSlot("09:00-10:00");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.create(newBooking));

        assertEquals("咨询师不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建预约失败 - 咨询师不可用")
    void create_CounselorUnavailable() {
        testCounselor.setAvailable(0);
        when(counselorMapper.selectById(anyLong())).thenReturn(testCounselor);

        Booking newBooking = new Booking();
        newBooking.setUserId(1L);
        newBooking.setCounselorId(1L);
        newBooking.setBookingDate(LocalDate.now().plusDays(1));
        newBooking.setTimeSlot("09:00-10:00");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.create(newBooking));

        assertEquals("该咨询师暂不可预约", exception.getMessage());
    }

    @Test
    @DisplayName("创建预约失败 - 时间段已被预约")
    void create_TimeSlotTaken() {
        when(counselorMapper.selectById(anyLong())).thenReturn(testCounselor);
        when(timeSlotMapper.selectOne(any())).thenReturn(testTimeSlot);
        when(bookingMapper.selectOne(any())).thenReturn(testBooking);

        Booking newBooking = new Booking();
        newBooking.setUserId(2L);
        newBooking.setCounselorId(1L);
        newBooking.setBookingDate(LocalDate.now().plusDays(1));
        newBooking.setTimeSlot("09:00-10:00");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.create(newBooking));

        assertEquals("该时间段已被预约", exception.getMessage());
    }

    @Test
    @DisplayName("取消预约成功")
    void cancel_Success() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(timeSlotMapper.selectOne(any())).thenReturn(testTimeSlot);

        bookingService.cancel(1L, 1L);

        verify(bookingMapper).updateById(any());
        verify(timeSlotMapper).updateById(any());
    }

    @Test
    @DisplayName("取消预约失败 - 预约不存在")
    void cancel_NotFound() {
        when(bookingMapper.selectById(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancel(999L, 1L));

        assertEquals("预约不存在", exception.getMessage());
    }

    @Test
    @DisplayName("取消预约失败 - 无权限")
    void cancel_NoPermission() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancel(1L, 999L));

        assertEquals("无权取消此预约", exception.getMessage());
    }

    @Test
    @DisplayName("取消预约失败 - 已完成或已取消")
    void cancel_AlreadyCompleted() {
        testBooking.setStatus(2);
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bookingService.cancel(1L, 1L));

        assertEquals("该预约已完成或已取消", exception.getMessage());
    }

    @Test
    @DisplayName("更新预约状态")
    void updateStatus_Success() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        bookingService.updateStatus(1L, 1);

        verify(bookingMapper).updateById(any());
    }
}
