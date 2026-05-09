package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Review;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.ReviewMapper;
import com.psychology.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final BookingMapper bookingMapper;
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;

    public List<Review> listByCounselor(Long counselorId) {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getCounselorId, counselorId)
                .orderByDesc(Review::getCreatedAt)
        );
        reviews.forEach(r -> r.setUser(userMapper.selectById(r.getUserId())));
        return reviews;
    }

    @Transactional
    public Review create(Review review, Long userId) {
        log.info("Creating review for booking: {} by user: {}", review.getBookingId(), userId);
        
        // 验证预约是否存在且已完成
        Booking booking = bookingMapper.selectById(review.getBookingId());
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此预约");
        }
        if (booking.getStatus() != 2) { // 2-completed
            throw new BusinessException("只能评价已完成的预约");
        }
        
        // 检查是否已评价
        Review existing = reviewMapper.selectOne(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getBookingId, review.getBookingId())
        );
        if (existing != null) {
            throw new BusinessException("该预约已评价");
        }
        
        review.setUserId(userId);
        review.setCounselorId(booking.getCounselorId());
        reviewMapper.insert(review);
        
        // 更新咨询师评分
        updateCounselorRating(booking.getCounselorId());
        
        log.info("Review created successfully");
        return review;
    }

    private void updateCounselorRating(Long counselorId) {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getCounselorId, counselorId)
        );
        
        if (!reviews.isEmpty()) {
            BigDecimal totalRating = reviews.stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgRating = totalRating.divide(
                BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
            
            Counselor counselor = counselorMapper.selectById(counselorId);
            if (counselor != null) {
                counselor.setRating(avgRating);
                counselorMapper.updateById(counselor);
            }
        }
    }

    public List<Review> listAll() {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().orderByDesc(Review::getCreatedAt)
        );
        reviews.forEach(r -> {
            r.setUser(userMapper.selectById(r.getUserId()));
            r.setBooking(bookingMapper.selectById(r.getBookingId()));
        });
        return reviews;
    }

    public void delete(Long id) {
        reviewMapper.deleteById(id);
    }
}
