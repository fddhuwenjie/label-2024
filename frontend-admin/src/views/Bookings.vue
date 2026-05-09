<template>
  <div class="page">
    <div class="page-header">
      <h1>预约管理</h1>
      <p>查看和处理所有预约订单</p>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchKey" placeholder="搜索用户/咨询师" clearable style="width: 200px" @input="handleSearch" />
      <el-date-picker v-model="dateFilter" type="date" placeholder="预约日期" clearable value-format="YYYY-MM-DD" @change="handleFilter" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="handleFilter">
        <el-option label="待确认" :value="0" />
        <el-option label="已确认" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h3>预约列表 <span class="total-count">共 {{ total }} 条</span></h3>
        <el-button type="primary" @click="fetchList" :icon="Refresh">刷新</el-button>
      </div>
      <el-table :data="pagedList" v-loading="loading">
        <el-table-column label="用户" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar user">{{ row.user?.realName?.charAt(0) || 'U' }}</div>
              <span>{{ row.user?.realName || '用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="咨询师" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar counselor">{{ row.counselor?.user?.realName?.charAt(0) || 'C' }}</div>
              <span>{{ row.counselor?.user?.realName || '咨询师' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约日期" prop="bookingDate" min-width="110" />
        <el-table-column label="时间段" prop="timeSlot" min-width="100" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="statusClass(row.status)">{{ statusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="notes" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="updateStatus(row.id, 1)">确认</el-button>
            <el-button v-if="row.status === 1" type="primary" size="small" @click="updateStatus(row.id, 2)">完成</el-button>
            <el-button v-if="row.status < 2" type="danger" size="small" @click="updateStatus(row.id, 3)">取消</el-button>
            <span v-if="row.status >= 2" class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { bookings } from '../api'

const allList = ref([])
const loading = ref(false)
const searchKey = ref('')
const dateFilter = ref(null)
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)

const statusText = (s) => ['待确认', '已确认', '已完成', '已取消'][s]
const statusClass = (s) => ['pending', 'confirmed', 'completed', 'cancelled'][s]

const filteredList = computed(() => {
  let result = allList.value
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    result = result.filter(b => 
      b.user?.realName?.toLowerCase().includes(key) || 
      b.counselor?.user?.realName?.toLowerCase().includes(key)
    )
  }
  if (dateFilter.value) {
    result = result.filter(b => b.bookingDate === dateFilter.value)
  }
  if (statusFilter.value !== null) {
    result = result.filter(b => b.status === statusFilter.value)
  }
  return result
})

const total = computed(() => filteredList.value.length)
const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const handleSearch = () => { currentPage.value = 1 }
const handleFilter = () => { currentPage.value = 1 }
const handlePageChange = (page) => { currentPage.value = page }
const handleSizeChange = (size) => { pageSize.value = size; currentPage.value = 1 }

const fetchList = async () => {
  loading.value = true
  try {
    const res = await bookings.all()
    if (res.code === 200) allList.value = res.data
  } finally {
    loading.value = false
  }
}

const updateStatus = async (id, status) => {
  await bookings.updateStatus(id, status)
  ElMessage.success('状态已更新')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.page { min-height: 100%; padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }
.filter-bar { margin-bottom: 16px; display: flex; gap: 12px; }
.table-card { background: white; border-radius: 20px; border: 1px solid #e2e8f0; overflow: hidden; }
.table-header { padding: 24px 28px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.table-header h3 { font-size: 18px; font-weight: 700; color: #0f172a; }
.total-count { font-size: 14px; font-weight: 400; color: #64748b; margin-left: 8px; }
.pagination-wrapper { padding: 16px 28px; display: flex; justify-content: flex-end; border-top: 1px solid #e2e8f0; background: white; }
.user-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; }
.avatar.user { background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); }
.avatar.counselor { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
.status-badge { padding: 4px 12px; border-radius: 100px; font-size: 12px; font-weight: 600; }
.status-badge.pending { background: #fef3c7; color: #d97706; }
.status-badge.confirmed { background: #d1fae5; color: #059669; }
.status-badge.completed { background: #e0e7ff; color: #4f46e5; }
.status-badge.cancelled { background: #fee2e2; color: #dc2626; }
.no-action { color: #cbd5e1; }
:deep(.el-table th.el-table__cell) { background: #f8fafc; font-weight: 600; color: #475569; }
</style>
