<template>
  <div class="page">
    <div class="page-header">
      <h1>咨询师管理</h1>
      <p>管理咨询师信息和上下架</p>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchKey" placeholder="搜索咨询师姓名/专长" clearable style="width: 220px" @input="handleSearch" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="handleFilter">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
    </div>

    <div class="table-card">
      <div class="table-header">
        <h3>咨询师列表 <span class="total-count">共 {{ total }} 条</span></h3>
        <div>
          <el-button type="primary" @click="openDialog(null)">添加咨询师</el-button>
          <el-button @click="fetchList" :icon="Refresh">刷新</el-button>
        </div>
      </div>
      <el-table :data="pagedList" v-loading="loading">
        <el-table-column label="咨询师" min-width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="avatar">{{ row.user?.realName?.charAt(0) || 'C' }}</div>
              <span>{{ row.user?.realName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="职称" prop="title" min-width="100" />
        <el-table-column label="专长" prop="specialty" min-width="120" show-overflow-tooltip />
        <el-table-column label="经验" min-width="80">
          <template #default="{ row }">{{ row.experienceYears }}年</template>
        </el-table-column>
        <el-table-column label="价格" min-width="80">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="评分" min-width="80">
          <template #default="{ row }">{{ row.rating || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.available === 1 ? 'success' : 'info'">
              {{ row.available === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button 
              v-if="row.available === 1" 
              type="warning" 
              size="small"
              @click="updateAvailable(row.id, 0)"
            >下架</el-button>
            <el-button 
              v-else 
              type="success" 
              size="small"
              @click="updateAvailable(row.id, 1)"
            >上架</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑咨询师' : '添加咨询师'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择用户" prop="userId" v-if="!form.id">
          <el-select v-model="form.userId" placeholder="请选择要设为咨询师的用户" filterable style="width: 100%" :loading="loadingUsers">
            <el-option v-for="u in availableUsers" :key="u.id" :label="`${u.realName || u.username} (${u.phone || '无手机号'})`" :value="u.id" />
          </el-select>
          <div class="form-tip" v-if="availableUsers.length === 0 && !loadingUsers">暂无可选用户</div>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-input v-model="form.title" placeholder="如：资深心理咨询师" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="专长" prop="specialty">
          <el-input v-model="form.specialty" placeholder="如：情感咨询、职业规划" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="经验年限" prop="experienceYears">
          <el-input-number v-model="form.experienceYears" :min="0" :max="50" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="1" :max="10000" :precision="2" />
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input v-model="form.introduction" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入咨询师简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForm" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { counselors, users } from '../api'

const allList = ref([])
const userList = ref([])
const loading = ref(false)
const loadingUsers = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const form = ref({})
const formRef = ref(null)
const searchKey = ref('')
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)

const availableUsers = computed(() => {
  const counselorUserIds = allList.value.map(c => c.userId)
  return userList.value.filter(u => u.role === 0 && u.status === 1 && !counselorUserIds.includes(u.id))
})

const filteredList = computed(() => {
  let result = allList.value
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    result = result.filter(c => c.user?.realName?.toLowerCase().includes(key) || c.specialty?.toLowerCase().includes(key))
  }
  if (statusFilter.value !== null) {
    result = result.filter(c => c.available === statusFilter.value)
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

const rules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  title: [{ required: true, message: '请输入职称', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入专长领域', trigger: 'blur' }],
  experienceYears: [{ required: true, message: '请输入经验年限', trigger: 'blur' }],
  price: [{ required: true, message: '请输入咨询价格', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await counselors.listAll()
    if (res.code === 200) allList.value = res.data
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await users.list()
    if (res.code === 200) userList.value = res.data
  } finally {
    loadingUsers.value = false
  }
}

const openDialog = async (row) => {
  form.value = row ? { ...row } : { experienceYears: 1, price: 200, available: 1 }
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
  if (!row) await fetchUsers()
}

const saveForm = async () => {
  try { await formRef.value.validate() } catch { return }
  saving.value = true
  try {
    if (form.value.id) {
      await counselors.update(form.value.id, form.value)
    } else {
      await counselors.save(form.value)
      await users.updateRole(form.value.userId, 1)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const updateAvailable = async (id, available) => {
  await counselors.updateAvailable(id, available)
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
.avatar { width: 36px; height: 36px; background: linear-gradient(135deg, #10b981 0%, #34d399 100%); border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; }
:deep(.el-table th.el-table__cell) { background: #f8fafc; font-weight: 600; color: #475569; }
.form-tip { font-size: 12px; color: #f59e0b; margin-top: 8px; }
</style>
