<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="currentColor"/>
          </svg>
        </div>
        <h1>心悦心理</h1>
        <p class="tagline">专业 · 温暖 · 值得信赖</p>
      </div>
      <div class="features-list">
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span>国家认证心理咨询师</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span>严格隐私保护机制</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span>7×24小时在线预约</span>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="subtitle">登录您的账户，开启心灵之旅</p>
        <el-form :model="form" @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>用户名</label>
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
          </div>
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit-btn">
            登录
          </el-button>
        </el-form>
        <div class="divider">
          <span>还没有账号？</span>
        </div>
        <router-link to="/register" class="register-link">
          立即注册
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { auth } from '../api'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await auth.login(form.value)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      localStorage.setItem('user', JSON.stringify(res.data.user))
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.login-left::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 50%);
  animation: pulse 15s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.brand {
  position: relative;
  z-index: 1;
  color: white;
  margin-bottom: 60px;
  text-align: center;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.logo-icon svg {
  width: 36px;
  height: 36px;
  color: white;
}

.brand h1 {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -1px;
}

.tagline {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4px;
}

.features-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  margin-bottom: 20px;
}

.feature-icon {
  width: 24px;
  height: 24px;
  background: rgba(99, 102, 241, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #a5b4fc;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  padding: 40px;
  position: relative;
}

.login-right::before {
  content: '';
  position: absolute;
  top: 10%;
  right: 10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.login-right::after {
  content: '';
  position: absolute;
  bottom: 10%;
  left: 10%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.06) 0%, transparent 70%);
  border-radius: 50%;
}

.login-card {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
}

.login-card h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.subtitle {
  color: #6b7280;
  margin-bottom: 40px;
  font-size: 15px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
  padding: 4px 16px;
  transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: #6366f1;
}

.login-form :deep(.el-input__wrapper.is-focus) {
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
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(99, 102, 241, 0.3);
}

.divider {
  text-align: center;
  margin: 32px 0;
  color: #9ca3af;
  font-size: 14px;
}

.register-link {
  display: block;
  text-align: center;
  padding: 16px;
  border: 2px solid #c7d2fe;
  border-radius: 12px;
  color: #4f46e5;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
  background: rgba(99, 102, 241, 0.05);
}

.register-link:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  .login-left {
    padding: 40px 24px;
    min-height: auto;
  }
  .brand {
    margin-bottom: 40px;
  }
  .brand h1 {
    font-size: 32px;
  }
  .tagline {
    font-size: 14px;
    letter-spacing: 2px;
  }
  .features-list {
    display: none;
  }
  .login-right {
    padding: 32px 20px 48px;
  }
  .login-card {
    max-width: 100%;
  }
  .login-card h2 {
    font-size: 26px;
  }
  .subtitle {
    font-size: 14px;
    margin-bottom: 32px;
  }
  .form-group {
    margin-bottom: 20px;
  }
  .submit-btn {
    height: 48px;
    font-size: 15px;
  }
  .divider {
    margin: 24px 0;
  }
  .register-link {
    padding: 14px;
  }
}

@media (max-width: 480px) {
  .login-left {
    padding: 32px 20px;
  }
  .logo-icon {
    width: 56px;
    height: 56px;
  }
  .brand h1 {
    font-size: 28px;
  }
  .login-card h2 {
    font-size: 24px;
  }
}
</style>
