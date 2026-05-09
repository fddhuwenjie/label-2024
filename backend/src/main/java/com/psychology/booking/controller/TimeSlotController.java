package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.service.CounselorService;
import com.psychology.booking.service.TimeSlotService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    @GetMapping("/counselor/{counselorId}")
    public Result<List<TimeSlot>> listByCounselor(@PathVariable Long counselorId) {
        return Result.success(timeSlotService.listByCounselor(counselorId));
    }

    // 管理员查看咨询师所有时段（包括已预约和不可用的）
    @GetMapping("/counselor/{counselorId}/all")
    public Result<List<TimeSlot>> listAllByCounselorForAdmin(@PathVariable Long counselorId) {
        return Result.success(timeSlotService.listAllByCounselor(counselorId));
    }

    @GetMapping("/counselor/{counselorId}/date")
    public Result<List<TimeSlot>> listByCounselorAndDate(
            @PathVariable Long counselorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(timeSlotService.listByCounselorAndDate(counselorId, date));
    }

    // 咨询师获取自己的所有时间槽（包括已预约的）
    @GetMapping("/my")
    public Result<List<TimeSlot>> myTimeSlots(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor == null) {
            return Result.success(List.of());
        }
        return Result.success(timeSlotService.listAllByCounselor(counselor.getId()));
    }

    @PostMapping
    public Result<TimeSlot> create(@Valid @RequestBody TimeSlot timeSlot,
                                   @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor != null) {
            timeSlot.setCounselorId(counselor.getId());
        }
        return Result.success(timeSlotService.create(timeSlot));
    }

    // 咨询师批量创建自己的时间槽
    @PostMapping("/my/batch")
    public Result<Void> myBatchCreate(@RequestBody Map<String, Object> params,
                                      @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        if (counselor == null) {
            return Result.error("您不是咨询师");
        }
        
        LocalDate startDate = LocalDate.parse(params.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(params.get("endDate").toString());
        @SuppressWarnings("unchecked")
        List<String> timeSlots = (List<String>) params.get("timeSlots");
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 0;
        
        timeSlotService.batchCreate(counselor.getId(), startDate, endDate, timeSlots, status);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchCreate(@RequestBody Map<String, Object> params,
                                    @RequestHeader("Authorization") String token) {
        Long counselorId = Long.valueOf(params.get("counselorId").toString());
        LocalDate startDate = LocalDate.parse(params.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(params.get("endDate").toString());
        @SuppressWarnings("unchecked")
        List<String> timeSlots = (List<String>) params.get("timeSlots");
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 0;
        
        timeSlotService.batchCreate(counselorId, startDate, endDate, timeSlots, status);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        timeSlotService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timeSlotService.delete(id);
        return Result.success();
    }
}
