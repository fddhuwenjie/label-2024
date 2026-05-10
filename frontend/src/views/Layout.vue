<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="currentColor"/>
            </svg>
          </div>
          <span class="logo-text">心悦心理</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/counselors" class="nav-item" :class="{ active: $route.path.startsWith('/counselors') }">咨询师</router-link>
          <router-link to="/bookings" class="nav-item" :class="{ active: $route.path === '/bookings' }">我的预约</router-link>
        </nav>
        <div class="user-area">
          <el-popover
            placement="bottom-end"
            :width="360"
            trigger="click"
            :visible="popoverVisible"
            @update:visible="popoverVisible = $event"
            @show="handlePopoverShow"
            popper-class="notification-popover"
          >
            <template #reference>
              <div class="notification-trigger">
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                  <el-icon class="notification-icon"><Bell /></el-icon>
                </el-badge>
              </div>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <span class="notification-title">通知</span>
                <span class="notification-count" v-if="unreadCount > 0">{{ unreadCount }}条未读</span>
              </div>
              <div class="notification-list">
                <div v-if="notificationList.length === 0" class="notification-empty">
                  暂无通知
                </div>
                <div
                  v-for="item in notificationList"
                  :key="item.id"
                  class="notification-item"
                  :class="{ 'is-read': item.readStatus === 1 }"
                >
                  <div class="notification-icon-wrap">{{ getNotificationIcon(item.type) }}</div>
                  <div class="notification-content">
                    <div class="notification-item-title">{{ item.title }}</div>
                    <div class="notification-item-text">{{ item.content }}</div>
                    <div class="notification-item-time">{{ formatTime(item.createdAt) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-popover>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-trigger">
              <div class="user-avatar" :class="{ 'counselor-avatar': isCounselor }">
                {{ (user?.realName || user?.username || 'U').charAt(0) }}
              </div>
              <span class="user-name">
                {{ user?.realName || user?.username || '用户' }}
                <el-tag v-if="isCounselor" size="small" type="success" class="role-tag">咨询师</el-tag>
              </span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item v-if="isCounselor" command="workspace">
                  <el-icon><Monitor /></el-icon>
                  咨询师工作台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
    <footer class="footer">
      <div class="footer-content">
        <p>© 2024 心悦心理咨询. 用心倾听，专业陪伴</p>
      </div>
    </footer>
  </el-container>
</template>

<script setup>
import { onMounted, computed, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Monitor, Bell } from '@element-plus/icons-vue'
import { notifications as notificationsApi } from '../api'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.user)
const isCounselor = computed(() => user.value?.role === 1)

const unreadCount = ref(0)
const notificationList = ref([])
const popoverVisible = ref(false)
let refreshTimer = null

const fetchUnreadCount = async () => {
  if (!user.value) return
  try {
    const res = await notificationsApi.unreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {}
}

const fetchNotifications = async () => {
  if (!user.value) return
  try {
    const res = await notificationsApi.my(20)
    notificationList.value = res.data || []
  } catch (e) {}
}

const handlePopoverShow = async () => {
  await fetchNotifications()
  if (unreadCount.value > 0) {
    try {
      await notificationsApi.markAllRead()
      unreadCount.value = 0
    } catch (e) {}
  }
}

const getNotificationIcon = (type) => {
  const icons = {
    REMINDER_1H: '⏰',
    REMINDER_15M: '🔔',
    AUTO_CANCEL: '❌',
    BOOKING_CONFIRMED: '✅',
    BOOKING_REJECTED: '⚠️'
  }
  return icons[type] || '📢'
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  userStore.fetchUser()
  fetchUnreadCount()
  refreshTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'workspace') {
    // 跳转到后台管理系统
    window.open('/admin/', '_blank')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f8fafc;
}

.header {
  height: 72px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 60px;
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

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
}

.nav-item {
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  color: #0f172a;
  background: rgba(0, 0, 0, 0.04);
}

.nav-item.active {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.1);
}

.user-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-trigger {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-trigger:hover {
  background: rgba(0, 0, 0, 0.04);
}

.notification-icon {
  font-size: 22px;
  color: #64748b;
}

.notification-panel {
  max-height: 400px;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.notification-count {
  font-size: 12px;
  color: #f59e0b;
  background: #fef3c7;
  padding: 2px 8px;
  border-radius: 10px;
}

.notification-list {
  max-height: 340px;
  overflow-y: auto;
}

.notification-empty {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: #f8fafc;
}

.notification-item.is-read {
  opacity: 0.7;
}

.notification-icon-wrap {
  font-size: 20px;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 8px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #0f172a;
  margin-bottom: 4px;
}

.notification-item-text {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
  line-height: 1.5;
}

.notification-item-time {
  font-size: 12px;
  color: #94a3b8;
}

:deep(.notification-popover) {
  padding: 0;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 100px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.user-trigger:hover {
  background: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 50%;
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
  color: #334155;
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
  line-height: 16px;
}

.arrow {
  color: #94a3b8;
  font-size: 12px;
}

.main {
  padding: 0;
  min-height: calc(100vh - 72px - 80px);
}

.footer {
  background: #0f172a;
  padding: 32px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
}

.footer p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

:deep(.user-dropdown) {
  border-radius: 12px;
  padding: 8px;
  min-width: 160px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 768px) {
  .header {
    height: 60px;
  }
  .header-content {
    padding: 0 16px;
  }
  .logo {
    margin-right: 0;
  }
  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
  }
  .logo-icon svg {
    width: 20px;
    height: 20px;
  }
  .logo-text {
    font-size: 16px;
  }
  .nav-menu {
    display: none;
  }
  .user-trigger {
    padding: 4px 8px 4px 4px;
  }
  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
  .user-name {
    display: none;
  }
  .main {
    min-height: calc(100vh - 60px - 60px);
  }
  .footer {
    padding: 20px 16px;
  }
  .footer p {
    font-size: 12px;
  }
}

/* 移动端底部导航 */
@media (max-width: 768px) {
  .layout {
    padding-bottom: 60px;
  }
  .layout::after {
    content: '';
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: white;
    border-top: 1px solid #e2e8f0;
    z-index: 99;
  }
}
</style>
