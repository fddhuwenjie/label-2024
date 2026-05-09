<template>
  <div class="page">
    <div class="page-header">
      <h1>资料设置</h1>
      <p>编辑您的咨询师资料</p>
    </div>

    <div class="content-grid">
      <div class="card profile-card" v-loading="loading">
        <h3>基本信息</h3>
        <el-form :model="form" label-width="100px">
          <el-form-item label="职称">
            <el-input v-model="form.title" placeholder="如：资深心理咨询师" />
          </el-form-item>
          <el-form-item label="擅长领域">
            <el-input v-model="form.specialty" placeholder="如：情绪管理、婚姻家庭、职场压力" />
          </el-form-item>
          <el-form-item label="从业年限">
            <el-input-number v-model="form.experienceYears" :min="0" :max="50" />
            <span class="unit">年</span>
          </el-form-item>
          <el-form-item label="咨询价格">
            <el-input-number v-model="form.price" :min="0" :step="50" />
            <span class="unit">元/次</span>
          </el-form-item>
          <el-form-item label="个人介绍">
            <el-input v-model="form.introduction" type="textarea" :rows="5" placeholder="介绍您的专业背景、咨询理念等" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="card stats-card">
        <h3>我的数据</h3>
        <div class="stats-list">
          <div class="stat-row">
            <span class="label">综合评分</span>
            <span class="value">
              <el-rate v-model="counselor.rating" disabled allow-half />
              <span class="rating-num">{{ counselor.rating || '暂无' }}</span>
            </span>
          </div>
          <div class="stat-row">
            <span class="label">累计预约</span>
            <span class="value">{{ counselor.bookingCount || 0 }} 次</span>
          </div>
          <div class="stat-row">
            <span class="label">账号状态</span>
            <span class="value">
              <el-tag :type="counselor.available === 1 ? 'success' : 'info'">
                {{ counselor.available === 1 ? '已上架' : '已下架' }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { counselors as counselorsApi } from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const counselor = ref({})
const form = ref({ title: '', specialty: '', experienceYears: 0, price: 0, introduction: '' })

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await counselorsApi.me()
    if (res.code === 200 && res.data) {
      counselor.value = res.data
      form.value = {
        title: res.data.title || '',
        specialty: res.data.specialty || '',
        experienceYears: res.data.experienceYears || 0,
        price: res.data.price || 0,
        introduction: res.data.introduction || ''
      }
    }
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    await counselorsApi.updateMe(form.value)
    ElMessage.success('资料已更新')
    fetchProfile()
  } finally {
    saving.value = false
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.page { padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }

.content-grid { display: grid; grid-template-columns: 1fr 360px; gap: 24px; }

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

.unit { margin-left: 8px; color: #64748b; }

.stats-list { display: flex; flex-direction: column; gap: 16px; }

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.stat-row:last-child { border-bottom: none; }
.stat-row .label { color: #64748b; }
.stat-row .value { display: flex; align-items: center; gap: 8px; font-weight: 500; color: #0f172a; }
.rating-num { font-size: 14px; color: #f59e0b; }

@media (max-width: 1024px) {
  .content-grid { grid-template-columns: 1fr; }
}
</style>
