import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// Token刷新逻辑
let isRefreshing = false
let refreshSubscribers = []

const subscribeTokenRefresh = (cb) => {
  refreshSubscribers.push(cb)
}

const onRefreshed = (token) => {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  async error => {
    const originalRequest = error.config
    
    // Token过期，尝试刷新
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(resolve => {
          subscribeTokenRefresh(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(api(originalRequest))
          })
        })
      }
      
      originalRequest._retry = true
      isRefreshing = true
      
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          const { token, refreshToken: newRefreshToken } = res.data.data
          localStorage.setItem('token', token)
          localStorage.setItem('refreshToken', newRefreshToken)
          onRefreshed(token)
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        } catch (refreshError) {
          // 刷新失败，清除登录状态
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          window.location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      }
    }
    
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export const auth = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken })
}

export const users = {
  me: () => api.get('/users/me'),
  update: (data) => api.put('/users/me', data)
}

export const counselors = {
  list: () => api.get('/counselors'),
  get: (id) => api.get(`/counselors/${id}`),
  // 咨询师专属
  me: () => api.get('/counselors/me'),
  updateMe: (data) => api.put('/counselors/me', data)
}

export const bookings = {
  my: () => api.get('/bookings/my'),
  all: () => api.get('/bookings/all'),
  create: (data) => api.post('/bookings', data),
  updateStatus: (id, status) => api.put(`/bookings/${id}/status?status=${status}`),
  cancel: (id) => api.delete(`/bookings/${id}`),
  // 咨询师专属
  counselorBookings: () => api.get('/bookings/counselor'),
  confirm: (id) => api.put(`/bookings/${id}/confirm`),
  complete: (id) => api.put(`/bookings/${id}/complete`),
  reject: (id) => api.put(`/bookings/${id}/reject`)
}

export const timeSlots = {
  listByCounselor: (counselorId) => api.get(`/timeslots/counselor/${counselorId}`),
  listByDate: (counselorId, date) => api.get(`/timeslots/counselor/${counselorId}/date?date=${date}`),
  create: (data) => api.post('/timeslots', data),
  batchCreate: (data) => api.post('/timeslots/batch', data),
  updateStatus: (id, status) => api.put(`/timeslots/${id}/status?status=${status}`),
  delete: (id) => api.delete(`/timeslots/${id}`),
  // 咨询师专属
  my: () => api.get('/timeslots/my'),
  myBatchCreate: (data) => api.post('/timeslots/my/batch', data)
}

export const reviews = {
  listByCounselor: (counselorId) => api.get(`/reviews/counselor/${counselorId}`),
  create: (data) => api.post('/reviews', data),
  // 咨询师专属
  my: () => api.get('/reviews/my')
}

export const stats = {
  // 咨询师专属
  counselorMy: () => api.get('/stats/counselor/my')
}

export const payments = {
  create: (data) => api.post('/payments', data),
  mockCallback: (data) => api.post('/payments/mock-callback', data),
  refund: (id) => api.post(`/payments/${id}/refund`),
  getByOrderNo: (orderNo) => api.get(`/payments/order/${orderNo}`)
}

export const notifications = {
  my: (limit = 20) => api.get(`/notifications/my?limit=${limit}`),
  unreadCount: () => api.get('/notifications/unread-count'),
  markAllRead: () => api.put('/notifications/mark-all-read'),
  markRead: (id) => api.put(`/notifications/${id}/mark-read`)
}

export default api
