package com.psychology.booking.controller;

import com.psychology.booking.common.Result;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.service.CounselorService;
import com.psychology.booking.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorController {
    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public Result<List<Counselor>> list() {
        return Result.success(counselorService.list());
    }

    // 管理员接口：获取所有咨询师（包括下架的）
    @GetMapping("/all")
    public Result<List<Counselor>> listAll() {
        return Result.success(counselorService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Counselor> getById(@PathVariable Long id) {
        return Result.success(counselorService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Counselor counselor) {
        counselorService.save(counselor);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Counselor counselor) {
        counselor.setId(id);
        counselorService.save(counselor);
        return Result.success();
    }

    // 管理员接口：上下架咨询师
    @PutMapping("/{id}/available")
    public Result<Void> updateAvailable(@PathVariable Long id, @RequestParam Integer available) {
        counselorService.updateAvailable(id, available);
        return Result.success();
    }

    // 咨询师获取自己的资料
    @GetMapping("/me")
    public Result<Counselor> getMe(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor counselor = counselorService.getByUserId(userId);
        return Result.success(counselor);
    }

    // 咨询师更新自己的资料
    @PutMapping("/me")
    public Result<Void> updateMe(@RequestBody Counselor counselor,
                                 @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Counselor existing = counselorService.getByUserId(userId);
        if (existing == null) {
            return Result.error("您不是咨询师");
        }
        // 只允许更新部分字段
        existing.setTitle(counselor.getTitle());
        existing.setSpecialty(counselor.getSpecialty());
        existing.setIntroduction(counselor.getIntroduction());
        existing.setExperienceYears(counselor.getExperienceYears());
        existing.setPrice(counselor.getPrice());
        counselorService.save(existing);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        counselorService.delete(id);
        return Result.success();
    }
}
