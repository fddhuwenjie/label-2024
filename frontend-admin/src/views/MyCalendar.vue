<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>日程管理</h1>
        <p>直观管理您的可预约时段和预约</p>
      </div>
      <div class="header-actions">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button value="timeGridWeek">周视图</el-radio-button>
          <el-radio-button value="dayGridMonth">月视图</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="showBatchDialog = true">
          <el-icon><Plus /></el-icon>批量添加时间
        </el-button>
      </div>
    </div>

    <div class="legend-bar">
      <div class="legend-item">
        <span class="legend-dot available"></span>
        <span>可预约</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot booked"></span>
        <span>已预约</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot completed"></span>
        <span>已完成</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot pending"></span>
        <span>待确认</span>
      </div>
    </div>

    <div class="calendar-container" v-loading="loading">
      <FullCalendar
        ref="calendarRef"
        :options="calendarOptions"
      />
    </div>

    <el-dialog v-model="showSlotDialog" title="时间段详情" width="500px">
      <div v-if="currentSlot" class="slot-detail">
        <div class="detail-row">
          <span class="label">日期：</span>
          <span class="value">{{ currentSlot.extendedProps.date }}</span>
        </div>
        <div class="detail-row">
          <span class="label">时间：</span>
          <span class="value">{{ currentSlot.extendedProps.startTime }} - {{ currentSlot.extendedProps.endTime }}</span>
        </div>
        <div class="detail-row">
          <span class="label">状态：</span>
          <el-tag :type="slotStatusType(currentSlot.extendedProps.status)">
            {{ slotStatusText(currentSlot.extendedProps.status) }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="showSlotDialog = false">关闭</el-button>
        <el-button 
          v-if="currentSlot?.extendedProps?.status === 0 && isWithin30Days(currentSlot.extendedProps.date)" 
          type="danger" 
          @click="deleteCurrentSlot"
        >
          删除时段
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBookingDialog" title="预约详情" width="600px">
      <div v-if="currentBooking" class="booking-detail">
        <div class="user-section">
          <div class="user-avatar">{{ (currentBooking.user?.realName || currentBooking.user?.username || 'U').charAt(0) }}</div>
          <div class="user-info">
            <div class="user-name">{{ currentBooking.user?.realName || currentBooking.user?.username }}</div>
            <div class="user-phone">{{ currentBooking.user?.phone || '未填写电话' }}</div>
          </div>
          <el-tag :type="bookingStatusType(currentBooking.status)" size="large">
            {{ bookingStatusText(currentBooking.status) }}
          </el-tag>
        </div>
        
        <div class="info-section">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentBooking.bookingDate }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ currentBooking.timeSlot }}</span>
          </div>
        </div>

        <div v-if="currentBooking.notes" class="notes-section">
          <div class="notes-label">备注信息</div>
          <div class="notes-content">{{ currentBooking.notes }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showBookingDialog = false">关闭</el-button>
        <template v-if="currentBooking?.status === 0">
          <el-button type="danger" plain @click="rejectCurrentBooking">拒绝</el-button>
          <el-button type="primary" @click="confirmCurrentBooking">确认预约</el-button>
        </template>
        <el-button v-if="currentBooking?.status === 1" type="success" @click="completeCurrentBooking">
          标记完成
        </el-button>
      </template>
    </el-dialog>

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

    <el-dialog v-model="showCreateDialog" title="创建可预约时段" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="日期">
          <el-date-picker
            v-model="createForm.date"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="createForm.startTime"
            start="09:00"
            step="01:00"
            end="17:00"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="createForm.endTime"
            start="10:00"
            step="01:00"
            end="18:00"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="creating">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Plus, Calendar, Clock } from '@element-plus/icons-vue'
import { timeSlots as timeSlotsApi, bookings as bookingsApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

const loading = ref(false)
const submitting = ref(false)
const creating = ref(false)
const calendarRef = ref(null)
const viewMode = ref('timeGridWeek')

const allSlots = ref([])
const allBookings = ref([])

const showSlotDialog = ref(false)
const showBookingDialog = ref(false)
const showBatchDialog = ref(false)
const showCreateDialog = ref(false)

const currentSlot = ref(null)
const currentBooking = ref(null)

const defaultTimeSlots = [
  '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

const batchForm = ref({
  dateRange: [],
  timeSlots: ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00']
})

const createForm = ref({
  date: '',
  startTime: '09:00',
  endTime: '10:00'
})

const maxDate = new Date()
maxDate.setDate(maxDate.getDate() + 30)
maxDate.setHours(23, 59, 59, 999)

const isWithin30Days = (date) => {
  const d = new Date(date)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d >= today && d <= maxDate
}

const disabledDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today || date > maxDate
}

const slotStatusText = (status) => ({ 0: '可预约', 1: '已预约', 2: '不可用' }[status] || '未知')
const slotStatusType = (status) => ({ 0: 'success', 1: 'warning', 2: 'info' }[status] || 'info')

const bookingStatusText = (status) => ({ 0: '待处理', 1: '已确认', 2: '已完成', 3: '已取消' }[status] || '未知')
const bookingStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')

const calendarEvents = computed(() => {
  const events = []
  
  allSlots.value.forEach(slot => {
    const [startHour, startMin] = slot.startTime.split(':').map(Number)
    const [endHour, endMin] = slot.endTime.split(':').map(Number)
    
    let backgroundColor, borderColor
    if (slot.status === 0) {
      backgroundColor = '#10b981'
      borderColor = '#059669'
    } else if (slot.status === 1) {
      backgroundColor = '#f59e0b'
      borderColor = '#d97706'
    } else {
      backgroundColor = '#94a3b8'
      borderColor = '#64748b'
    }
    
    events.push({
      id: `slot-${slot.id}`,
      title: `${slot.startTime}-${slot.endTime}`,
      start: `${slot.date}T${String(startHour).padStart(2, '0')}:${String(startMin).padStart(2, '0')}:00`,
      end: `${slot.date}T${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}:00`,
      backgroundColor,
      borderColor,
      extendedProps: {
        type: 'slot',
        slotId: slot.id,
        date: slot.date,
        startTime: slot.startTime,
        endTime: slot.endTime,
        status: slot.status
      }
    })
  })
  
  allBookings.value.forEach(booking => {
    if (booking.timeSlot) {
      const [startTime, endTime] = booking.timeSlot.split('-')
      if (startTime && endTime) {
        const [startHour, startMin] = startTime.split(':').map(Number)
        const [endHour, endMin] = endTime.split(':').map(Number)
        
        let backgroundColor, borderColor
        if (booking.status === 0) {
          backgroundColor = '#f59e0b'
          borderColor = '#d97706'
        } else if (booking.status === 1) {
          backgroundColor = '#3b82f6'
          borderColor = '#2563eb'
        } else if (booking.status === 2) {
          backgroundColor = '#94a3b8'
          borderColor = '#64748b'
        } else {
          backgroundColor = '#ef4444'
          borderColor = '#dc2626'
        }
        
        events.push({
          id: `booking-${booking.id}`,
          title: booking.user?.realName || booking.user?.username || '预约',
          start: `${booking.bookingDate}T${String(startHour).padStart(2, '0')}:${String(startMin).padStart(2, '0')}:00`,
          end: `${booking.bookingDate}T${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}:00`,
          backgroundColor,
          borderColor,
          extendedProps: {
            type: 'booking',
            bookingId: booking.id,
            booking: booking
          }
        })
      }
    }
  })
  
  return events
})

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: viewMode.value,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: ''
  },
  locale: 'zh-cn',
  timeZone: 'local',
  slotMinTime: '08:00:00',
  slotMaxTime: '20:00:00',
  allDaySlot: false,
  editable: true,
  selectable: true,
  selectMirror: true,
  dayMaxEvents: true,
  events: calendarEvents.value,
  eventClick: handleEventClick,
  select: handleSelect,
  eventDrop: handleEventDrop,
  height: 'auto'
}))

watch(viewMode, (newView) => {
  if (calendarRef.value) {
    const calendarApi = calendarRef.value.getApi()
    calendarApi.changeView(newView)
  }
})

const handleEventClick = (info) => {
  const { type, slotId, bookingId, booking } = info.event.extendedProps
  
  if (type === 'slot') {
    const slot = allSlots.value.find(s => s.id === slotId)
    if (slot) {
      currentSlot.value = info.event
      showSlotDialog.value = true
    }
  } else if (type === 'booking') {
    currentBooking.value = booking
    showBookingDialog.value = true
  }
}

const handleSelect = (info) => {
  const startDate = info.startStr.split('T')[0]
  
  if (!isWithin30Days(startDate)) {
    ElMessage.warning('只能选择今天到30天内的日期')
    return
  }
  
  const startTime = info.start.toTimeString().slice(0, 5)
  const endTime = info.end.toTimeString().slice(0, 5)
  
  createForm.value = {
    date: startDate,
    startTime: startTime,
    endTime: endTime
  }
  
  showCreateDialog.value = true
}

const handleEventDrop = async (info) => {
  const { type, slotId } = info.event.extendedProps
  
  if (type !== 'slot') {
    info.revert()
    ElMessage.warning('只能拖拽可预约时段')
    return
  }
  
  const slot = allSlots.value.find(s => s.id === slotId)
  if (!slot || slot.status !== 0) {
    info.revert()
    ElMessage.warning('只能拖拽可预约状态的时段')
    return
  }
  
  const newDate = info.event.startStr.split('T')[0]
  
  if (!isWithin30Days(newDate)) {
    info.revert()
    ElMessage.warning('只能移动到今天到30天内的日期')
    return
  }
  
  const newStartTime = info.event.start.toTimeString().slice(0, 5)
  const newEndTime = info.event.end.toTimeString().slice(0, 5)
  
  try {
    await ElMessageBox.confirm('确认移动此时间段？', '移动确认', { type: 'info' })
    
    await timeSlotsApi.delete(slotId)
    await timeSlotsApi.create({
      date: newDate,
      startTime: newStartTime,
      endTime: newEndTime
    })
    
    ElMessage.success('时间段已移动')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error('移动失败')
    }
    info.revert()
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [slotsRes, bookingsRes] = await Promise.all([
      timeSlotsApi.my(),
      bookingsApi.counselorBookings()
    ])
    
    if (slotsRes.code === 200) allSlots.value = slotsRes.data
    if (bookingsRes.code === 200) allBookings.value = bookingsRes.data
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
    fetchData()
  } finally {
    submitting.value = false
  }
}

const submitCreate = async () => {
  if (!createForm.value.date || !createForm.value.startTime || !createForm.value.endTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  creating.value = true
  try {
    await timeSlotsApi.create({
      date: createForm.value.date,
      startTime: createForm.value.startTime,
      endTime: createForm.value.endTime
    })
    ElMessage.success('时间段创建成功')
    showCreateDialog.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

const deleteCurrentSlot = async () => {
  if (!currentSlot.value) return
  
  try {
    await ElMessageBox.confirm('确定删除此时间段？', '删除确认', { type: 'warning' })
    await timeSlotsApi.delete(currentSlot.value.extendedProps.slotId)
    ElMessage.success('已删除')
    showSlotDialog.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const confirmCurrentBooking = async () => {
  if (!currentBooking.value) return
  
  try {
    await ElMessageBox.confirm('确认接受此预约？', '确认预约')
    await bookingsApi.confirm(currentBooking.value.id)
    ElMessage.success('预约已确认')
    showBookingDialog.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const rejectCurrentBooking = async () => {
  if (!currentBooking.value) return
  
  try {
    await ElMessageBox.confirm('确定要拒绝此预约吗？', '拒绝预约', { type: 'warning' })
    await bookingsApi.reject(currentBooking.value.id)
    ElMessage.success('已拒绝预约')
    showBookingDialog.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const completeCurrentBooking = async () => {
  if (!currentBooking.value) return
  
  try {
    await ElMessageBox.confirm('确认此次咨询已完成？', '完成咨询')
    await bookingsApi.complete(currentBooking.value.id)
    ElMessage.success('咨询已完成')
    showBookingDialog.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page { padding: 32px; }
.page-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-start; 
  margin-bottom: 24px; 
}
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.legend-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-dot.available { background: #10b981; }
.legend-dot.booked { background: #f59e0b; }
.legend-dot.completed { background: #94a3b8; }
.legend-dot.pending { background: #3b82f6; }

.calendar-container {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.calendar-container :deep(.fc) {
  font-family: inherit;
}

.calendar-container :deep(.fc-toolbar-title) {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.calendar-container :deep(.fc-button) {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-weight: 500;
}

.calendar-container :deep(.fc-button:hover) {
  background: #e2e8f0;
  color: #0f172a;
}

.calendar-container :deep(.fc-button-primary:not(:disabled).fc-button-active) {
  background: #6366f1;
  border-color: #6366f1;
}

.calendar-container :deep(.fc-daygrid-day) {
  cursor: pointer;
}

.calendar-container :deep(.fc-day-today) {
  background: #f0f9ff !important;
}

.calendar-container :deep(.fc-event) {
  cursor: pointer;
  border-radius: 6px;
  padding: 2px 4px;
  font-size: 12px;
  border: none;
}

.calendar-container :deep(.fc-event-main) {
  padding: 2px 4px;
}

.calendar-container :deep(.fc-timegrid-slot) {
  height: 40px;
}

.calendar-container :deep(.fc-timegrid-slot-label) {
  font-size: 13px;
  color: #64748b;
}

.calendar-container :deep(.fc-col-header-cell) {
  background: #f8fafc;
  padding: 8px;
}

.calendar-container :deep(.fc-col-header-cell-cushion) {
  font-weight: 600;
  color: #475569;
}

.slot-detail, .booking-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-row .label {
  font-weight: 500;
  color: #64748b;
  min-width: 60px;
}

.detail-row .value {
  color: #0f172a;
  font-weight: 500;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #0f172a;
  font-size: 16px;
}

.user-phone {
  font-size: 13px;
  color: #64748b;
}

.info-section {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.notes-section {
  padding: 16px;
  background: #fef3c7;
  border-radius: 10px;
  border: 1px solid #fcd34d;
}

.notes-label {
  font-weight: 600;
  color: #92400e;
  margin-bottom: 8px;
  font-size: 14px;
}

.notes-content {
  color: #78350f;
  font-size: 14px;
}

.time-checkbox {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
</style>
