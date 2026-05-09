package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.dto.ReviewRequest;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.Review;
import com.psychology.booking.service.CounselorService;
import com.psychology.booking.service.ReviewService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    // 管理员接口：获取所有评价
    @GetMapping
    public Result<List<Review>> listAll() {
        return Result.success(reviewService.listAll());
    }

    @GetMapping("/counselor/{counselorId}")
    public Result<List<Review>> listByCounselor(@PathVariable Long counselorId) {
        return Result.success(reviewService.listByCounselor(counselorId));
    }

    // 咨询师获取自己的评价
    @GetMapping("/my")
    public Result<List<Review>> myReviews(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor == null) {
            return Result.success(List.of());
        }
        return Result.success(reviewService.listByCounselor(counselor.getId()));
    }

    @PostMapping
    public Result<Review> create(@Valid @RequestBody ReviewRequest request,
                                 @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        
        Review review = new Review();
        review.setBookingId(request.getBookingId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        
        return Result.success(reviewService.create(review, userId));
    }

    // 管理员接口：删除评价
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.success();
    }
}
