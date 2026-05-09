<template>
  <div class="detail-page" v-if="counselor">
    <div class="page-header">
      <div class="header-content">
        <el-button class="back-btn" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>

    <div class="detail-container">
      <div class="main-content">
        <!-- Profile Card -->
        <div class="profile-card">
          <div class="profile-header">
            <div class="avatar">
              {{ counselor.user?.realName?.charAt(0) || 'C' }}
            </div>
            <div class="profile-info">
              <div class="name-row">
                <h1>{{ counselor.user?.realName }}</h1>
                <div class="verified-badge">
                  <el-icon><CircleCheck /></el-icon>
                  已认证
                </div>
              </div>
              <p class="title">{{ counselor.title }}</p>
              <div class="stats-row">
                <div class="stat">
                  <el-icon><Star /></el-icon>
                  <span class="value">{{ counselor.rating }}</span>
                  <span class="label">评分</span>
                </div>
                <div class="stat">
                  <el-icon><Calendar /></el-icon>
                  <span class="value">{{ counselor.experienceYears }}年</span>
                  <span class="label">经验</span>
                </div>
                <div class="stat">
                  <el-icon><User /></el-icon>
                  <span class="value">{{ counselor.bookingCount || 0 }}</span>
                  <span class="label">咨询人次</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="profile-section">
            <h3>擅长领域</h3>
            <div class="specialty-tags">
              <span class="specialty-tag" v-for="(tag, i) in (counselor.specialty || '').split(',')" :key="i">
                {{ tag.trim() }}
              </span>
            </div>
          </div>

          <div class="profile-section">
            <h3>个人简介</h3>
            <p class="introduction">{{ counselor.introduction || '这位咨询师暂未填写个人简介。' }}</p>
          </div>
        </div>

        <!-- Reviews Section -->
        <div class="reviews-card" v-if="reviewList.length > 0">
          <h3>用户评价 ({{ reviewList.length }})</h3>
          <div class="review-list">
            <div class="review-item" v-for="review in reviewList" :key="review.id">
              <div class="review-header">
                <div class="reviewer-info">
                  <div class="reviewer-avatar">{{ review.user?.nickname?.charAt(0) || 'U' }}</div>
                  <span class="reviewer-name">{{ review.user?.nickname || '匿名用户' }}</span>
                </div>
                <div class="review-rating">
                  <el-icon v-for="n in 5" :key="n" :class="{ filled: n <= review.rating }"><Star /></el-icon>
                </div>
              </div>
              <p class="review-content">{{ review.content }}</p>
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Booking Sidebar -->
      <div class="booking-sidebar">
        <div class="booking-card">
          <div class="price-header">
            <span class="price">¥{{ counselor.price }}</span>
            <span class="unit">/次咨询</span>
          </div>
          
          <el-form :model="form" class="booking-form">
            <div class="form-group">
              <label>选择日期</label>
              <el-date-picker 
                v-model="form.bookingDate" 
                type="date" 
                placeholder="请选择咨询日期"
                :disabled-date="disabledDate" 
                style="width: 100%"
                size="large"
                @change="loadAvailableSlots"
              />
            </div>
            <div class="form-group">
              <label>选择时间</label>
              <div class="time-slots" v-if="availableSlots.length > 0">
                <div 
                  class="time-slot" 
                  v-for="slot in availableSlots" 
                  :key="slot.id || slot"
                  :class="{ active: form.timeSlot === getSlotValue(slot) }"
                  @click="form.timeSlot = getSlotValue(slot)"
                >
                  {{ getSlotDisplay(slot) }}
                </div>
              </div>
              <div class="no-slots" v-else-if="form.bookingDate">
                <el-icon><Warning /></el-icon>
                <span>该日期暂无可预约时间段</span>
              </div>
              <div class="no-slots" v-else>
                <span>请先选择日期</span>
              </div>
            </div>
            <div class="form-group">
              <label>备注信息</label>
              <el-input 
                v-model="form.notes" 
                type="textarea" 
                :rows="3" 
                placeholder="简单描述您想咨询的问题（选填）"
                maxlength="500"
                show-word-limit
              />
            </div>
            <el-button type="primary" @click="handleBook" :loading="loading" class="submit-btn">
              确认预约
            </el-button>
          </el-form>

          <div class="booking-tips">
            <div class="tip-item">
              <el-icon><CircleCheck /></el-icon>
              <span>预约成功后咨询师将在24小时内确认</span>
            </div>
            <div class="tip-item">
              <el-icon><CircleCheck /></el-icon>
              <span>支持预约前24小时免费取消</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { counselors, bookings, timeSlots, reviews } from '../api'

const route = useRoute()
const router = useRouter()
const counselor = ref(null)
const loading = ref(false)
const form = ref({ bookingDate: '', timeSlot: '', notes: '' })
const availableSlots = ref([])
const reviewList = ref([])

// 默认时间段（当咨询师没有配置时间段时使用）
const defaultTimeSlots = [
  '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

const disabledDate = (date) => date < new Date(new Date().setHours(0, 0, 0, 0))

const getSlotValue = (slot) => {
  if (typeof slot === 'string') return slot
  return `${slot.startTime}-${slot.endTime}`
}

const getSlotDisplay = (slot) => {
  if (typeof slot === 'string') return slot
  return `${slot.startTime}-${slot.endTime}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatLocalDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const loadAvailableSlots = async () => {
  if (!form.value.bookingDate || !counselor.value) {
    availableSlots.value = []
    return
  }
  
  try {
    const dateStr = formatLocalDate(form.value.bookingDate)
    const res = await timeSlots.listByDate(counselor.value.id, dateStr)
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 过滤出可预约的时段（status=0），排除禁用的（status=2）
      const available = res.data.filter(s => s.status === 0)
      // 获取禁用的时段列表
      const disabledSlots = res.data.filter(s => s.status === 2).map(s => `${s.startTime}-${s.endTime}`)
      
      if (available.length > 0) {
        availableSlots.value = available
      } else {
        // 没有可预约时段，使用默认时段但排除禁用的
        availableSlots.value = defaultTimeSlots.filter(slot => !disabledSlots.includes(slot))
      }
    } else {
      // 如果没有配置时间段，使用默认时间段
      availableSlots.value = defaultTimeSlots
    }
  } catch (e) {
    // 出错时使用默认时间段
    availableSlots.value = defaultTimeSlots
  }
  form.value.timeSlot = ''
}

const loadReviews = async () => {
  try {
    const res = await reviews.listByCounselor(route.params.id)
    if (res.code === 200) {
      reviewList.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to load reviews', e)
  }
}

onMounted(async () => {
  const res = await counselors.get(route.params.id)
  if (res.code === 200) {
    counselor.value = res.data
  }
  loadReviews()
})

const handleBook = async () => {
  if (!form.value.bookingDate || !form.value.timeSlot) {
    ElMessage.warning('请选择日期和时间')
    return
  }
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const dateStr = formatLocalDate(form.value.bookingDate)
    const res = await bookings.create({
      counselorId: counselor.value.id,
      bookingDate: dateStr,
      timeSlot: form.value.timeSlot,
      notes: form.value.notes
    })
    if (res.code === 200) {
      ElMessage.success('预约成功')
      router.push('/bookings')
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.detail-page {
  background: #f8fafc;
  min-height: 100%;
}

.page-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 24px 32px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 32px;
}

.profile-card, .reviews-card {
  background: white;
  border-radius: 20px;
  padding: 36px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  gap: 28px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 32px;
}

.avatar {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  font-weight: 700;
  flex-shrink: 0;
}

.profile-info {
  flex: 1;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name-row h1 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.verified-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: #ecfdf5;
  color: #059669;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
}

.title {
  font-size: 16px;
  color: #6366f1;
  font-weight: 500;
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 32px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat .el-icon {
  color: #94a3b8;
}

.stat .value {
  font-weight: 700;
  color: #0f172a;
}

.stat .label {
  color: #64748b;
  font-size: 14px;
}

.profile-section {
  margin-bottom: 28px;
}

.profile-section:last-child {
  margin-bottom: 0;
}

.profile-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 16px;
}

.specialty-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.specialty-tag {
  padding: 8px 16px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
  color: #6366f1;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.introduction {
  font-size: 15px;
  line-height: 1.8;
  color: #475569;
}

/* Reviews */
.reviews-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 20px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #e2e8f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reviewer-avatar {
  width: 36px;
  height: 36px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
}

.reviewer-name {
  font-weight: 500;
  color: #334155;
}

.review-rating .el-icon {
  color: #e2e8f0;
  font-size: 14px;
}

.review-rating .el-icon.filled {
  color: #fbbf24;
}

.review-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-date {
  font-size: 12px;
  color: #94a3b8;
}

/* Booking Sidebar */
.booking-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  border: 1px solid #e2e8f0;
  position: sticky;
  top: 104px;
}

.price-header {
  text-align: center;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.price {
  font-size: 36px;
  font-weight: 700;
  color: #0f172a;
}

.unit {
  font-size: 16px;
  color: #64748b;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 10px;
}

.time-slots {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.time-slot {
  padding: 12px;
  text-align: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.time-slot:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.time-slot.active {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-color: transparent;
  color: white;
}

.no-slots {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 10px;
  color: #64748b;
  font-size: 14px;
}

.no-slots .el-icon {
  color: #f59e0b;
}

.booking-form :deep(.el-input__wrapper),
.booking-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  box-shadow: none;
}

.booking-form :deep(.el-input__wrapper:hover),
.booking-form :deep(.el-textarea__inner:hover) {
  border-color: #6366f1;
}

.booking-form :deep(.el-input__wrapper.is-focus),
.booking-form :deep(.el-textarea__inner:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.submit-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  margin-top: 8px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(99, 102, 241, 0.3);
}

.booking-tips {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 10px;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-item .el-icon {
  color: #10b981;
}

@media (max-width: 1024px) {
  .detail-container {
    grid-template-columns: 1fr;
  }
  .booking-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 16px 20px;
  }
  .detail-container {
    padding: 20px 16px;
    gap: 20px;
  }
  .profile-card, .reviews-card {
    padding: 24px 20px;
    border-radius: 16px;
  }
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
    padding-bottom: 24px;
    margin-bottom: 24px;
  }
  .avatar {
    width: 100px;
    height: 100px;
    font-size: 40px;
    border-radius: 20px;
  }
  .name-row {
    flex-direction: column;
    gap: 8px;
  }
  .name-row h1 {
    font-size: 24px;
  }
  .stats-row {
    justify-content: center;
    flex-wrap: wrap;
    gap: 20px;
  }
  .profile-section h3 {
    font-size: 15px;
  }
  .specialty-tags {
    justify-content: center;
  }
  .specialty-tag {
    padding: 6px 12px;
    font-size: 13px;
  }
  .introduction {
    font-size: 14px;
    text-align: center;
  }
  .booking-card {
    padding: 24px 20px;
    border-radius: 16px;
  }
  .price {
    font-size: 32px;
  }
  .time-slots {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
  .time-slot {
    padding: 10px;
    font-size: 13px;
  }
  .submit-btn {
    height: 48px;
    font-size: 15px;
  }
  .booking-tips {
    margin-top: 20px;
    padding-top: 20px;
  }
  .tip-item {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  .name-row h1 {
    font-size: 22px;
  }
  .stats-row {
    gap: 16px;
  }
  .stat {
    font-size: 13px;
  }
  .price {
    font-size: 28px;
  }
  .time-slots {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
