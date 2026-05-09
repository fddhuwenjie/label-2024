package com.psychology.booking.service;

import com.psychology.booking.entity.TimeSlot;
import com.psychology.booking.exception.BusinessException;
import com.psychology.booking.mapper.TimeSlotMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeSlotService 单元测试")
class TimeSlotServiceTest {

    @Mock
    private TimeSlotMapper timeSlotMapper;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setCounselorId(1L);
        testTimeSlot.setDate(LocalDate.now().plusDays(1));
        testTimeSlot.setStartTime("09:00");
        testTimeSlot.setEndTime("10:00");
        testTimeSlot.setStatus(0);
    }

    @Test
    @DisplayName("根据咨询师和日期获取时间段")
    void listByCounselorAndDate_Success() {
        when(timeSlotMapper.selectList(any())).thenReturn(Arrays.asList(testTimeSlot));

        List<TimeSlot> result = timeSlotService.listByCounselorAndDate(1L, LocalDate.now().plusDays(1));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("09:00", result.get(0).getStartTime());
    }

    @Test
    @DisplayName("根据咨询师和日期获取时间段 - 空列表")
    void listByCounselorAndDate_Empty() {
        when(timeSlotMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<TimeSlot> result = timeSlotService.listByCounselorAndDate(1L, LocalDate.now().plusDays(1));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取咨询师所有时间段")
    void listByCounselor_Success() {
        TimeSlot slot2 = new TimeSlot();
        slot2.setId(2L);
        slot2.setCounselorId(1L);
        slot2.setDate(LocalDate.now().plusDays(2));
        slot2.setStartTime("14:00");
        slot2.setEndTime("15:00");
        slot2.setStatus(0);

        when(timeSlotMapper.selectList(any())).thenReturn(Arrays.asList(testTimeSlot, slot2));

        List<TimeSlot> result = timeSlotService.listByCounselor(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("创建时间段成功")
    void create_Success() {
        when(timeSlotMapper.selectOne(any())).thenReturn(null);
        when(timeSlotMapper.insert(any())).thenReturn(1);

        TimeSlot newSlot = new TimeSlot();
        newSlot.setCounselorId(1L);
        newSlot.setDate(LocalDate.now().plusDays(1));
        newSlot.setStartTime("10:00");
        newSlot.setEndTime("11:00");

        TimeSlot result = timeSlotService.create(newSlot);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(timeSlotMapper).insert(any());
    }

    @Test
    @DisplayName("创建时间段失败 - 时间段已存在")
    void create_AlreadyExists() {
        when(timeSlotMapper.selectOne(any())).thenReturn(testTimeSlot);

        TimeSlot newSlot = new TimeSlot();
        newSlot.setCounselorId(1L);
        newSlot.setDate(LocalDate.now().plusDays(1));
        newSlot.setStartTime("09:00");
        newSlot.setEndTime("10:00");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> timeSlotService.create(newSlot));

        assertEquals("该时间段已存在", exception.getMessage());
    }

    @Test
    @DisplayName("批量创建时间段")
    void batchCreate_Success() {
        when(timeSlotMapper.selectOne(any())).thenReturn(null);
        when(timeSlotMapper.insert(any())).thenReturn(1);

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        List<String> slots = Arrays.asList("09:00-10:00", "10:00-11:00");

        timeSlotService.batchCreate(1L, startDate, endDate, slots, 0);

        // 3天 * 2个时间段 = 6次插入
        verify(timeSlotMapper, times(6)).insert(any());
    }

    @Test
    @DisplayName("批量创建时间段 - 跳过已存在的")
    void batchCreate_SkipExisting() {
        when(timeSlotMapper.selectOne(any()))
                .thenReturn(testTimeSlot)  // 第一个已存在
                .thenReturn(null);         // 其他不存在
        when(timeSlotMapper.insert(any())).thenReturn(1);

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
        List<String> slots = Arrays.asList("09:00-10:00", "10:00-11:00");

        timeSlotService.batchCreate(1L, startDate, endDate, slots, 0);

        // 只有1个时间段被插入（另一个已存在）
        verify(timeSlotMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("更新时间段状态")
    void updateStatus_Success() {
        when(timeSlotMapper.selectById(1L)).thenReturn(testTimeSlot);

        timeSlotService.updateStatus(1L, 1);

        verify(timeSlotMapper).updateById(any());
    }

    @Test
    @DisplayName("更新时间段状态 - 时间段不存在")
    void updateStatus_NotFound() {
        when(timeSlotMapper.selectById(999L)).thenReturn(null);

        timeSlotService.updateStatus(999L, 1);

        verify(timeSlotMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("删除时间段")
    void delete_Success() {
        timeSlotService.delete(1L);

        verify(timeSlotMapper).deleteById(1L);
    }
}
