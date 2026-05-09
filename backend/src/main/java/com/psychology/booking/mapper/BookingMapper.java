package com.psychology.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.booking.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
