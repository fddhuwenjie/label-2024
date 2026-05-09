package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.dto.BookingRequest;
import com.psychology.booking.entity.Booking;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.service.BookingService;
import com.psychology.booking.service.CounselorService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    @GetMapping("/my")
    public Result<List<Booking>> myBookings(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(bookingService.listByUserId(userId));
    }

    // 咨询师查看自己收到的预约
    @GetMapping("/counselor")
    public Result<List<Booking>> counselorBookings(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor == null) {
            return Result.success(List.of());
        }
        return Result.success(bookingService.listByCounselorId(counselor.getId()));
    }

    // 咨询师确认预约
    @PutMapping("/{id}/confirm")
    public Result<Void> confirmBooking(@PathVariable Long id, 
                                       @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        bookingService.confirmByCounselor(id, userId);
        return Result.success();
    }

    // 咨询师完成预约
    @PutMapping("/{id}/complete")
    public Result<Void> completeBooking(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        bookingService.completeByCounselor(id, userId);
        return Result.success();
    }

    // 咨询师拒绝预约
    @PutMapping("/{id}/reject")
    public Result<Void> rejectBooking(@PathVariable Long id,
                                      @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        bookingService.rejectByCounselor(id, userId);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<List<Booking>> allBookings() {
        return Result.success(bookingService.listAll());
    }

    @PostMapping
    public Result<Booking> create(@Valid @RequestBody BookingRequest request, 
                                  @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setCounselorId(request.getCounselorId());
        booking.setBookingDate(request.getBookingDate());
        booking.setTimeSlot(request.getTimeSlot());
        booking.setNotes(request.getNotes());
        
        return Result.success(bookingService.create(booking));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bookingService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id, 
                               @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        bookingService.cancel(id, userId);
        return Result.success();
    }
}
