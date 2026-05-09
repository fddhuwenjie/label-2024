package com.psychology.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.psychology.booking.entity.Counselor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CounselorMapper extends BaseMapper<Counselor> {
    @Select("SELECT c.*, u.real_name, u.avatar, u.phone FROM counselor c " +
            "LEFT JOIN user u ON c.user_id = u.id WHERE c.deleted = 0 AND c.available = 1")
    List<Counselor> selectWithUser();
}
