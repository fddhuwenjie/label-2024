package com.psychology.booking.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull(message = "咨询师ID不能为空")
    private Long counselorId;
    
    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;
    
    @NotNull(message = "时间段不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):([0-5][0-9])-([01]?[0-9]|2[0-3]):([0-5][0-9])$", 
             message = "时间段格式不正确，应为HH:mm-HH:mm")
    private String timeSlot;
    
    @Size(max = 500, message = "备注不能超过500字")
    private String notes;
}
