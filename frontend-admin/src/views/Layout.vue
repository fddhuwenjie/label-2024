<template>
  <el-container class="layout">
    <el-aside class="sidebar" width="240px">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon" :class="{ 'counselor-icon': isCounselor }">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="currentColor"/>
            </svg>
          </div>
          <span class="logo-text">{{ isCounselor ? '咨询师工作台' : '心悦管理' }}</span>
        </div>
      </div>
      <el-menu :default-active="$route.path" class="sidebar-menu" router>
        <!-- 管理员菜单 -->
        <template v-if="isAdmin">
          <el-menu-item index="/">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/counselors">
            <el-icon><Avatar /></el-icon>
            <span>咨询师管理</span>
          </el-menu-item>
          <el-menu-item index="/bookings">
            <el-icon><Calendar /></el-icon>
            <span>预约管理</span>
          </el-menu-item>
          <el-menu-item index="/timeslots">
            <el-icon><Clock /></el-icon>
            <span>时段管理</span>
          </el-menu-item>
          <el-menu-item index="/reviews">
            <el-icon><ChatDotRound /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
        </template>
        <!-- 咨询师菜单 -->
        <template v-if="isCounselor">
          <el-menu-item index="/">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/my-bookings">
            <el-icon><Calendar /></el-icon>
            <span>预约管理</span>
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="menu-badge" />
          </el-menu-item>
          <el-menu-item index="/my-calendar">
            <el-icon><Calendar /></el-icon>
            <span>日程管理</span>
          </el-menu-item>
          <el-menu-item index="/my-timeslots">
            <el-icon><Clock /></el-icon>
            <span>时间管理</span>
          </el-menu-item>
          <el-menu-item index="/my-reviews">
            <el-icon><ChatDotRound /></el-icon>
            <span>我的评价</span>
          </el-menu-item>
          <el-menu-item index="/my-profile">
            <el-icon><Setting /></el-icon>
            <span>资料设置</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentPageName">{{ currentPageName }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-right">
          <div class="user-info">
            <div class="user-avatar" :class="{ 'counselor-avatar': isCounselor }">
              {{ (user?.realName || 'A').charAt(0) }}
            </div>
            <span class="user-name">{{ user?.realName || '用户' }}</span>
          </div>
          <el-button text class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出</span>
          </el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Setting, SwitchButton } from '@element-plus/icons-vue'
import { bookings } from '../api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const isAdmin = computed(() => user.value?.role === 2)
const isCounselor = computed(() => user.value?.role === 1)
const pendingCount = ref(0)

const pageNames = {
  '/': '数据统计',
  '/users': '用户管理',
  '/counselors': '咨询师管理',
  '/bookings': '预约管理',
  '/timeslots': '时段管理',
  '/reviews': '评价管理',
  '/my-bookings': '预约管理',
  '/my-calendar': '日程管理',
  '/my-timeslots': '时间管理',
  '/my-reviews': '我的评价',
  '/my-profile': '资料设置'
}

const currentPageName = computed(() => {
  if (route.path === '/') return ''
  return pageNames[route.path] || ''
})

onMounted(async () => {
  await userStore.fetchUser()
  if (isCounselor.value) {
    fetchPendingCount()
  }
})

const fetchPendingCount = async () => {
  try {
    const res = await bookings.counselorBookings()
    if (res.code === 200) {
      pendingCount.value = res.data.filter(b => b.status === 0).length
    }
  } catch (e) {
    console.error(e)
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: #0f172a;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 240px;
  z-index: 100;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-icon.counselor-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: white;
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  padding: 16px 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  border-radius: 10px;
  margin-bottom: 4px;
  color: rgba(255, 255, 255, 0.7);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
}

.sidebar-menu :deep(.el-menu-item) {
  position: relative;
}

.menu-badge {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
}

.main-container {
  flex-direction: column;
  margin-left: 240px;
  height: 100vh;
  overflow: hidden;
}

.header {
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header :deep(.el-breadcrumb__item) {
  font-size: 14px;
}

.header :deep(.el-breadcrumb__inner) {
  color: #64748b;
}

.header :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-avatar.counselor-avatar {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #0f172a;
}

.logout-btn {
  color: #64748b;
  font-size: 14px;
}

.logout-btn:hover {
  color: #ef4444;
}

.main {
  background: #f8fafc;
  padding: 0;
  overflow-y: auto;
  flex: 1;
  height: calc(100vh - 64px);
}
</style>
