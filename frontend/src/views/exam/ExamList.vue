<template>
  <div class="page-container">
    <h2 class="page-title">考试管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="考试状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="未开始" :value="0" />
                <el-option label="进行中" :value="1" />
                <el-option label="已结束" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="16">
            <div class="search-actions">
              <el-button type="primary" @click="loadData">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button v-if="isAdminOrTeacher" type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                添加考试
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card mobile-hidden">
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="考试名称" min-width="150" />
          <el-table-column prop="paperName" label="试卷名称" min-width="150" />
          <el-table-column prop="startTime" label="开始时间" width="170" />
          <el-table-column prop="endTime" label="结束时间" width="170" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canTakeExam(row)" type="primary" size="small" link @click="handleTake(row)">参加</el-button>
              <el-button size="small" link @click="handleView(row)">查看</el-button>
              <template v-if="isAdminOrTeacher">
                <el-button type="warning" size="small" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination-wrap"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <div class="mobile-list mobile-only">
      <div class="list-item" v-for="item in tableData" :key="item.id">
        <div class="item-header">
          <span class="item-title">{{ item.name }}</span>
          <el-tag :type="statusTagType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
        </div>
        <div class="item-info">
          <span class="info-label">试卷:</span>
          <span class="info-value">{{ item.paperName }}</span>
        </div>
        <div class="item-info">
          <span class="info-label">开始:</span>
          <span class="info-value">{{ item.startTime }}</span>
        </div>
        <div class="item-info">
          <span class="info-label">结束:</span>
          <span class="info-value">{{ item.endTime }}</span>
        </div>
        <div class="item-actions">
          <el-button v-if="canTakeExam(item)" type="primary" size="small" @click="handleTake(item)">参加</el-button>
          <el-button size="small" @click="handleView(item)">查看</el-button>
          <template v-if="isAdminOrTeacher">
            <el-button type="warning" size="small" @click="handleEdit(item)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
          </template>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="responsive-form">
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="试卷" prop="paperId">
          <el-select v-model="form.paperId" placeholder="请选择试卷" style="width: 100%">
            <el-option v-for="p in paperList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="new Date()"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="new Date()"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="考试详情" class="responsive-dialog">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="考试名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="试卷名称">{{ detailData.paperName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getExamPage, addExam, updateExam, deleteExam, getExamDetail } from '../../api/exam'
import { getPaperPage } from '../../api/paper'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const tableData = ref([])
const paperList = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('添加考试')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const detailData = ref({})

const isAdminOrTeacher = computed(() => {
  const role = userStore.userInfo?.role
  return role === 1 || role === 2
})

const searchForm = reactive({ status: null })

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  name: '',
  paperId: null,
  startTime: '',
  endTime: ''
})

const rules = {
  name: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  paperId: [{ required: true, message: '请选择试卷', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const statusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] ?? '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] ?? 'info'
}

const canTakeExam = (row) => {
  if (row.status === 1) return true
  if (row.startTime && row.endTime) {
    const now = new Date().getTime()
    const start = new Date(row.startTime.replace(/-/g, '/')).getTime()
    const end = new Date(row.endTime.replace(/-/g, '/')).getTime()
    return now >= start && now <= end
  }
  return false
}

const loadData = async () => {
  const params = { current: pagination.current, size: pagination.size }
  if (searchForm.status !== null && searchForm.status !== '') {
    params.status = searchForm.status
  }
  const res = await getExamPage(params)
  tableData.value = res.data.records
  pagination.total = res.data.total
}

const loadPapers = async () => {
  const res = await getPaperPage({ current: 1, size: 100 })
  paperList.value = res.data.records
}

const handleReset = () => {
  searchForm.status = null
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '添加考试'
  Object.assign(form, { name: '', paperId: null, startTime: '', endTime: '' })
  dialogVisible.value = true
}

const handleView = async (row) => {
  const res = await getExamDetail(row.id)
  detailData.value = res.data
  detailVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  dialogTitle.value = '编辑考试'
  Object.assign(form, {
    name: row.name,
    paperId: row.paperId,
    startTime: row.startTime,
    endTime: row.endTime
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该考试吗？', '提示', { type: 'warning' })
  await deleteExam(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleTake = (row) => {
  router.push(`/exam/take/${row.id}`)
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...form }
    if (editingId.value) {
      await updateExam(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addExam(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  if (isAdminOrTeacher.value) {
    loadPapers()
  }
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.search-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.search-form {
  margin: 0;
}

.search-item {
  margin-bottom: 0;
}

.search-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-start;
}

.table-card {
  border-radius: 8px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.mobile-list {
  margin-top: 8px;
}

.list-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info {
  display: flex;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-label {
  color: #909399;
  width: 50px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

@media screen and (max-width: 768px) {
  .search-actions {
    width: 100%;
  }

  .search-actions .el-button {
    flex: 1;
    min-width: calc(50% - 4px);
  }
}

@media screen and (max-width: 480px) {
  .item-actions .el-button {
    flex: 1;
    min-width: calc(50% - 4px);
  }
}
</style>
