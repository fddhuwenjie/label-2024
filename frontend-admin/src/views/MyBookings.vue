<template>
  <div class="page">
    <div class="page-header">
      <h1>预约管理</h1>
      <p>管理您收到的预约请求</p>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="filterBookings">
        <el-radio-button :label="null">全部 ({{ allBookings.length }})</el-radio-button>
        <el-radio-button :label="0">待处理 ({{ countByStatus(0) }})</el-radio-button>
        <el-radio-button :label="1">已确认 ({{ countByStatus(1) }})</el-radio-button>
        <el-radio-button :label="2">已完成 ({{ countByStatus(2) }})</el-radio-button>
        <el-radio-button :label="3">已取消 ({{ countByStatus(3) }})</el-radio-button>
      </el-radio-group>
    </div>

    <div class="bookings-list" v-loading="loading">
      <el-empty v-if="filteredBookings.length === 0" description="暂无预约记录" />
      
      <div v-for="booking in filteredBookings" :key="booking.id" class="booking-card">
        <div class="booking-header">
          <div class="user-info">
            <div class="user-avatar">{{ (booking.user?.realName || booking.user?.username || 'U').charAt(0) }}</div>
            <div class="user-detail">
              <div class="user-name">{{ booking.user?.realName || booking.user?.username }}</div>
              <div class="user-phone">{{ booking.user?.phone || '未填写电话' }}</div>
            </div>
          </div>
          <el-tag :type="statusType(booking.status)" size="large">{{ statusText(booking.status) }}</el-tag>
        </div>
        
        <div class="booking-body">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ booking.bookingDate }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ booking.timeSlot }}</span>
          </div>
          <div v-if="booking.notes" class="notes">
            <el-icon><Document /></el-icon>
            <span>{{ booking.notes }}</span>
          </div>
        </div>
        
        <div class="booking-actions" v-if="booking.status === 0 || booking.status === 1">
          <template v-if="booking.status === 0">
            <el-button type="primary" @click="confirmBooking(booking.id)">
              <el-icon><Check /></el-icon>确认预约
            </el-button>
            <el-button type="danger" plain @click="rejectBooking(booking.id)">
              <el-icon><Close /></el-icon>拒绝
            </el-button>
          </template>
          <template v-if="booking.status === 1">
            <el-button type="success" @click="completeBooking(booking.id)">
              <el-icon><CircleCheck /></el-icon>标记完成
            </el-button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Calendar, Clock, Document, Check, Close, CircleCheck } from '@element-plus/icons-vue'
import { bookings as bookingsApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allBookings = ref([])
const statusFilter = ref(null)

const filteredBookings = computed(() => {
  if (statusFilter.value === null) return allBookings.value
  return allBookings.value.filter(b => b.status === statusFilter.value)
})

const countByStatus = (status) => allBookings.value.filter(b => b.status === status).length

const statusText = (status) => ({ 0: '待处理', 1: '已确认', 2: '已完成', 3: '已取消' }[status] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')

const fetchBookings = async () => {
  loading.value = true
  try {
    const res = await bookingsApi.counselorBookings()
    if (res.code === 200) allBookings.value = res.data
  } finally {
    loading.value = false
  }
}

const confirmBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确认接受此预约？', '确认预约')
    await bookingsApi.confirm(id)
    ElMessage.success('预约已确认')
    fetchBookings()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const rejectBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此预约吗？', '拒绝预约', { type: 'warning' })
    await bookingsApi.reject(id)
    ElMessage.success('已拒绝预约')
    fetchBookings()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const completeBooking = async (id) => {
  try {
    await ElMessageBox.confirm('确认此次咨询已完成？', '完成咨询')
    await bookingsApi.complete(id)
    ElMessage.success('咨询已完成')
    fetchBookings()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(fetchBookings)
</script>

<style scoped>
.page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }
.filter-bar { margin-bottom: 20px; }

.bookings-list { display: flex; flex-direction: column; gap: 16px; }

.booking-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.user-info { display: flex; align-items: center; gap: 12px; }

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

.user-name { font-weight: 600; color: #0f172a; font-size: 16px; }
.user-phone { font-size: 13px; color: #64748b; }

.booking-body { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 16px; }
.info-item { display: flex; align-items: center; gap: 8px; color: #64748b; }

.notes {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: #64748b;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
}

.booking-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}
</style>
