<template>
  <div class="page-container">
    <h2 class="page-title">用户管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="关键字" class="search-item">
              <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机号/邮箱" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="角色" class="search-item">
              <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 100%">
                <el-option label="管理员" :value="1" />
                <el-option label="教师" :value="2" />
                <el-option label="学生" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
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
          <el-table-column prop="username" label="用户名" width="120" align="center" />
          <el-table-column prop="realName" label="姓名" width="100" align="center" />
          <el-table-column prop="role" label="角色" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="roleTagMap[row.role]" size="small">{{ roleMap[row.role] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" align="center" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                :loading="statusLoadingMap[row.id]"
                @change="(val) => handleStatusChange(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
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
          <div class="item-left">
            <span class="item-username">{{ item.username }}</span>
            <el-tag :type="roleTagMap[item.role]" size="small">{{ roleMap[item.role] }}</el-tag>
          </div>
          <el-switch
            v-model="item.status"
            :active-value="1"
            :inactive-value="0"
            :loading="statusLoadingMap[item.id]"
            size="small"
            @change="(val) => handleStatusChange(item, val)"
          />
        </div>
        <div class="item-info">
          <div class="info-row">
            <span class="info-label">姓名:</span>
            <span class="info-value">{{ item.realName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机:</span>
            <span class="info-value">{{ item.phone || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">邮箱:</span>
            <span class="info-value">{{ item.email || '-' }}</span>
          </div>
        </div>
        <div class="item-actions">
          <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog" destroy-on-close width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px" class="responsive-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          <div class="form-tip">{{ isEdit ? '不修改请留空' : '请设置初始密码' }}</div>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="学生" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
  getUserPage,
  getUserDetail,
  addUser,
  updateUser,
  deleteUser,
  updateUserStatus
} from '../../api/user'
import { getPublicKey } from '../../api/auth'
import { encryptPassword } from '../../utils/rsa'

const roleMap = { 1: '管理员', 2: '教师', 3: '学生' }
const roleTagMap = { 1: 'danger', 2: 'warning', 3: 'info' }

const searchForm = reactive({ keyword: '', role: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const statusLoadingMap = reactive({})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)
const publicKey = ref('')

const getDefaultForm = () => ({
  username: '',
  password: '',
  realName: '',
  role: 3,
  phone: '',
  email: ''
})

const form = reactive(getDefaultForm())

const validatePassword = (rule, value, callback) => {
  if (!isEdit.value && !value) {
    callback(new Error('请输入密码'))
  } else if (value && value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const fetchData = async () => {
  const params = {
    current: pagination.page,
    size: pagination.size,
    keyword: searchForm.keyword || undefined,
    role: searchForm.role || undefined,
    status: searchForm.status !== '' ? searchForm.status : undefined
  }
  const res = await getUserPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const fetchPublicKey = async () => {
  const res = await getPublicKey()
  publicKey.value = res.data?.publicKey || ''
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增用户'
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑用户'
  const res = await getUserDetail(row.id)
  Object.assign(form, {
    username: res.data.username,
    password: '',
    realName: res.data.realName,
    role: res.data.role,
    phone: res.data.phone,
    email: res.data.email
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const formData = { ...form }
    if (formData.password) {
      formData.password = encryptPassword(publicKey.value, formData.password)
    }
    if (isEdit.value) {
      await updateUser(editingId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addUser(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？删除后不可恢复。`, '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true
  })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

const handleStatusChange = async (row, status) => {
  statusLoadingMap[row.id] = true
  try {
    const action = status === 1 ? '启用' : '禁用'
    await updateUserStatus(row.id, status)
    ElMessage.success(`${action}成功`)
  } catch (err) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(err?.message || '操作失败')
  } finally {
    statusLoadingMap[row.id] = false
  }
}

onMounted(() => {
  fetchPublicKey()
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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

.item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-username {
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
  min-width: 50px;
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
