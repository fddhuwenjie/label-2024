package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookingId;
    private Long userId;
    private Long counselorId;
    private BigDecimal rating;  // 1-5分
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private Booking booking;
}
