<template>
  <div class="page">
    <div class="page-header">
      <h1>我的评价</h1>
      <p>查看用户对您的评价</p>
    </div>

    <div class="reviews-list" v-loading="loading">
      <el-empty v-if="reviews.length === 0" description="暂无评价" />
      
      <div v-for="review in reviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="user-info">
            <div class="user-avatar">{{ (review.user?.realName || review.user?.username || 'U').charAt(0) }}</div>
            <div class="user-detail">
              <div class="user-name">{{ review.user?.realName || review.user?.username }}</div>
              <div class="review-time">{{ formatTime(review.createdAt) }}</div>
            </div>
          </div>
          <el-rate v-model="review.rating" disabled />
        </div>
        <div class="review-content">{{ review.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reviews as reviewsApi } from '../api'

const loading = ref(false)
const reviews = ref([])

const formatTime = (time) => time ? new Date(time).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) : ''

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await reviewsApi.my()
    if (res.code === 200) reviews.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(fetchReviews)
</script>

<style scoped>
.page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }

.reviews-list { display: flex; flex-direction: column; gap: 16px; }

.review-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info { display: flex; align-items: center; gap: 12px; }

.user-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.user-name { font-weight: 600; color: #0f172a; }
.review-time { font-size: 13px; color: #94a3b8; }
.review-content { color: #475569; line-height: 1.6; }
</style>
