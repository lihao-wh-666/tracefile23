<template>
  <div class="page-container">
    <h2 class="page-title">系统参数管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="配置键名" class="search-item">
              <el-input v-model="searchForm.configKey" placeholder="请输入配置键名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="配置名称" class="search-item">
              <el-input v-model="searchForm.configName" placeholder="请输入配置名称" clearable />
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
                新增参数
              </el-button>
              <el-button type="warning" @click="handleRefreshCache">
                <el-icon><Refresh /></el-icon>
                刷新缓存
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
          <el-table-column prop="configKey" label="配置键名" width="200" align="center" />
          <el-table-column prop="configName" label="配置名称" width="180" align="center" />
          <el-table-column prop="configValue" label="配置值" min-width="150" show-overflow-tooltip />
          <el-table-column label="值类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getValueTypeTag(row.valueType)" size="small">
                {{ getValueTypeName(row.valueType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
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
          <span class="item-name">{{ item.configName }}</span>
          <el-tag :type="getValueTypeTag(item.valueType)" size="small">
            {{ getValueTypeName(item.valueType) }}
          </el-tag>
        </div>
        <div class="item-info">
          <div class="info-row">
            <span class="info-label">键名:</span>
            <span class="info-value">{{ item.configKey }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">键值:</span>
            <span class="info-value">{{ item.configValue }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">描述:</span>
            <span class="info-value">{{ item.description || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">更新时间:</span>
            <span class="info-value">{{ item.updateTime || '-' }}</span>
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
        <el-form-item label="配置键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键名，如：login.timeout.minutes" maxlength="100" show-word-limit :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="值类型" prop="valueType">
          <el-select v-model="form.valueType" placeholder="请选择值类型" style="width: 100%">
            <el-option :value="1" label="字符串" />
            <el-option :value="2" label="整数" />
            <el-option :value="3" label="布尔值" />
            <el-option :value="4" label="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-if="form.valueType !== 4"
            v-model="form.configValue"
            :placeholder="getConfigValuePlaceholder()"
            maxlength="500"
            show-word-limit
          />
          <el-input
            v-else
            v-model="form.configValue"
            type="textarea"
            :rows="4"
            placeholder="请输入JSON格式的配置值"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述（选填）"
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
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import {
  getSystemConfigPage,
  addSystemConfig,
  updateSystemConfig,
  deleteSystemConfig,
  refreshSystemConfigCache
} from '../../api/systemConfig'
import { updateSessionTimeoutConfig } from '../../utils/sessionTimeout'

const searchForm = reactive({ configKey: '', configName: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增参数')
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const getDefaultForm = () => ({
  configKey: '',
  configValue: '',
  configName: '',
  description: '',
  valueType: 1
})

const form = reactive(getDefaultForm())

const formRules = {
  configKey: [
    { required: true, message: '请输入配置键名', trigger: 'blur' },
    { min: 2, max: 100, message: '配置键名长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' },
    { min: 2, max: 200, message: '配置名称长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (form.valueType === 2 && value !== '' && isNaN(Number(value))) {
          callback(new Error('请输入有效的整数'))
        } else if (form.valueType === 3 && value !== '' && !['true', 'false'].includes(value.toLowerCase())) {
          callback(new Error('布尔值请输入 true 或 false'))
        } else if (form.valueType === 4 && value !== '') {
          try {
            JSON.parse(value)
          } catch (e) {
            callback(new Error('请输入有效的JSON格式'))
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  valueType: [
    { required: true, message: '请选择值类型', trigger: 'change' }
  ]
}

const getValueTypeName = (type) => {
  const names = { 1: '字符串', 2: '整数', 3: '布尔', 4: 'JSON' }
  return names[type] || '未知'
}

const getValueTypeTag = (type) => {
  const types = { 1: 'info', 2: 'primary', 3: 'success', 4: 'warning' }
  return types[type] || 'info'
}

const getConfigValuePlaceholder = () => {
  const placeholders = {
    1: '请输入字符串配置值',
    2: '请输入整数配置值',
    3: '请输入布尔配置值（true/false）'
  }
  return placeholders[form.valueType] || '请输入配置值'
}

const fetchData = async () => {
  const params = {
    current: pagination.page,
    size: pagination.size,
    configKey: searchForm.configKey || undefined,
    configName: searchForm.configName || undefined
  }
  const res = await getSystemConfigPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.configKey = ''
  searchForm.configName = ''
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增参数'
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑参数'
  Object.assign(form, {
    configKey: row.configKey,
    configValue: row.configValue,
    configName: row.configName,
    description: row.description || '',
    valueType: row.valueType
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
      await updateSystemConfig(formData)
      ElMessage.success('修改成功')
      if (form.configKey === 'login.timeout.minutes') {
        updateSessionTimeoutConfig(parseInt(form.configValue) || 30)
      }
    } else {
      await addSystemConfig(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除参数 "${row.configName}" 吗？删除后可能会影响系统功能。`, '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true
  })
    .then(async () => {
      await deleteSystemConfig(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

const handleRefreshCache = async () => {
  try {
    await refreshSystemConfigCache()
    ElMessage.success('缓存刷新成功')
    fetchData()
  } catch (e) {
    ElMessage.error('缓存刷新失败')
  }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  min-width: 60px;
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
    min-width: calc(50% - 4px);
  }

  .item-actions .el-button {
    flex: 1;
  }
}
</style>
