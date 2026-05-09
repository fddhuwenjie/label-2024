<template>
  <div class="page">
    <div class="page-header">
      <h1>时段管理</h1>
      <p>为咨询师设置可预约时段</p>
    </div>

    <div class="default-slots-info">
      <el-icon><InfoFilled /></el-icon>
      <span>默认时段（咨询师未设置时使用）：</span>
      <span class="slot-tag" v-for="slot in defaultTimeSlots" :key="slot">{{ slot }}</span>
    </div>

    <div class="filter-bar">
      <el-select v-model="selectedCounselor" placeholder="选择咨询师" @change="handleCounselorChange" style="width: 200px">
        <el-option v-for="c in counselorList" :key="c.id" :label="c.user?.realName" :value="c.id" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px" @change="fetchSlots">
        <el-option label="可预约" :value="0" />
        <el-option label="已预约" :value="1" />
        <el-option label="不可用" :value="2" />
      </el-select>
      <el-button type="primary" @click="openBatchDialog(0)" :disabled="!selectedCounselor">批量添加时段</el-button>
      <el-button type="warning" @click="openBatchDialog(2)" :disabled="!selectedCounselor">批量禁用时段</el-button>
    </div>

    <div class="table-card" v-if="selectedCounselor">
      <div class="table-header">
        <h3>时段列表 <span class="total-count">共 {{ total }} 条</span></h3>
        <el-button @click="fetchSlots" :icon="Refresh">刷新</el-button>
      </div>
      <el-table :data="pagedSlots" v-loading="loading">
        <el-table-column label="日期" prop="date" min-width="120" />
        <el-table-column label="开始时间" prop="startTime" min-width="100" />
        <el-table-column label="结束时间" prop="endTime" min-width="100" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'info' : 'danger'">
              {{ ['可预约', '已预约', '不可用'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="warning" size="small" @click="updateSlotStatus(row.id, 2)">禁用</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="updateSlotStatus(row.id, 0)">启用</el-button>
            <el-button type="danger" size="small" @click="deleteSlot(row.id)" :disabled="row.status === 1">删除</el-button>
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

    <el-dialog v-model="batchDialogVisible" :title="batchForm.status === 0 ? '批量添加时段' : '批量禁用时段'" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="日期范围">
          <el-date-picker 
            v-model="batchForm.dateRange" 
            type="daterange" 
            start-placeholder="开始日期" 
            end-placeholder="结束日期" 
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="时段">
          <div class="time-slots-grid">
            <el-checkbox-group v-model="batchForm.timeSlots">
              <el-checkbox label="09:00-10:00">09:00-10:00</el-checkbox>
              <el-checkbox label="10:00-11:00">10:00-11:00</el-checkbox>
              <el-checkbox label="11:00-12:00">11:00-12:00</el-checkbox>
              <el-checkbox label="14:00-15:00">14:00-15:00</el-checkbox>
              <el-checkbox label="15:00-16:00">15:00-16:00</el-checkbox>
              <el-checkbox label="16:00-17:00">16:00-17:00</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button :type="batchForm.status === 0 ? 'primary' : 'warning'" @click="saveBatch">
          {{ batchForm.status === 0 ? '添加' : '禁用' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, InfoFilled } from '@element-plus/icons-vue'
import { counselors, timeSlots } from '../api'

const counselorList = ref([])
const selectedCounselor = ref(null)
const allSlots = ref([])
const loading = ref(false)
const batchDialogVisible = ref(false)
const batchForm = ref({ dateRange: [], timeSlots: [], status: 0 })

// 默认时间段
const defaultTimeSlots = [
  '09:00-10:00', '10:00-11:00', '11:00-12:00',
  '14:00-15:00', '15:00-16:00', '16:00-17:00'
]

// 日期限制：只能选今天到30天后
const maxDate = new Date()
maxDate.setDate(maxDate.getDate() + 30)
maxDate.setHours(23, 59, 59, 999)

const disabledDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today || date > maxDate
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref(null)

// 过滤后的数据
const filteredSlots = computed(() => {
  if (statusFilter.value === null) return allSlots.value
  return allSlots.value.filter(s => s.status === statusFilter.value)
})

// 总数
const total = computed(() => filteredSlots.value.length)

// 当前页数据
const pagedSlots = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredSlots.value.slice(start, start + pageSize.value)
})

const fetchCounselors = async () => {
  const res = await counselors.listAll()
  if (res.code === 200) counselorList.value = res.data
}

const fetchSlots = async () => {
  if (!selectedCounselor.value) return
  loading.value = true
  try {
    const res = await timeSlots.listByCounselor(selectedCounselor.value)
    if (res.code === 200) allSlots.value = res.data
  } finally {
    loading.value = false
  }
}

const handleCounselorChange = () => {
  currentPage.value = 1
  fetchSlots()
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const openBatchDialog = (status) => {
  batchForm.value = { dateRange: [], timeSlots: [], status }
  batchDialogVisible.value = true
}

const saveBatch = async () => {
  const { dateRange, timeSlots: times, status } = batchForm.value
  if (!dateRange?.length || !times.length) {
    ElMessage.warning('请选择日期和时段')
    return
  }
  
  const params = {
    counselorId: selectedCounselor.value,
    startDate: dateRange[0],
    endDate: dateRange[1],
    timeSlots: times,
    status
  }
  
  await timeSlots.batchCreate(params)
  ElMessage.success(status === 0 ? '时段添加成功' : '时段已禁用')
  batchDialogVisible.value = false
  fetchSlots()
}

const updateSlotStatus = async (id, status) => {
  await timeSlots.updateStatus(id, status)
  ElMessage.success('状态已更新')
  fetchSlots()
}

const deleteSlot = async (id) => {
  await timeSlots.delete(id)
  ElMessage.success('已删除')
  fetchSlots()
}

onMounted(fetchCounselors)
</script>

<style scoped>
.page { min-height: 100%; padding: 32px; }
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.page-header p { font-size: 14px; color: #64748b; }
.default-slots-info { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  padding: 12px 16px; 
  background: #f0f9ff; 
  border: 1px solid #bae6fd; 
  border-radius: 10px; 
  margin-bottom: 16px; 
  font-size: 14px; 
  color: #0369a1; 
}
.default-slots-info .el-icon { font-size: 16px; }
.slot-tag { 
  padding: 4px 10px; 
  background: white; 
  border-radius: 6px; 
  font-size: 13px; 
  color: #0284c7; 
  border: 1px solid #7dd3fc; 
}
.filter-bar { margin-bottom: 16px; display: flex; gap: 12px; }
.table-card { background: white; border-radius: 20px; border: 1px solid #e2e8f0; overflow: hidden; }
.table-header { padding: 24px 28px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.table-header h3 { font-size: 18px; font-weight: 700; color: #0f172a; }
.total-count { font-size: 14px; font-weight: 400; color: #64748b; margin-left: 8px; }
.pagination-wrapper { padding: 16px 28px; display: flex; justify-content: flex-end; border-top: 1px solid #e2e8f0; background: white; }
:deep(.el-table th.el-table__cell) { background: #f8fafc; font-weight: 600; color: #475569; }
.time-slots-grid { width: 100%; }
.time-slots-grid :deep(.el-checkbox-group) { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.time-slots-grid :deep(.el-checkbox) { margin-right: 0; }
</style>
