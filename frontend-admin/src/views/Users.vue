<template>
  <div class="page">
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理系统用户和角色</p>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchKey" placeholder="搜索用户名/姓名/手机" clearable style="width: 220px" @input="handleSearch" />
      <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 120px" @change="handleFilter">
        <el-option label="普通用户" :value="0" />
        <el-option label="咨询师" :value="1" />
        <el-option label="管理员" :value="2" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="handleFilter">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h3>用户列表 <span class="total-count">共 {{ total }} 条</span></h3>
        <el-button type="primary" @click="fetchUsers" :icon="Refresh">刷新</el-button>
      </div>
      <el-table :data="pagedList" v-loading="loading">
        <el-table-column label="用户名" prop="username" min-width="100" />
        <el-table-column label="姓名" min-width="100">
          <template #default="{ row }">
            {{ row.realName || row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="手机" prop="phone" min-width="120" />
        <el-table-column label="角色" min-width="100">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1 && row.role !== 2" 
              type="danger" 
              size="small"
              @click="updateStatus(row.id, 0)"
            >禁用</el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small"
              @click="updateStatus(row.id, 1)"
            >启用</el-button>
            <span v-if="row.role === 2" class="admin-tip">管理员</span>
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
import { users } from '../api'

const allList = ref([])
const loading = ref(false)
const searchKey = ref('')
const roleFilter = ref(null)
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)

const roleText = (r) => ['普通用户', '咨询师', '管理员'][r]
const roleType = (r) => ['', 'success', 'danger'][r]
const formatDate = (d) => d ? d.replace('T', ' ').substring(0, 16) : ''

const filteredList = computed(() => {
  let result = allList.value
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    result = result.filter(u => 
      u.username?.toLowerCase().includes(key) ||
      u.realName?.toLowerCase().includes(key) ||
      u.phone?.includes(key)
    )
  }
  if (roleFilter.value !== null) {
    result = result.filter(u => u.role === roleFilter.value)
  }
  if (statusFilter.value !== null) {
    result = result.filter(u => u.status === statusFilter.value)
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

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await users.list()
    if (res.code === 200) allList.value = res.data
  } finally {
    loading.value = false
  }
}

const updateStatus = async (id, status) => {
  await users.updateStatus(id, status)
  ElMessage.success('状态已更新')
  fetchUsers()
}

onMounted(fetchUsers)
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
:deep(.el-table th.el-table__cell) { background: #f8fafc; font-weight: 600; color: #475569; }
.admin-tip { font-size: 12px; color: #94a3b8; }
</style>
