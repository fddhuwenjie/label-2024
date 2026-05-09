package com.psychology.booking.service;

import com.psychology.booking.entity.Counselor;
import com.psychology.booking.entity.User;
import com.psychology.booking.mapper.CounselorMapper;
import com.psychology.booking.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CounselorService 单元测试")
class CounselorServiceTest {

    @Mock
    private CounselorMapper counselorMapper;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CounselorService counselorService;

    private Counselor testCounselor;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("counselor1");
        testUser.setRealName("张医生");

        testCounselor = new Counselor();
        testCounselor.setId(1L);
        testCounselor.setUserId(1L);
        testCounselor.setTitle("资深心理咨询师");
        testCounselor.setSpecialty("焦虑,抑郁,婚姻");
        testCounselor.setExperienceYears(10);
        testCounselor.setPrice(new BigDecimal("500"));
        testCounselor.setRating(new BigDecimal("4.8"));
        testCounselor.setAvailable(1);
    }

    @Test
    @DisplayName("获取可用咨询师列表")
    void list_Success() {
        when(counselorMapper.selectList(any())).thenReturn(Arrays.asList(testCounselor));
        when(userMapper.selectById(anyLong())).thenReturn(testUser);

        List<Counselor> result = counselorService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());
        verify(counselorMapper).selectList(any());
    }

    @Test
    @DisplayName("获取可用咨询师列表 - 空列表")
    void list_Empty() {
        when(counselorMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Counselor> result = counselorService.list();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("根据ID获取咨询师")
    void getById_Success() {
        when(counselorMapper.selectById(1L)).thenReturn(testCounselor);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        Counselor result = counselorService.getById(1L);

        assertNotNull(result);
        assertEquals(testCounselor.getId(), result.getId());
        assertEquals(testUser, result.getUser());
    }

    @Test
    @DisplayName("根据ID获取咨询师 - 不存在")
    void getById_NotFound() {
        when(counselorMapper.selectById(999L)).thenReturn(null);

        Counselor result = counselorService.getById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("根据用户ID获取咨询师")
    void getByUserId_Success() {
        when(counselorMapper.selectOne(any())).thenReturn(testCounselor);

        Counselor result = counselorService.getByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("保存新咨询师")
    void save_Insert() {
        Counselor newCounselor = new Counselor();
        newCounselor.setUserId(2L);
        newCounselor.setTitle("初级咨询师");

        counselorService.save(newCounselor);

        verify(counselorMapper).insert(newCounselor);
        verify(counselorMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("更新咨询师信息")
    void save_Update() {
        testCounselor.setTitle("高级心理咨询师");

        counselorService.save(testCounselor);

        verify(counselorMapper).updateById(testCounselor);
        verify(counselorMapper, never()).insert(any());
    }

    @Test
    @DisplayName("获取所有咨询师列表（管理端）")
    void listAll_Success() {
        Counselor unavailableCounselor = new Counselor();
        unavailableCounselor.setId(2L);
        unavailableCounselor.setUserId(2L);
        unavailableCounselor.setAvailable(0);

        when(counselorMapper.selectList(any())).thenReturn(Arrays.asList(testCounselor, unavailableCounselor));
        when(userMapper.selectById(anyLong())).thenReturn(testUser);

        List<Counselor> result = counselorService.listAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("更新咨询师可用状态")
    void updateAvailable_Success() {
        counselorService.updateAvailable(1L, 0);

        verify(counselorMapper).updateById(any());
    }

    @Test
    @DisplayName("删除咨询师")
    void delete_Success() {
        counselorService.delete(1L);

        verify(counselorMapper).deleteById(1L);
    }
}
