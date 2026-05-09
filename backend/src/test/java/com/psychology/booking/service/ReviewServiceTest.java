package com.psychology.booking.service;

import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Review;
import com.psychology.booking.entity.User;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.ReviewMapper;
import com.psychology.booking.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 单元测试")
class ReviewServiceTest {

    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CounselorMapper counselorMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private Booking testBooking;
    private Counselor testCounselor;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testCounselor = new Counselor();
        testCounselor.setId(1L);
        testCounselor.setRating(new BigDecimal("4.5"));

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(1L);
        testBooking.setCounselorId(1L);
        testBooking.setStatus(2); // completed

        testReview = new Review();
        testReview.setId(1L);
        testReview.setBookingId(1L);
        testReview.setUserId(1L);
        testReview.setCounselorId(1L);
        testReview.setRating(new BigDecimal("5.0"));
        testReview.setContent("非常专业的咨询师");
    }

    @Test
    @DisplayName("获取咨询师评价列表")
    void listByCounselor_Success() {
        when(reviewMapper.selectList(any())).thenReturn(Arrays.asList(testReview));
        when(userMapper.selectById(anyLong())).thenReturn(testUser);

        List<Review> result = reviewService.listByCounselor(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    @DisplayName("获取咨询师评价列表 - 空列表")
    void listByCounselor_Empty() {
        when(reviewMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Review> result = reviewService.listByCounselor(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("创建评价成功")
    void create_Success() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(reviewMapper.selectOne(any())).thenReturn(null);
        when(reviewMapper.insert(any())).thenReturn(1);
        when(reviewMapper.selectList(any())).thenReturn(Arrays.asList(testReview));
        when(counselorMapper.selectById(1L)).thenReturn(testCounselor);

        Review newReview = new Review();
        newReview.setBookingId(1L);
        newReview.setRating(new BigDecimal("5.0"));
        newReview.setContent("很好的咨询体验");

        Review result = reviewService.create(newReview, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getCounselorId());
        verify(reviewMapper).insert(any());
        verify(counselorMapper).updateById(any());
    }

    @Test
    @DisplayName("创建评价失败 - 预约不存在")
    void create_BookingNotFound() {
        when(bookingMapper.selectById(999L)).thenReturn(null);

        Review newReview = new Review();
        newReview.setBookingId(999L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.create(newReview, 1L));

        assertEquals("预约不存在", exception.getMessage());
    }

    @Test
    @DisplayName("创建评价失败 - 无权评价")
    void create_NoPermission() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        Review newReview = new Review();
        newReview.setBookingId(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.create(newReview, 999L));

        assertEquals("无权评价此预约", exception.getMessage());
    }

    @Test
    @DisplayName("创建评价失败 - 预约未完成")
    void create_BookingNotCompleted() {
        testBooking.setStatus(0); // pending
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);

        Review newReview = new Review();
        newReview.setBookingId(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.create(newReview, 1L));

        assertEquals("只能评价已完成的预约", exception.getMessage());
    }

    @Test
    @DisplayName("创建评价失败 - 已评价")
    void create_AlreadyReviewed() {
        when(bookingMapper.selectById(1L)).thenReturn(testBooking);
        when(reviewMapper.selectOne(any())).thenReturn(testReview);

        Review newReview = new Review();
        newReview.setBookingId(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.create(newReview, 1L));

        assertEquals("该预约已评价", exception.getMessage());
    }

    @Test
    @DisplayName("获取所有评价列表（管理端）")
    void listAll_Success() {
        when(reviewMapper.selectList(any())).thenReturn(Arrays.asList(testReview));
        when(userMapper.selectById(anyLong())).thenReturn(testUser);
        when(bookingMapper.selectById(anyLong())).thenReturn(testBooking);

        List<Review> result = reviewService.listAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getUser());
        assertNotNull(result.get(0).getBooking());
    }

    @Test
    @DisplayName("删除评价")
    void delete_Success() {
        reviewService.delete(1L);

        verify(reviewMapper).deleteById(1L);
    }
}
