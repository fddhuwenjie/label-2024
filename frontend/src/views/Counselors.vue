<template>
  <div class="counselors-page">
    <div class="page-header">
      <div class="header-content">
        <h1>专业咨询师团队</h1>
        <p>每一位咨询师都经过严格筛选，持有国家认证资质</p>
      </div>
    </div>
    
    <div class="counselors-container">
      <div class="counselors-grid">
        <div class="counselor-card" v-for="c in list" :key="c.id" @click="handleCardClick(c)">
          <div class="card-header">
            <div class="avatar-wrapper">
              <div class="avatar">
                {{ c.user?.realName?.charAt(0) || 'C' }}
              </div>
              <div class="status-dot"></div>
            </div>
            <div class="rating">
              <el-icon><Star /></el-icon>
              <span>{{ c.rating }}</span>
            </div>
          </div>
          <div class="card-body">
            <h3>{{ c.user?.realName || '咨询师' }}</h3>
            <p class="title">{{ c.title }}</p>
            <div class="tags">
              <span class="tag" v-for="(tag, i) in (c.specialty || '').split(',').slice(0, 3)" :key="i">
                {{ tag.trim() }}
              </span>
            </div>
            <div class="meta">
              <div class="meta-item">
                <span class="label">从业经验</span>
                <span class="value">{{ c.experienceYears }}年</span>
              </div>
              <div class="meta-item">
                <span class="label">咨询费用</span>
                <span class="value price">¥{{ c.price }}<small>/次</small></span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="primary" class="book-btn">
              {{ isSelf(c) ? '这是您自己' : '立即预约' }}
              <el-icon v-if="!isSelf(c)"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="!list.length" description="暂无咨询师" class="empty-state">
        <template #image>
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" fill="currentColor"/>
            </svg>
          </div>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { counselors } from '../api'

const router = useRouter()
const list = ref([])
const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

const isSelf = (counselor) => {
  return currentUser.id && counselor.userId === currentUser.id
}

const handleCardClick = (counselor) => {
  if (isSelf(counselor)) {
    ElMessage.warning('不能预约自己的咨询服务，请前往工作台管理您的预约')
    return
  }
  router.push(`/counselors/${counselor.id}`)
}

onMounted(async () => {
  const res = await counselors.list()
  if (res.code === 200) {
    list.value = res.data
  }
})
</script>

<style scoped>
.counselors-page {
  min-height: 100%;
}

.page-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 60px 32px 60px;
}

.header-content {
  max-width: 1200px;
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

.counselors-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 32px 60px;
  background: #f8fafc;
}

.counselors-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.counselor-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.counselor-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}

.card-header {
  padding: 28px 28px 0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  font-weight: 700;
}

.status-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 16px;
  height: 16px;
  background: #10b981;
  border-radius: 50%;
  border: 3px solid white;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #fef3c7;
  border-radius: 100px;
  color: #d97706;
  font-weight: 600;
  font-size: 14px;
}

.rating .el-icon {
  font-size: 14px;
}

.card-body {
  padding: 20px 28px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-body h3 {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
  min-height: 30px;
  line-height: 1.3;
}

.title {
  font-size: 14px;
  color: #6366f1;
  font-weight: 500;
  margin-bottom: 16px;
  min-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
  min-height: 32px;
}

.tag {
  padding: 6px 12px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;
}

.meta {
  display: flex;
  gap: 24px;
  margin-top: auto;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-item .label {
  font-size: 12px;
  color: #94a3b8;
}

.meta-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

.meta-item .value.price {
  color: #6366f1;
}

.meta-item .value small {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

.card-footer {
  padding: 0 28px 28px;
}

.book-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.empty-state {
  padding: 80px 0;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: #f1f5f9;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.empty-icon svg {
  width: 40px;
  height: 40px;
  color: #94a3b8;
}

@media (max-width: 1024px) {
  .counselors-grid {
    grid-template-columns: repeat(2, 1fr);
  }
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
  .counselors-container {
    padding: 24px 16px 40px;
  }
  .counselors-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .counselor-card {
    border-radius: 16px;
  }
  .card-header {
    padding: 20px 20px 0;
  }
  .avatar {
    width: 60px;
    height: 60px;
    font-size: 24px;
    border-radius: 14px;
  }
  .card-body {
    padding: 16px 20px;
  }
  .card-body h3 {
    font-size: 20px;
  }
  .tags {
    gap: 6px;
  }
  .tag {
    padding: 5px 10px;
    font-size: 12px;
  }
  .meta {
    gap: 16px;
  }
  .card-footer {
    padding: 0 20px 20px;
  }
  .book-btn {
    height: 44px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .header-content h1 {
    font-size: 24px;
  }
  .avatar {
    width: 52px;
    height: 52px;
    font-size: 20px;
  }
  .card-body h3 {
    font-size: 18px;
  }
  .meta-item .value {
    font-size: 14px;
  }
}
</style>
