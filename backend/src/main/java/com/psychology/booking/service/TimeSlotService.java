package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotMapper timeSlotMapper;

    public List<TimeSlot> listByCounselorAndDate(Long counselorId, LocalDate date) {
        return timeSlotMapper.selectList(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, counselorId)
                .eq(TimeSlot::getDate, date)
                .in(TimeSlot::getStatus, 0, 2) // 返回可预约和不可用的，用于前端判断
                .orderByAsc(TimeSlot::getStartTime)
        );
    }

    public List<TimeSlot> listByCounselor(Long counselorId) {
        return timeSlotMapper.selectList(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, counselorId)
                .ge(TimeSlot::getDate, LocalDate.now())
                .eq(TimeSlot::getStatus, 0) // 只返回可预约的
                .orderByAsc(TimeSlot::getDate)
                .orderByAsc(TimeSlot::getStartTime)
        );
    }

    // 咨询师查看自己所有时间槽（包括已预约的）
    public List<TimeSlot> listAllByCounselor(Long counselorId) {
        return timeSlotMapper.selectList(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, counselorId)
                .ge(TimeSlot::getDate, LocalDate.now())
                .orderByAsc(TimeSlot::getDate)
                .orderByAsc(TimeSlot::getStartTime)
        );
    }

    @Transactional
    public TimeSlot create(TimeSlot timeSlot) {
        log.info("Creating time slot for counselor: {}", timeSlot.getCounselorId());
        
        // 检查是否有重叠的时间段
        TimeSlot existing = timeSlotMapper.selectOne(
            new LambdaQueryWrapper<TimeSlot>()
                .eq(TimeSlot::getCounselorId, timeSlot.getCounselorId())
                .eq(TimeSlot::getDate, timeSlot.getDate())
                .eq(TimeSlot::getStartTime, timeSlot.getStartTime())
        );
        if (existing != null) {
            throw new BusinessException("该时间段已存在");
        }
        
        timeSlot.setStatus(0);
        timeSlotMapper.insert(timeSlot);
        return timeSlot;
    }

    @Transactional
    public void batchCreate(Long counselorId, LocalDate startDate, LocalDate endDate, 
                           List<String> timeSlots, Integer status) {
        log.info("Batch creating time slots for counselor: {} from {} to {} with status {}", 
                 counselorId, startDate, endDate, status);
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            for (String slot : timeSlots) {
                String[] parts = slot.split("-");
                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setCounselorId(counselorId);
                timeSlot.setDate(current);
                timeSlot.setStartTime(parts[0]);
                timeSlot.setEndTime(parts[1]);
                timeSlot.setStatus(status != null ? status : 0);
                
                // 检查是否已存在
                TimeSlot existing = timeSlotMapper.selectOne(
                    new LambdaQueryWrapper<TimeSlot>()
                        .eq(TimeSlot::getCounselorId, counselorId)
                        .eq(TimeSlot::getDate, current)
                        .eq(TimeSlot::getStartTime, parts[0])
                );
                if (existing == null) {
                    timeSlotMapper.insert(timeSlot);
                } else if (status != null) {
                    // 如果已存在且指定了状态，更新状态
                    existing.setStatus(status);
                    timeSlotMapper.updateById(existing);
                }
            }
            current = current.plusDays(1);
        }
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        TimeSlot timeSlot = timeSlotMapper.selectById(id);
        if (timeSlot != null) {
            timeSlot.setStatus(status);
            timeSlotMapper.updateById(timeSlot);
        }
    }

    @Transactional
    public void delete(Long id) {
        // 使用物理删除，避免唯一索引冲突
        timeSlotMapper.physicalDeleteById(id);
    }
}
