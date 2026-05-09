package com.psychology.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.booking.entity.TimeSlot;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {
    
    // 物理删除，绕过软删除
    @Delete("DELETE FROM time_slot WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
