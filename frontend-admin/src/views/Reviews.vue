<template>
  <div class="page">
    <div class="page-header">
      <h1>评价管理</h1>
      <p>查看和管理用户评价</p>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchKey" placeholder="搜索用户/评价内容" clearable style="width: 220px" @input="handleSearch" />
      <el-select v-model="ratingFilter" placeholder="评分筛选" clearable style="width: 120px" @change="handleFilter">
        <el-option label="5星" :value="5" />
        <el-option label="4星" :value="4" />
        <el-option label="3星" :value="3" />
        <el-option label="2星" :value="2" />
        <el-option label="1星" :value="1" />
      </el-select>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h3>评价列表 <span class="total-count">共 {{ total }} 条</span></h3>
        <el-button type="primary" @click="fetchList" :icon="Refresh">刷新</el-button>
      </div>
      <el-table :data="pagedList" v-loading="loading">
        <el-table-column label="用户" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar">{{ row.user?.realName?.charAt(0) || 'U' }}</div>
              <span>{{ row.user?.realName || '用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" min-width="120">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled :max="5" />
          </template>
        </el-table-column>
        <el-table-column label="评价内容" prop="content" min-width="250" show-overflow-tooltip />
        <el-table-column label="预约日期" min-width="110">
          <template #default="{ row }">{{ row.booking?.bookingDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="评价时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此评价？" @confirm="deleteReview(row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
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
import { reviews } from '../api'

const allList = ref([])
const loading = ref(false)
const searchKey = ref('')
const ratingFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)

const formatDate = (d) => d ? d.replace('T', ' ').substring(0, 16) : ''

const filteredList = computed(() => {
  let result = allList.value
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    result = result.filter(r => 
      r.user?.realName?.toLowerCase().includes(key) || 
      r.content?.toLowerCase().includes(key)
    )
  }
  if (ratingFilter.value !== null) {
    result = result.filter(r => Math.floor(r.rating) === ratingFilter.value)
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
    const res = await reviews.list()
    if (res.code === 200) allList.value = res.data.map(r => ({ ...r, rating: Number(r.rating) }))
  } finally {
    loading.value = false
  }
}

const deleteReview = async (id) => {
  await reviews.delete(id)
  ElMessage.success('已删除')
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
.avatar { width: 36px; height: 36px; background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; }
:deep(.el-table th.el-table__cell) { background: #f8fafc; font-weight: 600; color: #475569; }
:deep(.el-rate) { --el-rate-icon-size: 16px; }
</style>
