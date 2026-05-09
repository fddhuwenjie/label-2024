package com.psychology.booking.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ReviewRequest {
    @NotNull(message = "预约ID不能为空")
    private Long bookingId;
    
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低为1分")
    @DecimalMax(value = "5.0", message = "评分最高为5分")
    private BigDecimal rating;
    
    @Size(max = 1000, message = "评价内容不能超过1000字")
    private String content;
}
