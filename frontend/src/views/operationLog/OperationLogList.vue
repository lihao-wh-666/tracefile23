<template>
  <div class="page-container">
    <h2 class="page-title">操作日志留痕</h2>

    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-icon"><el-icon :size="28"><DataLine /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">总操作数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-success">
          <div class="stat-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.successCount || 0 }}</div>
            <div class="stat-label">成功操作</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-fail">
          <div class="stat-icon"><el-icon :size="28"><CircleClose /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.failCount || 0 }}</div>
            <div class="stat-label">失败操作</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card stat-integrity" @click="handleVerifyIntegrity">
          <div class="stat-icon"><el-icon :size="28"><Lock /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">
              <el-tag v-if="integrityResult.isValid === true" type="success" size="small">完整</el-tag>
              <el-tag v-else-if="integrityResult.isValid === false" type="danger" size="small">异常</el-tag>
              <span v-else class="verify-link">点击校验</span>
            </div>
            <div class="stat-label">日志完整性</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-header">
            <span class="chart-title">操作类型分布</span>
          </div>
          <div ref="opTypeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-header">
            <span class="chart-title">每日操作趋势</span>
          </div>
          <div ref="dateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-header">
            <span class="chart-title">模块操作TOP10</span>
          </div>
          <div ref="moduleChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-header">
            <span class="chart-title">活跃用户TOP10</span>
          </div>
          <div ref="userChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="关键词" class="search-item">
              <el-input v-model="searchForm.keyword" placeholder="用户名/模块/操作/IP" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="操作类型" class="search-item">
              <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 100%">
                <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="模块" class="search-item">
              <el-input v-model="searchForm.module" placeholder="模块名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="操作人" class="search-item">
              <el-input v-model="searchForm.username" placeholder="用户名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="操作对象" class="search-item">
              <el-select v-model="searchForm.targetType" placeholder="全部" clearable style="width: 100%">
                <el-option label="用户" value="user" />
                <el-option label="题目" value="question" />
                <el-option label="试卷" value="paper" />
                <el-option label="考试" value="exam" />
                <el-option label="科目" value="subject" />
                <el-option label="系统配置" value="systemConfig" />
                <el-option label="操作日志" value="operationLog" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100%">
                <el-option label="成功" :value="1" />
                <el-option label="失败" :value="0" />
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
              <el-button type="success" @click="handleExport">
                <el-icon><Download /></el-icon>导出CSV
              </el-button>
              <el-button type="warning" @click="handleVerifyIntegrity">
                <el-icon><Lock /></el-icon>完整性校验
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="createTime" label="操作时间" width="170" align="center" />
          <el-table-column prop="username" label="操作人" width="110" align="center">
            <template #default="{ row }">
              <span>{{ row.username || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.operationType" :type="getOperationTypeTag(row.operationType)" size="small">
                {{ getOperationTypeName(row.operationType) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="module" label="模块" width="110" align="center" show-overflow-tooltip />
          <el-table-column prop="operation" label="操作" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作对象" width="100" align="center">
            <template #default="{ row }">
              <span v-if="row.targetType">
                {{ row.targetType }}
                <span v-if="row.targetId" class="target-id">#{{ row.targetId }}</span>
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址" width="130" align="center" show-overflow-tooltip />
          <el-table-column label="状态" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="780px" class="detail-dialog">
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detailData.username || '-' }} (ID: {{ detailData.userId || '-' }})
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ip || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detailData.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag v-if="detailData.operationType" :type="getOperationTypeTag(detailData.operationType)">
            {{ getOperationTypeName(detailData.operationType) }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作内容" :span="2">{{ detailData.operation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作对象">
          <span v-if="detailData.targetType">
            {{ detailData.targetType }} #{{ detailData.targetId || '-' }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
            {{ detailData.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="方法路径" :span="2">
          <span class="mono-text">{{ detailData.method || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="链路ID" :span="2">
          <span class="mono-text">{{ detailData.traceId || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">
          <div style="max-height: 60px; overflow-y: auto;">
            <span class="mono-text small-text">{{ detailData.userAgent || '-' }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre v-if="detailData.params" class="json-pre">{{ formatJson(detailData.params) }}</pre>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.status === 0" label="错误信息" :span="2">
          <span class="error-text">{{ detailData.errorMsg || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.beforeState || detailData.afterState" label="操作前状态" :span="2">
          <pre v-if="detailData.beforeState" class="json-pre before-json">{{ formatJson(detailData.beforeState) }}</pre>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.beforeState || detailData.afterState" label="操作后状态" :span="2">
          <pre v-if="detailData.afterState" class="json-pre after-json">{{ formatJson(detailData.afterState) }}</pre>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="哈希校验值" :span="2">
          <span class="mono-text small-text checksum-text">{{ detailData.checksum || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="前记录哈希" :span="2">
          <span class="mono-text small-text">{{ detailData.previousChecksum || '(第一条记录)' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="integrityVisible" title="日志完整性校验结果" width="600px">
      <el-result
        v-if="integrityResult.isValid === true"
        icon="success"
        title="日志完整性校验通过"
        :sub-title="`共校验 ${integrityResult.total} 条记录，全部有效`"
      />
      <el-result
        v-else-if="integrityResult.isValid === false"
        icon="error"
        title="检测到日志异常！"
        :sub-title="`共校验 ${integrityResult.total} 条，有效 ${integrityResult.valid} 条，异常 ${(integrityResult.errors || []).length} 条`"
      />
      <el-result
        v-else
        icon="warning"
        title="暂未执行校验"
        sub-title="请点击下方按钮开始校验"
      />
      <div v-if="(integrityResult.errors || []).length > 0" class="error-list-wrap">
        <h4 class="error-list-title">异常记录明细：</h4>
        <el-table :data="integrityResult.errors" border size="small">
          <el-table-column prop="id" label="日志ID" width="100" align="center" />
          <el-table-column label="内容校验" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.checksumMatch ? 'success' : 'danger'" size="small">
                {{ row.checksumMatch ? '通过' : '被篡改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="链路校验" align="center">
            <template #default="{ row }">
              <el-tag :type="row.prevChainMatch ? 'success' : 'danger'" size="small">
                {{ row.prevChainMatch ? '完整' : '断裂' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="integrityVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleVerifyIntegrity">重新校验</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshLeft, Download, Lock, DataLine,
  CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getOperationLogPage,
  getOperationLogDetail,
  getOperationLogStatistics,
  verifyOperationLogIntegrity,
  exportOperationLog
} from '../../api/operationLog'

const searchForm = reactive({
  keyword: '',
  operationType: null,
  module: '',
  username: '',
  targetType: '',
  status: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref(null)
const integrityVisible = ref(false)
const integrityResult = reactive({ total: 0, valid: 0, errors: [], isValid: null })
const statistics = reactive({ totalCount: 0, successCount: 0, failCount: 0 })

const opTypeChartRef = ref(null)
const dateChartRef = ref(null)
const moduleChartRef = ref(null)
const userChartRef = ref(null)

let opTypeChart = null
let dateChart = null
let moduleChart = null
let userChart = null

const operationTypeOptions = [
  { value: 1, label: '新增' },
  { value: 2, label: '修改' },
  { value: 3, label: '删除' },
  { value: 4, label: '查询' },
  { value: 5, label: '登录' },
  { value: 6, label: '登出' },
  { value: 7, label: '导出' },
  { value: 8, label: '导入' },
  { value: 9, label: '其他' }
]

const operationTypeNames = { 1: '新增', 2: '修改', 3: '删除', 4: '查询', 5: '登录', 6: '登出', 7: '导出', 8: '导入', 9: '其他' }
const operationTypeTags = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info', 5: 'primary', 6: 'info', 7: '', 8: 'success', 9: 'info' }

const getOperationTypeName = (type) => operationTypeNames[type] || '未知'
const getOperationTypeTag = (type) => operationTypeTags[type] || 'info'

const formatJson = (str) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const fetchData = async () => {
  const params = {
    current: pagination.page,
    size: pagination.size,
    keyword: searchForm.keyword || undefined,
    operationType: searchForm.operationType,
    module: searchForm.module || undefined,
    username: searchForm.username || undefined,
    targetType: searchForm.targetType || undefined,
    status: searchForm.status,
    startDate: searchForm.startDate ? searchForm.startDate.toISOString().slice(0, 10) : undefined,
    endDate: searchForm.endDate ? searchForm.endDate.toISOString().slice(0, 10) : undefined
  }
  const res = await getOperationLogPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const fetchStatistics = async () => {
  try {
    const params = {}
    if (searchForm.startDate) params.startDate = searchForm.startDate.toISOString().slice(0, 10)
    if (searchForm.endDate) params.endDate = searchForm.endDate.toISOString().slice(0, 10)
    const res = await getOperationLogStatistics(params)
    const data = res.data || {}
    statistics.totalCount = data.totalCount || 0
    statistics.successCount = data.successCount || 0
    statistics.failCount = data.failCount || 0
    await nextTick()
    renderCharts(data)
  } catch (e) {
    console.error('Load statistics failed:', e)
  }
}

const renderCharts = (data) => {
  const opTypeData = data.operationTypeStats || []
  if (opTypeChart && opTypeData.length > 0) {
    opTypeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: opTypeData.map(d => ({
          name: operationTypeNames[d.operation_type] || '其他',
          value: d.cnt
        }))
      }]
    })
  }

  const dateData = data.dateStats || []
  if (dateChart && dateData.length > 0) {
    dateChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dateData.map(d => (d.date || '').slice(5))
      },
      yAxis: { type: 'value' },
      series: [{
        name: '操作数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        lineStyle: { width: 3 },
        itemStyle: { color: '#409EFF' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.5)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ]) },
        data: dateData.map(d => d.cnt)
      }]
    })
  }

  const moduleData = data.moduleStats || []
  if (moduleChart && moduleData.length > 0) {
    moduleChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: moduleData.map(d => d.module).reverse() },
      series: [{
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#67C23A' },
            { offset: 1, color: '#95D475' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        data: moduleData.map(d => d.cnt).reverse()
      }]
    })
  }

  const userData = data.userStats || []
  if (userChart && userData.length > 0) {
    userChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: userData.map(d => d.username).reverse() },
      series: [{
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#E6A23C' },
            { offset: 1, color: '#F0C78E' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        data: userData.map(d => d.cnt).reverse()
      }]
    })
  }
}

const initCharts = () => {
  if (opTypeChartRef.value) opTypeChart = echarts.init(opTypeChartRef.value)
  if (dateChartRef.value) dateChart = echarts.init(dateChartRef.value)
  if (moduleChartRef.value) moduleChart = echarts.init(moduleChartRef.value)
  if (userChartRef.value) userChart = echarts.init(userChartRef.value)
}

const handleResize = () => {
  opTypeChart && opTypeChart.resize()
  dateChart && dateChart.resize()
  moduleChart && moduleChart.resize()
  userChart && userChart.resize()
}

const handleSearch = () => {
  pagination.page = 1
  Promise.all([fetchData(), fetchStatistics()])
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '', operationType: null, module: '', username: '',
    targetType: '', status: null, startDate: null, endDate: null
  })
  pagination.page = 1
  Promise.all([fetchData(), fetchStatistics()])
}

const handleDetail = async (row) => {
  const res = await getOperationLogDetail(row.id)
  detailData.value = res.data
  detailVisible.value = true
}

const handleExport = async () => {
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      operationType: searchForm.operationType,
      module: searchForm.module || undefined,
      username: searchForm.username || undefined,
      targetType: searchForm.targetType || undefined,
      status: searchForm.status,
      startDate: searchForm.startDate ? searchForm.startDate.toISOString().slice(0, 10) : undefined,
      endDate: searchForm.endDate ? searchForm.endDate.toISOString().slice(0, 10) : undefined
    }
    const response = await exportOperationLog(params)
    const blobData = response.data || response
    const blob = new Blob([blobData], { type: 'text/csv;charset=utf-8;' })
    
    let fileName = `operation_log_${new Date().toISOString().slice(0, 10).replace(/-/g, '')}.csv`
    const disposition = response.headers?.['content-disposition']
    if (disposition) {
      const match = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
      if (match) {
        fileName = decodeURIComponent(match[1] || match[2])
      }
    }
    
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

const handleVerifyIntegrity = async () => {
  try {
    const res = await verifyOperationLogIntegrity({})
    const data = res.data || {}
    integrityResult.total = data.total || 0
    integrityResult.valid = data.valid || 0
    integrityResult.errors = data.errors || []
    integrityResult.isValid = data.isValid
    integrityVisible.value = true
  } catch (e) {
    ElMessage.error('校验失败')
  }
}

onMounted(async () => {
  initCharts()
  window.addEventListener('resize', handleResize)
  await Promise.all([fetchData(), fetchStatistics()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  opTypeChart && opTypeChart.dispose()
  dateChart && dateChart.dispose()
  moduleChart && moduleChart.dispose()
  userChart && userChart.dispose()
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
  cursor: pointer;
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
}

.stat-total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.stat-fail .stat-icon {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  color: #fff;
}

.stat-integrity .stat-icon {
  background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
  color: #fff;
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

.verify-link {
  font-size: 14px;
  color: #909399;
  text-decoration: underline;
}

.stat-label {
  font-size: 13px;
  color: var(--text-regular);
  margin-top: 4px;
}

.charts-row {
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.chart-header {
  margin-bottom: 12px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  height: 260px;
  width: 100%;
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.target-id {
  color: #909399;
  font-size: 12px;
}

.mono-text {
  font-family: 'Consolas', 'Monaco', monospace;
  word-break: break-all;
}

.small-text {
  font-size: 12px;
  color: #606266;
}

.checksum-text {
  color: #67C23A;
}

.error-text {
  color: #F56C6C;
}

.json-pre {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 240px;
  overflow: auto;
  border-left: 3px solid #dcdfe6;
}

.before-json {
  border-left-color: #F56C6C;
  background: #fef0f0;
}

.after-json {
  border-left-color: #67C23A;
  background: #f0f9eb;
}

.error-list-wrap {
  margin-top: 16px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fbc4c4;
}

.error-list-title {
  margin: 0 0 12px 0;
  color: #F56C6C;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(50% - 4px);
  }
}
</style>
