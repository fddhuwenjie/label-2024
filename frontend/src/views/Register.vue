<template>
  <div class="register-container">
    <div class="register-left">
      <div class="brand">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="currentColor"/>
          </svg>
        </div>
        <h1>心悦心理</h1>
        <p class="tagline">开启您的心灵成长之旅</p>
      </div>
      <div class="testimonial">
        <p class="quote">"在这里，我找到了内心的平静。专业的咨询师帮助我走出了困境。"</p>
        <div class="author">
          <div class="avatar">李</div>
          <div class="info">
            <span class="name">李女士</span>
            <span class="title">已咨询用户</span>
          </div>
        </div>
      </div>
    </div>
    <div class="register-right">
      <div class="register-card">
        <h2>创建账户</h2>
        <p class="subtitle">加入我们，获得专业心理支持</p>
        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister" class="register-form" label-position="top">
          <div class="form-row">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="设置用户名" size="large" maxlength="50" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="您的姓名" size="large" maxlength="50" />
            </el-form-item>
          </div>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" size="large" maxlength="11" />
          </el-form-item>
          <el-form-item label="设置密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="至少6位密码" size="large" show-password maxlength="100" />
          </el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit-btn">
            注册账户
          </el-button>
        </el-form>
        <p class="terms">
          注册即表示您同意我们的 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a>
        </p>
        <div class="divider">
          <span>已有账号？</span>
        </div>
        <router-link to="/login" class="login-link">
          返回登录
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
const formRef = ref(null)
const form = ref({ username: '', password: '', realName: '', phone: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度应在3-50之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '姓名不能超过50字', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度应在6-100之间', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  loading.value = true
  try {
    const res = await auth.register(form.value)
    if (res.code === 200) {
      ElMessage.success('注册成功')
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
}

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.register-left::before {
  content: '';
  position: absolute;
  top: 20%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.brand {
  position: relative;
  z-index: 1;
  color: white;
  margin-bottom: 80px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-icon svg {
  width: 32px;
  height: 32px;
  color: white;
}

.brand h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
}

.tagline {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
}

.testimonial {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.quote {
  font-size: 18px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 24px;
  font-style: italic;
}

.author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.author .info {
  display: flex;
  flex-direction: column;
}

.author .name {
  color: white;
  font-weight: 600;
}

.author .title {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 40px;
}

.register-card {
  width: 100%;
  max-width: 480px;
}

.register-card h2 {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.subtitle {
  color: #64748b;
  margin-bottom: 36px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.register-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  padding-bottom: 8px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  padding: 4px 14px;
  transition: all 0.2s;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: #8b5cf6;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.submit-btn {
  width: 100%;
  height: 50px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  border: none;
  margin-top: 8px;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(139, 92, 246, 0.3);
}

.terms {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin-top: 20px;
}

.terms a {
  color: #8b5cf6;
  text-decoration: none;
}

.divider {
  text-align: center;
  margin: 28px 0;
  color: #94a3b8;
  font-size: 14px;
}

.login-link {
  display: block;
  text-align: center;
  padding: 14px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  color: #334155;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
}

.login-link:hover {
  border-color: #8b5cf6;
  color: #8b5cf6;
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
  }
  .register-left {
    padding: 40px 24px;
    min-height: auto;
  }
  .brand {
    margin-bottom: 32px;
  }
  .brand h1 {
    font-size: 28px;
  }
  .tagline {
    font-size: 14px;
  }
  .testimonial {
    display: none;
  }
  .register-right {
    padding: 32px 20px 48px;
  }
  .register-card {
    max-width: 100%;
  }
  .register-card h2 {
    font-size: 26px;
  }
  .subtitle {
    font-size: 14px;
    margin-bottom: 28px;
  }
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  .form-group {
    margin-bottom: 16px;
  }
  .submit-btn {
    height: 48px;
    font-size: 15px;
  }
  .terms {
    font-size: 12px;
    margin-top: 16px;
  }
  .divider {
    margin: 24px 0;
  }
  .login-link {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .register-left {
    padding: 32px 20px;
  }
  .logo-icon {
    width: 48px;
    height: 48px;
  }
  .brand h1 {
    font-size: 24px;
  }
  .register-card h2 {
    font-size: 22px;
  }
}
</style>
