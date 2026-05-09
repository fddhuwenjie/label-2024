package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("counselor")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String specialty;
    private String introduction;
    private Integer experienceYears;
    private BigDecimal price;
    private BigDecimal rating;
    private Integer bookingCount;
    private Integer available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private User user;
}
