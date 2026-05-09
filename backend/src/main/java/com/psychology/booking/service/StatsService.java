package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Review;
import com.psychology.booking.entity.User;
import com.psychology.booking.mapper.BookingMapper;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.ReviewMapper;
import com.psychology.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final UserMapper userMapper;
    private final CounselorMapper counselorMapper;
    private final BookingMapper bookingMapper;
    private final ReviewMapper reviewMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 0));
        stats.put("totalUsers", totalUsers);
        
        // 咨询师统计
        Long totalCounselors = counselorMapper.selectCount(new LambdaQueryWrapper<Counselor>().eq(Counselor::getAvailable, 1));
        stats.put("totalCounselors", totalCounselors);
        
        // 预约统计
        Long totalBookings = bookingMapper.selectCount(null);
        Long pendingBookings = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, 0));
        Long confirmedBookings = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, 1));
        Long completedBookings = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, 2));
        Long cancelledBookings = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>().eq(Booking::getStatus, 3));
        
        stats.put("totalBookings", totalBookings);
        stats.put("pendingBookings", pendingBookings);
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("completedBookings", completedBookings);
        stats.put("cancelledBookings", cancelledBookings);
        
        // 评价统计
        Long totalReviews = reviewMapper.selectCount(null);
        stats.put("totalReviews", totalReviews);
        
        // 今日数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>().ge(Booking::getCreatedAt, todayStart)
        );
        Long todayUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart).eq(User::getRole, 0)
        );
        stats.put("todayBookings", todayBookings);
        stats.put("todayUsers", todayUsers);
        
        return stats;
    }

    public Map<String, Object> getBookingTrend(Integer days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            Long count = bookingMapper.selectCount(
                new LambdaQueryWrapper<Booking>()
                    .ge(Booking::getCreatedAt, dayStart)
                    .lt(Booking::getCreatedAt, dayEnd)
            );
            counts.add(count);
        }
        
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }

    // 咨询师个人统计
    public Map<String, Object> getCounselorStats(Long counselorId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 预约统计
        Long totalBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>().eq(Booking::getCounselorId, counselorId)
        );
        Long pendingBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .eq(Booking::getStatus, 0)
        );
        Long confirmedBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .eq(Booking::getStatus, 1)
        );
        Long completedBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .eq(Booking::getStatus, 2)
        );
        
        stats.put("totalBookings", totalBookings);
        stats.put("pendingBookings", pendingBookings);
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("completedBookings", completedBookings);
        
        // 评价统计
        Long totalReviews = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>().eq(Review::getCounselorId, counselorId)
        );
        stats.put("totalReviews", totalReviews);
        
        // 咨询师信息
        Counselor counselor = counselorMapper.selectById(counselorId);
        if (counselor != null) {
            stats.put("rating", counselor.getRating());
            stats.put("price", counselor.getPrice());
        }
        
        // 本月数据
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long monthBookings = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .ge(Booking::getCreatedAt, monthStart)
        );
        Long monthCompleted = bookingMapper.selectCount(
            new LambdaQueryWrapper<Booking>()
                .eq(Booking::getCounselorId, counselorId)
                .eq(Booking::getStatus, 2)
                .ge(Booking::getCreatedAt, monthStart)
        );
        stats.put("monthBookings", monthBookings);
        stats.put("monthCompleted", monthCompleted);
        
        return stats;
    }
}
