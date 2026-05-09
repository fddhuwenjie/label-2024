package com.psychology.booking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookingId;
    private Long userId;
    private String orderNo;       // 订单号
    private BigDecimal amount;
    private Integer status;       // 0-待支付, 1-已支付, 2-已退款, 3-支付失败
    private String paymentMethod; // alipay, wechat, mock
    private String transactionId; // 第三方交易号
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private Booking booking;
}
