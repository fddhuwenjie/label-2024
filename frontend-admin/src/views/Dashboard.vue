<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h1>{{ isCounselor ? '数据概览' : '数据统计' }}</h1>
      <p>{{ isCounselor ? '您的工作数据概览' : '系统运营数据概览' }}</p>
    </div>

    <div class="dashboard-container">
      <!-- 管理员视图 -->
      <template v-if="!isCounselor">
        <!-- Stats Cards -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon blue">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ overview.totalUsers || 0 }}</span>
              <span class="stat-label">注册用户</span>
            </div>
            <div class="stat-extra">今日 +{{ overview.todayUsers || 0 }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon green">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ overview.totalCounselors || 0 }}</span>
              <span class="stat-label">咨询师</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon purple">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ overview.totalBookings || 0 }}</span>
              <span class="stat-label">总预约数</span>
            </div>
            <div class="stat-extra">今日 +{{ overview.todayBookings || 0 }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon yellow">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ overview.totalReviews || 0 }}</span>
              <span class="stat-label">用户评价</span>
            </div>
          </div>
        </div>

        <!-- Booking Status -->
        <div class="cards-row">
          <div class="card booking-status">
            <h3>预约状态分布</h3>
            <div class="status-list">
              <div class="status-item">
                <span class="status-dot pending"></span>
                <span class="status-name">待确认</span>
                <span class="status-count">{{ overview.pendingBookings || 0 }}</span>
              </div>
              <div class="status-item">
                <span class="status-dot confirmed"></span>
                <span class="status-name">已确认</span>
                <span class="status-count">{{ overview.confirmedBookings || 0 }}</span>
              </div>
              <div class="status-item">
                <span class="status-dot completed"></span>
                <span class="status-name">已完成</span>
                <span class="status-count">{{ overview.completedBookings || 0 }}</span>
              </div>
              <div class="status-item">
                <span class="status-dot cancelled"></span>
                <span class="status-name">已取消</span>
                <span class="status-count">{{ overview.cancelledBookings || 0 }}</span>
              </div>
            </div>
          </div>

          <div class="card trend-chart">
            <h3>近7天预约趋势</h3>
            <div class="chart-container">
              <div class="chart-bars">
                <div 
                  v-for="(count, idx) in trend.counts" 
                  :key="idx" 
                  class="bar-item"
                >
                  <div class="bar" :style="{ height: barHeight(count) + '%' }">
                    <span class="bar-value">{{ count }}</span>
                  </div>
                  <span class="bar-label">{{ trend.dates?.[idx] }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 咨询师视图 -->
      <template v-else>
        <div class="stats-grid counselor-stats">
          <div class="stat-card">
            <div class="stat-icon yellow">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ counselorStats.pendingBookings || 0 }}</span>
              <span class="stat-label">待处理预约</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon blue">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ counselorStats.confirmedBookings || 0 }}</span>
              <span class="stat-label">已确认预约</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon green">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ counselorStats.completedBookings || 0 }}</span>
              <span class="stat-label">已完成咨询</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon purple">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ counselorStats.rating || '暂无' }}</span>
              <span class="stat-label">综合评分</span>
            </div>
          </div>
        </div>

        <div class="cards-row counselor-cards">
          <div class="card">
            <h3>本月数据</h3>
            <div class="info-list">
              <div class="info-item">
                <span>新增预约</span>
                <span class="value">{{ counselorStats.monthBookings || 0 }}</span>
              </div>
              <div class="info-item">
                <span>完成咨询</span>
                <span class="value">{{ counselorStats.monthCompleted || 0 }}</span>
              </div>
              <div class="info-item">
                <span>收到评价</span>
                <span class="value">{{ counselorStats.totalReviews || 0 }}</span>
              </div>
            </div>
          </div>
          <div class="card">
            <h3>快捷操作</h3>
            <div class="quick-actions">
              <el-button type="primary" @click="$router.push('/my-bookings')">
                <el-icon><Calendar /></el-icon>处理预约
              </el-button>
              <el-button @click="$router.push('/my-timeslots')">
                <el-icon><Clock /></el-icon>设置时间
              </el-button>
              <el-button @click="$router.push('/my-profile')">
                <el-icon><Edit /></el-icon>编辑资料
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Clock, Check, CircleCheck, Edit } from '@element-plus/icons-vue'
import { stats } from '../api'

const user = JSON.parse(localStorage.getItem('admin_user') || '{}')
const isCounselor = computed(() => user.role === 1)

const overview = ref({})
const trend = ref({ dates: [], counts: [] })
const counselorStats = ref({})

const maxCount = computed(() => Math.max(...(trend.value.counts || [1]), 1))
const barHeight = (count) => (count / maxCount.value) * 100

const fetchData = async () => {
  if (isCounselor.value) {
    const res = await stats.counselorMy()
    if (res.code === 200) counselorStats.value = res.data
  } else {
    const [overviewRes, trendRes] = await Promise.all([
      stats.overview(),
      stats.bookingTrend(7)
    ])
    if (overviewRes.code === 200) overview.value = overviewRes.data
    if (trendRes.code === 200) trend.value = trendRes.data
  }
}

onMounted(fetchData)
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  padding: 32px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 14px;
  color: #64748b;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e2e8f0;
  position: relative;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.blue { background: #eff6ff; color: #3b82f6; }
.stat-icon.green { background: #d1fae5; color: #059669; }
.stat-icon.purple { background: #ede9fe; color: #7c3aed; }
.stat-icon.yellow { background: #fef3c7; color: #d97706; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.stat-extra {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  color: #10b981;
  background: #d1fae5;
  padding: 2px 8px;
  border-radius: 4px;
}

.cards-row {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.card h3 {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.2s;
}

.status-item:hover {
  background: #f1f5f9;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.pending { background: #f59e0b; }
.status-dot.confirmed { background: #10b981; }
.status-dot.completed { background: #6366f1; }
.status-dot.cancelled { background: #ef4444; }

.status-name {
  flex: 1;
  color: #475569;
  font-size: 14px;
}

.status-count {
  font-weight: 700;
  font-size: 18px;
  color: #0f172a;
}

.chart-container {
  height: 220px;
  padding-top: 20px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  gap: 8px;
  padding: 0 10px;
}

.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  max-width: 60px;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 8px 8px 0 0;
  min-height: 8px;
  display: flex;
  justify-content: center;
  position: relative;
  margin-top: auto;
  transition: all 0.3s;
}

.bar:hover {
  opacity: 0.85;
  transform: scaleY(1.02);
}

.bar-value {
  position: absolute;
  top: -24px;
  font-size: 13px;
  font-weight: 700;
  color: #6366f1;
}

.bar-label {
  margin-top: 12px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .cards-row { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .cards-row { grid-template-columns: 1fr; }
}

/* 咨询师视图样式 */
.counselor-stats {
  grid-template-columns: repeat(4, 1fr);
}

.counselor-cards {
  grid-template-columns: 1fr 1fr;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  color: #64748b;
}

.info-item .value {
  font-weight: 700;
  color: #0f172a;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  justify-content: flex-start;
  height: 48px;
}
</style>
