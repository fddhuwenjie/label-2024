package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.service.CounselorService;
import com.psychology.booking.service.StatsService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/bookings/trend")
    public Result<Map<String, Object>> getBookingTrend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(statsService.getBookingTrend(days));
    }

    // 咨询师个人统计
    @GetMapping("/counselor/my")
    public Result<Map<String, Object>> getMyCounselorStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor == null) {
            return Result.error("您不是咨询师");
        }
        return Result.success(statsService.getCounselorStats(counselor.getId()));
    }
}
