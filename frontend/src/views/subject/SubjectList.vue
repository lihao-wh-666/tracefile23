<template>
  <div class="page-container">
    <h2 class="page-title">科目管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="科目名称" class="search-item">
              <el-input v-model="searchForm.name" placeholder="请输入科目名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增科目
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
          <el-table-column prop="name" label="科目名称" width="160" align="center" />
          <el-table-column prop="description" label="科目描述" min-width="280" show-overflow-tooltip />
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination-wrap"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <div class="mobile-list mobile-only">
      <div class="list-item" v-for="item in tableData" :key="item.id">
        <div class="item-header">
          <span class="item-name">{{ item.name }}</span>
        </div>
        <div class="item-info">
          <div class="info-row">
            <span class="info-label">描述:</span>
            <span class="info-value">{{ item.description || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">创建时间:</span>
            <span class="info-value">{{ item.createTime || '-' }}</span>
          </div>
        </div>
        <div class="item-actions">
          <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog" destroy-on-close width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px" class="responsive-form">
        <el-form-item label="科目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入科目名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="科目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入科目描述（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import {
  getSubjectPage,
  addSubject,
  updateSubject,
  deleteSubject
} from '../../api/subject'

const searchForm = reactive({ name: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增科目')
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const getDefaultForm = () => ({
  name: '',
  description: ''
})

const form = reactive(getDefaultForm())

const formRules = {
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '科目名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '科目描述长度不能超过 500 个字符', trigger: 'blur' }
  ]
}

const fetchData = async () => {
  const params = {
    current: pagination.page,
    size: pagination.size,
    name: searchForm.name || undefined
  }
  const res = await getSubjectPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增科目'
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑科目'
  Object.assign(form, {
    name: row.name,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const formData = { ...form }
    if (isEdit.value) {
      formData.id = editingId.value
      await updateSubject(formData)
      ElMessage.success('修改成功')
    } else {
      await addSubject(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除科目 "${row.name}" 吗？删除后该科目的题目和试卷可能会受影响。`, '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true
  })
    .then(async () => {
      await deleteSubject(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
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
  margin-bottom: 12px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.item-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  margin-bottom: 6px;
  font-size: 13px;
}

.info-label {
  color: #909399;
  min-width: 70px;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  flex: 1;
  word-break: break-all;
}

.item-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(33.33% - 6px);
  }

  .item-actions .el-button {
    flex: 1;
  }
}
</style>
