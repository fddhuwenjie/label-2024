import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      // 管理员页面
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { adminOnly: true } },
      { path: 'counselors', name: 'Counselors', component: () => import('../views/Counselors.vue'), meta: { adminOnly: true } },
      { path: 'bookings', name: 'Bookings', component: () => import('../views/Bookings.vue'), meta: { adminOnly: true } },
      { path: 'timeslots', name: 'TimeSlots', component: () => import('../views/TimeSlots.vue'), meta: { adminOnly: true } },
      { path: 'reviews', name: 'Reviews', component: () => import('../views/Reviews.vue'), meta: { adminOnly: true } },
      // 咨询师页面
      { path: 'my-bookings', name: 'MyBookings', component: () => import('../views/MyBookings.vue'), meta: { counselorOnly: true } },
      { path: 'my-timeslots', name: 'MyTimeSlots', component: () => import('../views/MyTimeSlots.vue'), meta: { counselorOnly: true } },
      { path: 'my-calendar', name: 'MyCalendar', component: () => import('../views/MyCalendar.vue'), meta: { counselorOnly: true } },
      { path: 'my-reviews', name: 'MyReviews', component: () => import('../views/MyReviews.vue'), meta: { counselorOnly: true } },
      { path: 'my-profile', name: 'MyProfile', component: () => import('../views/MyProfile.vue'), meta: { counselorOnly: true } }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  const user = JSON.parse(localStorage.getItem('admin_user') || 'null')
  
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else if (user && user.role !== 2 && user.role !== 1) {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_refreshToken')
    localStorage.removeItem('admin_user')
    next('/login')
  } else if (to.meta.adminOnly && user?.role !== 2) {
    // 管理员专属页面，咨询师不能访问
    next('/')
  } else if (to.meta.counselorOnly && user?.role !== 1) {
    // 咨询师专属页面，管理员不能访问
    next('/')
  } else {
    next()
  }
})

export default router
