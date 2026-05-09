<template>
  <div class="bookings-page">
    <div class="page-header">
      <div class="header-content">
        <h1>我的预约</h1>
        <p>查看和管理您的咨询预约记录</p>
      </div>
    </div>

    <div class="bookings-container">
      <div class="bookings-list" v-if="list.length">
        <div class="booking-card" v-for="item in list" :key="item.id">
          <div class="card-left">
            <div class="counselor-avatar">
              {{ item.counselor?.user?.realName?.charAt(0) || 'C' }}
            </div>
            <div class="booking-info">
              <h3>{{ item.counselor?.user?.realName || '咨询师' }}</h3>
              <p class="counselor-title">{{ item.counselor?.title }}</p>
              <div class="booking-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ item.bookingDate }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  {{ item.timeSlot }}
                </span>
                <span class="meta-item price">
                  <el-icon><Money /></el-icon>
                  ¥{{ item.counselor?.price || 0 }}
                </span>
              </div>
            </div>
          </div>
          <div class="card-right">
            <div class="status-badge" :class="statusClass(item.status)">
              {{ statusText(item.status) }}
            </div>
            <div class="action-buttons">
              <!-- 待确认状态：可取消、可支付 -->
              <el-button 
                v-if="item.status === 0" 
                type="primary"
                class="pay-btn"
                @click="handlePay(item)"
              >
                去支付
              </el-button>
              <el-button 
                v-if="item.status === 0" 
                type="danger" 
                plain
                class="cancel-btn"
                @click="handleCancel(item.id)"
              >
                取消预约
              </el-button>
              <!-- 已完成状态：可评价 -->
              <el-button 
                v-if="item.status === 2 && !item.reviewed" 
                type="primary"
                plain
                class="review-btn"
                @click="openReviewDialog(item)"
              >
                评价咨询
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else>
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11zM9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm-8 4H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z" fill="currentColor"/>
          </svg>
        </div>
        <h3>暂无预约记录</h3>
        <p>您还没有预约过咨询服务</p>
        <el-button type="primary" class="action-btn" @click="$router.push('/counselors')">
          浏览咨询师
        </el-button>
      </div>
    </div>

    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="确认支付" width="400px">
      <div class="pay-dialog-content">
        <div class="pay-info">
          <p>咨询师：{{ currentBooking?.counselor?.user?.realName }}</p>
          <p>预约时间：{{ currentBooking?.bookingDate }} {{ currentBooking?.timeSlot }}</p>
          <p class="pay-amount">支付金额：<span>¥{{ currentBooking?.counselor?.price }}</span></p>
        </div>
        <div class="pay-methods">
          <div 
            class="pay-method" 
            :class="{ active: payMethod === 'mock' }"
            @click="payMethod = 'mock'"
          >
            <el-icon><CreditCard /></el-icon>
            <span>模拟支付</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay" :loading="payLoading">
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="评价咨询" width="500px">
      <div class="review-dialog-content">
        <div class="rating-section">
          <label>评分</label>
          <div class="rating-stars">
            <el-icon 
              v-for="n in 5" 
              :key="n" 
              :class="{ filled: n <= reviewForm.rating }"
              @click="reviewForm.rating = n"
            >
              <StarFilled />
            </el-icon>
          </div>
        </div>
        <div class="content-section">
          <label>评价内容</label>
          <el-input 
            v-model="reviewForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="分享您的咨询体验..."
            maxlength="1000"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewLoading">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookings, payments, reviews } from '../api'

const list = ref([])
const payDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentBooking = ref(null)
const payMethod = ref('mock')
const payLoading = ref(false)
const reviewLoading = ref(false)
const reviewForm = ref({ rating: 5, content: '' })

const statusText = (s) => ['待确认', '已确认', '已完成', '已取消'][s]
const statusClass = (s) => ['pending', 'confirmed', 'completed', 'cancelled'][s]

const fetchBookings = async () => {
  const res = await bookings.my()
  if (res.code === 200) {
    list.value = res.data
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '取消预约', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    await bookings.cancel(id)
    ElMessage.success('预约已取消')
    fetchBookings()
  } catch {}
}

const handlePay = (booking) => {
  currentBooking.value = booking
  payDialogVisible.value = true
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    // 创建支付订单
    const createRes = await payments.create({
      bookingId: currentBooking.value.id,
      paymentMethod: payMethod.value
    })
    
    if (createRes.code === 200) {
      // 模拟支付回调（实际项目中由支付平台回调）
      const callbackRes = await payments.mockCallback({
        orderNo: createRes.data.orderNo,
        success: true
      })
      
      if (callbackRes.code === 200) {
        ElMessage.success('支付成功')
        payDialogVisible.value = false
        fetchBookings()
      } else {
        ElMessage.error(callbackRes.message || '支付失败')
      }
    } else {
      ElMessage.error(createRes.message || '创建订单失败')
    }
  } catch (e) {
    ElMessage.error('支付失败，请重试')
  } finally {
    payLoading.value = false
  }
}

const openReviewDialog = (booking) => {
  currentBooking.value = booking
  reviewForm.value = { rating: 5, content: '' }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (reviewForm.value.rating < 1) {
    ElMessage.warning('请选择评分')
    return
  }
  
  reviewLoading.value = true
  try {
    const res = await reviews.create({
      bookingId: currentBooking.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    
    if (res.code === 200) {
      ElMessage.success('评价成功')
      reviewDialogVisible.value = false
      // 标记为已评价
      const booking = list.value.find(b => b.id === currentBooking.value.id)
      if (booking) booking.reviewed = true
    } else {
      ElMessage.error(res.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error('评价失败，请重试')
  } finally {
    reviewLoading.value = false
  }
}

onMounted(fetchBookings)
</script>

<style scoped>
.bookings-page {
  min-height: 100%;
  background: #f8fafc;
}

.page-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 60px 32px 60px;
}

.header-content {
  max-width: 1000px;
  margin: 0 auto;
  text-align: center;
  color: white;
}

.header-content h1 {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 12px;
}

.header-content p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
}

.bookings-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 32px 60px;
}

.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.booking-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.booking-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  border-color: transparent;
}

.card-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.counselor-avatar {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.booking-info h3 {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.counselor-title {
  font-size: 14px;
  color: #6366f1;
  margin-bottom: 12px;
}

.booking-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #64748b;
}

.meta-item.price {
  color: #059669;
  font-weight: 600;
}

.meta-item .el-icon {
  font-size: 16px;
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.confirmed {
  background: #d1fae5;
  color: #059669;
}

.status-badge.completed {
  background: #e0e7ff;
  color: #4f46e5;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #dc2626;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.cancel-btn, .pay-btn, .review-btn {
  border-radius: 8px;
  font-weight: 500;
}

.pay-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
}

/* Empty State */
.empty-state {
  background: white;
  border-radius: 20px;
  padding: 80px 40px;
  text-align: center;
  border: 1px solid #e2e8f0;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: #f1f5f9;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.empty-icon svg {
  width: 40px;
  height: 40px;
  color: #94a3b8;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.empty-state p {
  color: #64748b;
  margin-bottom: 24px;
}

.action-btn {
  height: 48px;
  padding: 0 32px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
}

/* Pay Dialog */
.pay-dialog-content {
  padding: 10px 0;
}

.pay-info {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.pay-info p {
  margin-bottom: 8px;
  color: #475569;
}

.pay-info p:last-child {
  margin-bottom: 0;
}

.pay-amount {
  font-size: 16px;
  font-weight: 600;
}

.pay-amount span {
  color: #dc2626;
  font-size: 20px;
}

.pay-methods {
  display: flex;
  gap: 12px;
}

.pay-method {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-method:hover {
  border-color: #6366f1;
}

.pay-method.active {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.05);
}

.pay-method .el-icon {
  font-size: 24px;
  color: #6366f1;
}

/* Review Dialog */
.review-dialog-content {
  padding: 10px 0;
}

.rating-section, .content-section {
  margin-bottom: 20px;
}

.rating-section label, .content-section label {
  display: block;
  font-weight: 600;
  color: #334155;
  margin-bottom: 10px;
}

.rating-stars {
  display: flex;
  gap: 8px;
}

.rating-stars .el-icon {
  font-size: 32px;
  color: #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.rating-stars .el-icon:hover,
.rating-stars .el-icon.filled {
  color: #fbbf24;
}

@media (max-width: 768px) {
  .page-header {
    padding: 40px 20px;
  }
  .header-content h1 {
    font-size: 28px;
  }
  .header-content p {
    font-size: 15px;
  }
  .bookings-container {
    padding: 24px 16px 40px;
  }
  .bookings-list {
    gap: 12px;
  }
  .booking-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
    border-radius: 14px;
  }
  .card-left {
    width: 100%;
    gap: 16px;
  }
  .counselor-avatar {
    width: 56px;
    height: 56px;
    font-size: 20px;
    border-radius: 14px;
  }
  .booking-info h3 {
    font-size: 16px;
  }
  .counselor-title {
    font-size: 13px;
    margin-bottom: 8px;
  }
  .booking-meta {
    flex-wrap: wrap;
    gap: 12px;
  }
  .meta-item {
    font-size: 13px;
  }
  .card-right {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  .status-badge {
    padding: 5px 12px;
    font-size: 12px;
  }
  .action-buttons {
    flex-wrap: wrap;
    gap: 6px;
  }
  .cancel-btn, .pay-btn, .review-btn {
    font-size: 13px;
    padding: 8px 12px;
  }
  .empty-state {
    padding: 60px 24px;
    border-radius: 16px;
  }
  .empty-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
  }
  .empty-icon svg {
    width: 32px;
    height: 32px;
  }
  .empty-state h3 {
    font-size: 18px;
  }
  .action-btn {
    width: 100%;
    height: 44px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .header-content h1 {
    font-size: 24px;
  }
  .booking-meta {
    flex-direction: column;
    gap: 8px;
  }
  .card-right {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .action-buttons {
    width: 100%;
  }
  .cancel-btn, .pay-btn, .review-btn {
    flex: 1;
  }
}
</style>
