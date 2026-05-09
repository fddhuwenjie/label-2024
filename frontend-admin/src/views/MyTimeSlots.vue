<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>时间管理</h1>
        <p>设置您的可预约时间段</p>
      </div>
      <el-button type="primary" @click="showBatchDialog = true">
        <el-icon><Plus /></el-icon>批量添加时间
      </el-button>
    </div>

    <div class="default-slots-info">
      <el-icon><InfoFilled /></el-icon>
      <span>默认时段（未设置时用户看到的选项）：</span>
      <span class="slot-tag" v-for="slot in defaultTimeSlots" :key="slot">{{ slot }}</span>
    </div>

    <div class="timeslots-container" v-loading="loading">
      <el-empty v-if="groupedSlots.length === 0" description="暂无可预约时间，请添加时间段" />
      
      <div v-for="group in groupedSlots" :key="group.date" class="date-group">
        <div class="date-header">
          <span class="date-text">{{ formatDate(group.date) }}</span>
          <span class="weekday">{{ getWeekday(group.date) }}</span>
          <el-tag v-if="!isWithin30Days(group.date)" type="info" size="small">不可编辑</el-tag>
        </div>
        <div class="slots-grid">
          <div v-for="slot in group.slots" :key="slot.id" 
               class="slot-item" :class="{ booked: slot.status === 1, disabled: slot.status === 2 }">
            <span class="time">{{ slot.startTime }} - {{ slot.endTime }}</span>
            <span class="status">{{ slotStatusText(slot.status) }}</span>
            <el-button v-if="slot.status === 0 && isWithin30Days(group.date)" type="danger" size="small" text @click="deleteSlot(slot.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showBatchDialog" title="批量添加可预约时间" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-checkbox-group v-model="batchForm.timeSlots" class="time-checkbox">
            <el-checkbox v-for="slot in defaultTimeSlots" :key="slot" :label="slot">{{ slot }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBatch" :loading="submitting">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Delete, InfoFilled } from '@element-plus/icons-vue'
import { timeSlots as timeSlotsApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const allSlots = ref([])
const showBatchDialog = ref(false)

const defaultTimeSlots = [
  '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

const batchForm = ref({
  dateRange: [],
  timeSlots: ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00']
})

// 计算30天后的日期
const maxDate = new Date()
maxDate.setDate(maxDate.getDate() + 30)
maxDate.setHours(23, 59, 59, 999)

const isWithin30Days = (date) => {
  const d = new Date(date)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d >= today && d <= maxDate
}

const groupedSlots = computed(() => {
  const groups = {}
  allSlots.value.forEach(slot => {
    if (!groups[slot.date]) groups[slot.date] = []
    groups[slot.date].push(slot)
  })
  return Object.keys(groups).sort().map(date => ({ date, slots: groups[date] }))
})

const formatDate = (date) => new Date(date).toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
const getWeekday = (date) => ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(date).getDay()]
const slotStatusText = (status) => ({ 0: '可预约', 1: '已预约', 2: '不可用' }[status] || '未知')

// 日期选择器限制：只能选今天到7天后
const disabledDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today || date > maxDate
}

const fetchSlots = async () => {
  loading.value = true
  try {
    const res = await timeSlotsApi.my()
    if (res.code === 200) allSlots.value = res.data
  } finally {
    loading.value = false
  }
}

const submitBatch = async () => {
  if (!batchForm.value.dateRange?.length || !batchForm.value.timeSlots?.length) {
    ElMessage.warning('请选择日期范围和时间段')
    return
  }
  submitting.value = true
  try {
    await timeSlotsApi.myBatchCreate({
      startDate: batchForm.value.dateRange[0],
      endDate: batchForm.value.dateRange[1],
      timeSlots: batchForm.value.timeSlots
    })
    ElMessage.success('时间段添加成功')
    showBatchDialog.value = false
    fetchSlots()
  } finally {
    submitting.value = false
  }
}

const deleteSlot = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此时间段？', '删除确认', { type: 'warning' })
    await timeSlotsApi.delete(id)
    ElMessage.success('已删除')
    fetchSlots()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(fetchSlots)
</script>

<style scoped>
.page { padding: 32px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }

.default-slots-info { 
  display: flex; 
  align-items: center; 
  flex-wrap: wrap;
  gap: 8px; 
  padding: 12px 16px; 
  background: #f0f9ff; 
  border: 1px solid #bae6fd; 
  border-radius: 10px; 
  margin-bottom: 16px; 
  font-size: 14px; 
  color: #0369a1; 
}
.default-slots-info .el-icon { font-size: 16px; }
.slot-tag { 
  padding: 4px 10px; 
  background: white; 
  border-radius: 6px; 
  font-size: 13px; 
  color: #0284c7; 
  border: 1px solid #7dd3fc; 
}

.date-group {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
}

.date-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.date-text { font-size: 16px; font-weight: 600; color: #0f172a; }
.weekday { font-size: 14px; color: #64748b; }

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.slot-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
}

.slot-item.booked { background: #fef3c7; border-color: #fcd34d; }
.slot-item.disabled { background: #f1f5f9; border-color: #e2e8f0; }
.slot-item .time { font-weight: 500; color: #0f172a; }
.slot-item .status { font-size: 12px; color: #64748b; }

.time-checkbox { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
</style>
