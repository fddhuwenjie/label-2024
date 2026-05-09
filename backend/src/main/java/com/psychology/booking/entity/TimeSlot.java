package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("time_slot")
public class TimeSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long counselorId;
    private LocalDate date;
    private String startTime; // HH:mm
    private String endTime;   // HH:mm
    private Integer status;   // 0-可预约, 1-已预约, 2-不可用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private Counselor counselor;
}
