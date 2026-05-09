package com.psychology.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.booking.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
