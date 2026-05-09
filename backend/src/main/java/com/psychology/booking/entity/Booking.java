package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("booking")
public class Booking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long counselorId;
    private LocalDate bookingDate;
    private String timeSlot;
    private Integer status; // 0-pending, 1-confirmed, 2-completed, 3-cancelled
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private Counselor counselor;
}
