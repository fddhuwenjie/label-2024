package com.psychology.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.User;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorService {
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;

    public List<Counselor> list() {
        List<Counselor> counselors = counselorMapper.selectList(
            new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getAvailable, 1)
                .orderByDesc(Counselor::getRating)
        );
        counselors.forEach(c -> c.setUser(userMapper.selectById(c.getUserId())));
        return counselors;
    }

    public Counselor getById(Long id) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor != null) {
            counselor.setUser(userMapper.selectById(counselor.getUserId()));
        }
        return counselor;
    }

    public Counselor getByUserId(Long userId) {
        return counselorMapper.selectOne(
            new LambdaQueryWrapper<Counselor>().eq(Counselor::getUserId, userId)
        );
    }

    public void save(Counselor counselor) {
        if (counselor.getId() == null) {
            counselorMapper.insert(counselor);
        } else {
            counselorMapper.updateById(counselor);
        }
    }

    public List<Counselor> listAll() {
        List<Counselor> counselors = counselorMapper.selectList(
            new LambdaQueryWrapper<Counselor>().orderByDesc(Counselor::getCreatedAt)
        );
        counselors.forEach(c -> c.setUser(userMapper.selectById(c.getUserId())));
        return counselors;
    }

    public void updateAvailable(Long id, Integer available) {
        Counselor counselor = new Counselor();
        counselor.setId(id);
        counselor.setAvailable(available);
        counselorMapper.updateById(counselor);
    }

    public void delete(Long id) {
        counselorMapper.deleteById(id);
    }
}
