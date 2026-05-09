<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="header-content">
        <h1>个人资料</h1>
        <p>管理您的账户信息</p>
      </div>
    </div>

    <div class="profile-container">
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar">
            {{ (form.realName || form.username || 'U').charAt(0) }}
          </div>
          <div class="avatar-info">
            <h3>{{ form.realName || form.username }}</h3>
            <p>普通用户</p>
          </div>
        </div>

        <el-form :model="form" label-position="top" class="profile-form">
          <div class="form-row">
            <el-form-item label="用户名" class="form-item">
              <el-input v-model="form.username" disabled size="large" />
              <span class="form-hint">用户名不可修改</span>
            </el-form-item>
            <el-form-item label="真实姓名" class="form-item">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" size="large" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="手机号码" class="form-item">
              <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
            </el-form-item>
            <el-form-item label="电子邮箱" class="form-item">
              <el-input v-model="form.email" placeholder="请输入邮箱" size="large" />
            </el-form-item>
          </div>
          <div class="form-actions">
            <el-button type="primary" @click="handleSave" :loading="loading" class="save-btn">
              保存修改
            </el-button>
          </div>
        </el-form>
      </div>


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { users } from '../api'

const loading = ref(false)
const form = ref({ username: '', realName: '', phone: '', email: '' })

onMounted(async () => {
  const res = await users.me()
  if (res.code === 200) {
    form.value = res.data
  }
})

const handleSave = async () => {
  loading.value = true
  try {
    const res = await users.update(form.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100%;
  background: #f8fafc;
}

.page-header {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 48px 32px;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  color: white;
}

.header-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-content p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 32px 60px;
}

.profile-card {
  background: white;
  border-radius: 20px;
  padding: 36px;
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 32px;
}

.avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  font-weight: 700;
}

.avatar-info h3 {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.avatar-info p {
  color: #64748b;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.form-item {
  margin-bottom: 24px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  padding: 4px 16px;
}

.profile-form :deep(.el-input__wrapper:hover) {
  border-color: #6366f1;
}

.profile-form :deep(.el-input__wrapper.is-focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f8fafc;
}

.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  display: block;
}

.form-actions {
  padding-top: 16px;
}

.save-btn {
  height: 48px;
  padding: 0 40px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(99, 102, 241, 0.3);
}



@media (max-width: 768px) {
  .page-header {
    padding: 32px 20px;
  }
  .header-content h1 {
    font-size: 24px;
  }
  .header-content p {
    font-size: 14px;
  }
  .profile-container {
    padding: 24px 16px 40px;
  }
  .profile-card {
    padding: 24px 20px;
    border-radius: 16px;
  }
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding-bottom: 24px;
    margin-bottom: 24px;
  }
  .avatar {
    width: 72px;
    height: 72px;
    font-size: 28px;
    border-radius: 18px;
  }
  .avatar-info h3 {
    font-size: 20px;
  }
  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .form-item {
    margin-bottom: 16px;
  }
  .save-btn {
    width: 100%;
    height: 44px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .header-content h1 {
    font-size: 24px;
  }
  .avatar {
    width: 64px;
    height: 64px;
    font-size: 24px;
  }
  .avatar-info h3 {
    font-size: 18px;
  }
}
</style>
