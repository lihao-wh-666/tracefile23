<template>
  <div class="page-container">
    <h2 class="page-title">日志归档管理</h2>

    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-hot">
          <div class="stat-icon"><el-icon :size="28"><Sunny /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(statistics.hotCount || 0) }}</div>
            <div class="stat-label">热数据（近7天）</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-warm">
          <div class="stat-icon"><el-icon :size="28"><Cloudy /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(statistics.warmCount || 0) }}</div>
            <div class="stat-label">温数据（7天-3月）</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-cold">
          <div class="stat-icon"><el-icon :size="28"><IceCreamRound /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(statistics.coldCount || 0) }}</div>
            <div class="stat-label">冷数据（3月以上）</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-icon"><el-icon :size="28"><DataLine /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(statistics.totalCount || 0) }}</div>
            <div class="stat-label">日志总量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="policy-card">
          <template #header>
            <div class="card-header">
              <span>存储策略配置</span>
              <el-button type="primary" link size="small" @click="policyEditVisible = true">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
            </div>
          </template>
          <div class="policy-info">
            <div class="policy-item">
              <span class="policy-label">策略名称：</span>
              <span class="policy-value">{{ storagePolicy.policyName || '-' }}</span>
            </div>
            <div class="policy-item">
              <span class="policy-label">热数据保留：</span>
              <span class="policy-value">{{ storagePolicy.hotDays || 7 }} 天</span>
            </div>
            <div class="policy-item">
              <span class="policy-label">温数据保留：</span>
              <span class="policy-value">{{ storagePolicy.warmDays || 90 }} 天</span>
            </div>
            <div class="policy-item">
              <span class="policy-label">冷数据保留：</span>
              <span class="policy-value">{{ storagePolicy.coldDays || 1095 }} 天</span>
            </div>
            <div class="policy-item">
              <span class="policy-label">自动归档：</span>
              <el-tag :type="storagePolicy.autoArchiveEnabled === 1 ? 'success' : 'info'" size="small">
                {{ storagePolicy.autoArchiveEnabled === 1 ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
            <div class="policy-item">
              <span class="policy-label">归档时间：</span>
              <span class="policy-value mono-text">{{ storagePolicy.archiveCron || '0 0 2 * * ?' }}</span>
            </div>
            <div class="policy-item">
              <span class="policy-label">文件导出：</span>
              <el-tag :type="storagePolicy.fileExportEnabled === 1 ? 'success' : 'info'" size="small">
                {{ storagePolicy.fileExportEnabled === 1 ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
            <div class="policy-item">
              <span class="policy-label">完整性校验：</span>
              <el-tag :type="storagePolicy.integrityVerifyEnabled === 1 ? 'success' : 'info'" size="small">
                {{ storagePolicy.integrityVerifyEnabled === 1 ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card shadow="hover" class="action-card">
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="handleCreateTask(1)">
              <el-icon><Upload /></el-icon>热 → 温 归档
            </el-button>
            <el-button type="warning" size="large" @click="handleCreateTask(2)">
              <el-icon><UploadFilled /></el-icon>温 → 冷 归档
            </el-button>
            <el-button type="info" size="large" @click="handleCreateTask(3)" :disabled="storagePolicy.fileExportEnabled !== 1">
              <el-icon><Download /></el-icon>冷 → 文件 导出
            </el-button>
            <el-button type="success" size="large" @click="handleAutoRun">
              <el-icon><MagicStick /></el-icon>一键自动归档
            </el-button>
          </div>
          <div class="action-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>归档操作会按存储策略将日志从热表迁移到温表、冷表或文件，以优化数据库性能。</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="任务类型" class="search-item">
              <el-select v-model="searchForm.taskType" placeholder="全部" clearable style="width: 100%">
                <el-option label="热→温" :value="1" />
                <el-option label="温→冷" :value="2" />
                <el-option label="冷→文件" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="任务状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100%">
                <el-option label="待执行" :value="0" />
                <el-option label="执行中" :value="1" />
                <el-option label="成功" :value="2" />
                <el-option label="失败" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="开始日期" class="search-item">
              <el-date-picker v-model="searchForm.startDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="结束日期" class="search-item">
              <el-date-picker v-model="searchForm.endDate" type="date" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshLeft /></el-icon>重置
              </el-button>
              <el-button type="primary" @click="handleCreateTaskDialog">
                <el-icon><Plus /></el-icon>新建归档任务
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <div class="table-header">
        <span class="table-title">归档任务列表</span>
      </div>
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="batchId" label="批次ID" width="200" align="center" show-overflow-tooltip />
          <el-table-column label="任务类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTaskTypeTag(row.taskType)" size="small">
                {{ getTaskTypeName(row.taskType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="迁移方向" width="120" align="center">
            <template #default="{ row }">
              {{ getLevelName(row.sourceLevel) }} → {{ getLevelName(row.targetLevel) }}
            </template>
          </el-table-column>
          <el-table-column label="时间范围" min-width="220" align="center">
            <template #default="{ row }">
              <div class="time-range">
                <div class="time-item">{{ formatDate(row.startTime) }}</div>
                <div class="time-divider">至</div>
                <div class="time-item">{{ formatDate(row.endTime) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="总数" width="80" align="center">
            <template #default="{ row }">{{ formatNumber(row.totalCount) }}</template>
          </el-table-column>
          <el-table-column label="成功数" width="90" align="center">
            <template #default="{ row }">
              <span class="success-text">{{ formatNumber(row.successCount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="失败数" width="90" align="center">
            <template #default="{ row }">
              <span class="fail-text">{{ formatNumber(row.failCount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)" size="small">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="100" align="center">
            <template #default="{ row }">{{ row.operatorName || '系统' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
              <el-button 
                v-if="row.status === 0" 
                type="success" 
                link 
                size="small" 
                @click="handleExecute(row)"
              >执行</el-button>
              <el-button 
                v-if="row.status === 2" 
                type="warning" 
                link 
                size="small" 
                @click="handleVerify(row)"
              >校验</el-button>
              <el-button 
                v-if="row.status === 2 && row.taskType !== 3" 
                type="info" 
                link 
                size="small" 
                @click="handleExport(row)"
              >导出</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        class="pagination-wrap"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="createTaskVisible" title="新建归档任务" width="500px">
      <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="100px">
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="taskForm.taskType" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="热表 → 温表（近期日志归档）" :value="1" />
            <el-option label="温表 → 冷表（历史日志归档）" :value="2" />
            <el-option 
              label="冷表 → 文件（文件导出归档）" 
              :value="3" 
              :disabled="storagePolicy.fileExportEnabled !== 1"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="taskForm.startDate" type="date" placeholder="选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="taskForm.endDate" type="date" placeholder="选择结束日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createTaskVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTask" :loading="submitting">创建并执行</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="归档任务详情" width="700px" class="detail-dialog">
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次ID">
            <span class="mono-text">{{ detailData.batchId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeTag(detailData.taskType)">
              {{ getTaskTypeName(detailData.taskType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="迁移方向">
            {{ getLevelName(detailData.sourceLevel) }} → {{ getLevelName(detailData.targetLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusTag(detailData.status)">
              {{ getStatusName(detailData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="时间范围" :span="2">
            {{ formatDate(detailData.startTime) }} 至 {{ formatDate(detailData.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="总记录数">
            <span class="stat-num">{{ formatNumber(detailData.totalCount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="成功数">
            <span class="stat-num success-text">{{ formatNumber(detailData.successCount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败数">
            <span class="stat-num fail-text">{{ formatNumber(detailData.failCount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">
            {{ detailData.operatorName || '系统' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detailData.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="执行开始时间">
            {{ detailData.executeStartTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="执行结束时间">
            {{ detailData.executeEndTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.filePath" label="文件路径" :span="2">
            <span class="mono-text">{{ detailData.filePath }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.fileSize" label="文件大小">
            {{ formatFileSize(detailData.fileSize) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detailData.fileChecksum" label="文件校验值">
            <span class="mono-text small-text">{{ detailData.fileChecksum }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.errorMsg" class="error-section">
          <h4 class="error-title"><el-icon><Warning /></el-icon>错误信息</h4>
          <div class="error-content">{{ detailData.errorMsg }}</div>
        </div>

        <div v-if="detailData.status === 2 && detailData.failCount > 0" class="fail-log-section">
          <h4 class="fail-log-title"><el-icon><CircleClose /></el-icon>失败日志记录</h4>
          <el-alert 
            title="失败记录将记录在错误信息中，可通过批次ID在日志系统中追溯详细失败原因" 
            type="warning" 
            :closable="false"
            show-icon
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="detailData?.status === 0" 
          type="success" 
          @click="handleExecute(detailData)"
        >执行任务</el-button>
        <el-button 
          v-if="detailData?.status === 2" 
          type="warning" 
          @click="handleVerify(detailData)"
        >完整性校验</el-button>
        <el-button 
          v-if="detailData?.status === 2 && detailData?.taskType !== 3" 
          type="primary" 
          @click="handleExport(detailData)"
        >导出日志</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="policyEditVisible" title="编辑存储策略" width="560px">
      <el-form :model="policyForm" label-width="130px">
        <el-form-item label="策略名称">
          <el-input v-model="policyForm.policyName" />
        </el-form-item>
        <el-form-item label="热数据保留天数">
          <el-input-number v-model="policyForm.hotDays" :min="1" :max="30" style="width: 100%" />
        </el-form-item>
        <el-form-item label="温数据保留天数">
          <el-input-number v-model="policyForm.warmDays" :min="7" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item label="冷数据保留天数">
          <el-input-number v-model="policyForm.coldDays" :min="30" :max="3650" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用自动归档">
          <el-switch v-model="policyForm.autoArchiveEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="归档Cron表达式">
          <el-input v-model="policyForm.archiveCron" placeholder="0 0 2 * * ?" />
        </el-form-item>
        <el-form-item label="启用文件导出">
          <el-switch v-model="policyForm.fileExportEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="文件存储路径">
          <el-input v-model="policyForm.fileStoragePath" placeholder="/data/log-archive" />
        </el-form-item>
        <el-form-item label="启用完整性校验">
          <el-switch v-model="policyForm.integrityVerifyEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="归档后删除源数据">
          <el-switch v-model="policyForm.deleteAfterArchive" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="每批处理记录数">
          <el-input-number v-model="policyForm.batchSize" :min="100" :max="10000" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="policyForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="policyEditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePolicy" :loading="policySaving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="verifyVisible" title="完整性校验结果" width="560px">
      <el-result
        v-if="verifyResult.isValid === true"
        icon="success"
        title="归档完整性校验通过"
        :sub-title="`共校验 ${verifyResult.total || 0} 条记录，全部有效`"
      />
      <el-result
        v-else-if="verifyResult.isValid === false"
        icon="error"
        title="检测到归档数据异常！"
        :sub-title="`共校验 ${verifyResult.total || 0} 条，存在异常记录`"
      />
      <el-result
        v-else
        icon="warning"
        title="校验中..."
        sub-title="请稍候"
      />
      <template #footer>
        <el-button @click="verifyVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshLeft, Plus, Edit, Upload, UploadFilled, Download,
  Sunny, Cloudy, IceCreamRound, DataLine, MagicStick, InfoFilled,
  Warning, CircleClose
} from '@element-plus/icons-vue'
import {
  getStoragePolicy,
  updateStoragePolicy,
  getStorageStatistics,
  createArchiveTask,
  executeArchiveTask,
  listArchiveTasks,
  getArchiveTaskDetail,
  verifyArchiveIntegrity,
  exportArchivedLogs,
  manualTriggerAutoArchive
} from '../../api/logArchive'

const searchForm = reactive({
  taskType: null,
  status: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const statistics = reactive({ hotCount: 0, warmCount: 0, coldCount: 0, totalCount: 0 })
const storagePolicy = reactive({})

const createTaskVisible = ref(false)
const taskFormRef = ref(null)
const taskForm = reactive({ taskType: 1, startDate: null, endDate: null })
const taskRules = {
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}
const submitting = ref(false)

const detailVisible = ref(false)
const detailData = ref(null)

const policyEditVisible = ref(false)
const policyForm = reactive({})
const policySaving = ref(false)

const verifyVisible = ref(false)
const verifyResult = reactive({ isValid: null, total: 0 })

const taskTypeNames = { 1: '热→温', 2: '温→冷', 3: '冷→文件' }
const taskTypeTags = { 1: 'primary', 2: 'warning', 3: 'info' }

const statusNames = { 0: '待执行', 1: '执行中', 2: '成功', 3: '失败' }
const statusTags = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }

const levelNames = { 1: '热表', 2: '温表', 3: '冷表', 4: '文件' }

const getTaskTypeName = (type) => taskTypeNames[type] || '未知'
const getTaskTypeTag = (type) => taskTypeTags[type] || 'info'
const getStatusName = (status) => statusNames[status] || '未知'
const getStatusTag = (status) => statusTags[status] || 'info'
const getLevelName = (level) => levelNames[level] || '未知'

const formatNumber = (num) => {
  if (num == null) return '0'
  return num.toLocaleString()
}

const formatDate = (date) => {
  if (!date) return '-'
  return date
}

const formatFileSize = (bytes) => {
  if (bytes == null) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const fetchData = async () => {
  const params = {
    current: pagination.page,
    size: pagination.size,
    taskType: searchForm.taskType,
    status: searchForm.status,
    startDate: searchForm.startDate ? searchForm.startDate.toISOString().slice(0, 10) : undefined,
    endDate: searchForm.endDate ? searchForm.endDate.toISOString().slice(0, 10) : undefined
  }
  const res = await listArchiveTasks(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const fetchStatistics = async () => {
  try {
    const res = await getStorageStatistics()
    const data = res.data || {}
    statistics.hotCount = data.hotCount || 0
    statistics.warmCount = data.warmCount || 0
    statistics.coldCount = data.coldCount || 0
    statistics.totalCount = data.totalCount || 0
  } catch (e) {
    console.error('Load statistics failed:', e)
  }
}

const fetchStoragePolicy = async () => {
  try {
    const res = await getStoragePolicy()
    Object.assign(storagePolicy, res.data || {})
  } catch (e) {
    console.error('Load storage policy failed:', e)
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    taskType: null, status: null, startDate: null, endDate: null
  })
  pagination.page = 1
  fetchData()
}

const handleCreateTaskDialog = () => {
  Object.assign(taskForm, { taskType: 1, startDate: null, endDate: null })
  createTaskVisible.value = true
}

const handleCreateTask = (type) => {
  Object.assign(taskForm, { taskType: type, startDate: null, endDate: null })
  createTaskVisible.value = true
}

const handleSubmitTask = async () => {
  if (!taskFormRef.value) return
  try {
    await taskFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const params = {
      taskType: taskForm.taskType,
      startDate: taskForm.startDate.toISOString().slice(0, 10),
      endDate: taskForm.endDate.toISOString().slice(0, 10)
    }
    const res = await createArchiveTask(params)
    const batchId = res.data?.batchId
    if (batchId) {
      ElMessage.success('归档任务创建成功，开始执行...')
      createTaskVisible.value = false
      await executeArchiveTask(batchId)
      ElMessage.success('归档任务执行完成')
      Promise.all([fetchData(), fetchStatistics()])
    }
  } catch (e) {
    ElMessage.error(e.message || '创建归档任务失败')
  } finally {
    submitting.value = false
  }
}

const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要执行归档任务 ${row.batchId} 吗？`,
      '确认执行',
      { type: 'warning' }
    )
  } catch {
    return
  }

  try {
    const res = await executeArchiveTask(row.batchId)
    if (res.data?.success) {
      ElMessage.success('归档任务执行成功')
    } else {
      ElMessage.warning(res.data?.message || '归档任务执行失败')
    }
    Promise.all([fetchData(), fetchStatistics()])
    if (detailVisible.value) {
      fetchDetail(row.batchId)
    }
  } catch (e) {
    ElMessage.error(e.message || '执行归档任务失败')
  }
}

const handleDetail = async (row) => {
  await fetchDetail(row.batchId)
  detailVisible.value = true
}

const fetchDetail = async (batchId) => {
  try {
    const res = await getArchiveTaskDetail(batchId)
    detailData.value = res.data
  } catch (e) {
    ElMessage.error('获取任务详情失败')
  }
}

const handleVerify = async (row) => {
  verifyResult.isValid = null
  verifyResult.total = 0
  verifyVisible.value = true
  try {
    const res = await verifyArchiveIntegrity(row.batchId)
    const data = res.data || {}
    verifyResult.isValid = data.isValid
    verifyResult.total = data.total || 0
  } catch (e) {
    verifyResult.isValid = false
    ElMessage.error('校验失败')
  }
}

const handleExport = async (row) => {
  try {
    const res = await exportArchivedLogs(row.batchId)
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    const fileName = `archive_log_${row.batchId}.csv`
    link.download = fileName
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const handleAutoRun = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要执行一键自动归档吗？系统将按照存储策略自动执行所有层级的归档。',
      '确认归档',
      { type: 'warning' }
    )
  } catch {
    return
  }

  try {
    const res = await manualTriggerAutoArchive()
    const data = res.data || {}
    let successCount = 0
    let failCount = 0
    if (data.hotToWarm?.success) successCount++
    else failCount++
    if (data.warmToCold?.success) successCount++
    else failCount++
    if (data.coldToFile) {
      if (data.coldToFile.success) successCount++
      else failCount++
    }
    ElMessage.success(`自动归档完成：成功 ${successCount} 个，失败 ${failCount} 个`)
    Promise.all([fetchData(), fetchStatistics()])
  } catch (e) {
    ElMessage.error(e.message || '自动归档失败')
  }
}

const handleSavePolicy = async () => {
  policySaving.value = true
  try {
    const res = await updateStoragePolicy(policyForm)
    Object.assign(storagePolicy, res.data || {})
    ElMessage.success('存储策略保存成功')
    policyEditVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    policySaving.value = false
  }
}

const openPolicyEdit = () => {
  Object.assign(policyForm, { ...storagePolicy })
  policyEditVisible.value = true
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchStatistics(), fetchStoragePolicy()])
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px !important;
  border-radius: 8px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0;
  width: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.stat-hot .stat-icon {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa502 100%);
}

.stat-warm .stat-icon {
  background: linear-gradient(135deg, #70a1ff 0%, #5352ed 100%);
}

.stat-cold .stat-icon {
  background: linear-gradient(135deg, #5f27cd 0%, #341f97 100%);
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #00d2d3 0%, #01a3a4 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-regular);
  margin-top: 4px;
}

.policy-card, .action-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: var(--text-primary);
}

.policy-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.policy-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.policy-label {
  color: var(--text-regular);
}

.policy-value {
  color: var(--text-primary);
  font-weight: 500;
}

.mono-text {
  font-family: 'Consolas', 'Monaco', monospace;
  word-break: break-all;
}

.small-text {
  font-size: 12px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 140px;
}

.action-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-regular);
}

.action-tip .el-icon {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--color-primary);
}

.search-card, .table-card {
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
}

.table-header {
  margin-bottom: 12px;
}

.table-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.time-divider {
  color: var(--text-placeholder);
}

.success-text {
  color: #67C23A;
  font-weight: 600;
}

.fail-text {
  color: #F56C6C;
  font-weight: 600;
}

.stat-num {
  font-size: 16px;
  font-weight: 600;
}

.detail-content {
  margin-bottom: 16px;
}

.error-section {
  margin-top: 16px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fbc4c4;
}

.error-title {
  margin: 0 0 8px 0;
  color: #F56C6C;
  display: flex;
  align-items: center;
  gap: 6px;
}

.error-content {
  color: #606266;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  white-space: pre-wrap;
}

.fail-log-section {
  margin-top: 16px;
}

.fail-log-title {
  margin: 0 0 12px 0;
  color: #E6A23C;
  display: flex;
  align-items: center;
  gap: 6px;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(50% - 4px);
  }
  
  .action-buttons .el-button {
    flex: 1 1 calc(50% - 6px);
    min-width: auto;
  }
}
</style>
