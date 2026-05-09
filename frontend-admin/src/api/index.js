import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

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
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  async error => {
    const originalRequest = error.config
    
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
      
      const refreshToken = localStorage.getItem('admin_refreshToken')
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          const { token, refreshToken: newRefreshToken } = res.data.data
          localStorage.setItem('admin_token', token)
          localStorage.setItem('admin_refreshToken', newRefreshToken)
          onRefreshed(token)
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        } catch (refreshError) {
          localStorage.removeItem('admin_token')
          localStorage.removeItem('admin_refreshToken')
          localStorage.removeItem('admin_user')
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
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken })
}

export const users = {
  me: () => api.get('/users/me'),
  update: (data) => api.put('/users/me', data),
  list: () => api.get('/users'),
  updateStatus: (id, status) => api.put(`/users/${id}/status?status=${status}`),
  updateRole: (id, role) => api.put(`/users/${id}/role?role=${role}`)
}

export const counselors = {
  list: () => api.get('/counselors'),
  listAll: () => api.get('/counselors/all'),
  get: (id) => api.get(`/counselors/${id}`),
  save: (data) => api.post('/counselors', data),
  update: (id, data) => api.put(`/counselors/${id}`, data),
  updateAvailable: (id, available) => api.put(`/counselors/${id}/available?available=${available}`),
  delete: (id) => api.delete(`/counselors/${id}`),
  // 咨询师专属
  me: () => api.get('/counselors/me'),
  updateMe: (data) => api.put('/counselors/me', data)
}

export const bookings = {
  all: () => api.get('/bookings/all'),
  updateStatus: (id, status) => api.put(`/bookings/${id}/status?status=${status}`),
  // 咨询师专属
  counselorBookings: () => api.get('/bookings/counselor'),
  confirm: (id) => api.put(`/bookings/${id}/confirm`),
  complete: (id) => api.put(`/bookings/${id}/complete`),
  reject: (id) => api.put(`/bookings/${id}/reject`)
}

export const timeSlots = {
  listByCounselor: (counselorId) => api.get(`/timeslots/counselor/${counselorId}/all`),
  create: (data) => api.post('/timeslots', data),
  batchCreate: (data) => api.post('/timeslots/batch', data),
  updateStatus: (id, status) => api.put(`/timeslots/${id}/status?status=${status}`),
  delete: (id) => api.delete(`/timeslots/${id}`),
  // 咨询师专属
  my: () => api.get('/timeslots/my'),
  myBatchCreate: (data) => api.post('/timeslots/my/batch', data)
}

export const reviews = {
  list: () => api.get('/reviews'),
  delete: (id) => api.delete(`/reviews/${id}`),
  // 咨询师专属
  my: () => api.get('/reviews/my')
}

export const stats = {
  overview: () => api.get('/stats/overview'),
  bookingTrend: (days = 7) => api.get(`/stats/bookings/trend?days=${days}`),
  // 咨询师专属
  counselorMy: () => api.get('/stats/counselor/my')
}

export default api
