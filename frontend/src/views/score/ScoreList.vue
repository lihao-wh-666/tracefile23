<template>
  <div class="page-container">
    <h2 class="page-title">成绩统计</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="选择考试" class="search-item">
          <el-select v-model="filterExamId" placeholder="请选择考试" clearable style="min-width: 200px; width: 100%" @change="loadData">
            <el-option v-for="e in examList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-item export-item">
          <el-dropdown @command="handleExport">
            <el-button type="primary">
              导出成绩
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
                <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
                <el-dropdown-item command="pdf">导出 PDF</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card mobile-hidden">
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="examName" label="考试名称" min-width="150" />
          <el-table-column prop="realName" label="学生姓名" width="110" />
          <el-table-column prop="score" label="分数" width="80" align="center">
            <template #default="{ row }">
              <span :class="{ 'text-danger': Number(row.score) < 60, 'text-success': Number(row.score) >= 60 }">
                {{ row.score }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="总分" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="recordStatusType(row.status)" size="small">{{ recordStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" min-width="160" />
          <el-table-column label="用时" width="90" align="center">
            <template #default="{ row }">
              {{ row.duration ? row.duration + '分钟' : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="handleDetail(row)">查看详情</el-button>
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
          <span class="item-title">{{ item.realName }}</span>
          <el-tag :type="recordStatusType(item.status)" size="small">{{ recordStatusText(item.status) }}</el-tag>
        </div>
        <div class="item-exam">{{ item.examName }}</div>
        <div class="item-score-row">
          <div class="score-item">
            <span class="score-label">得分</span>
            <span class="score-value" :class="{ 'text-danger': Number(item.score) < 60, 'text-success': Number(item.score) >= 60 }">
              {{ item.score }}
            </span>
          </div>
          <div class="score-item">
            <span class="score-label">总分</span>
            <span class="score-value">{{ item.totalScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">用时</span>
            <span class="score-value">{{ item.duration ? item.duration + '分' : '-' }}</span>
          </div>
        </div>
        <div class="item-submit">提交: {{ item.submitTime }}</div>
        <div class="item-actions">
          <el-button type="primary" size="small" @click="handleDetail(item)">查看详情</el-button>
        </div>
      </div>
    </div>

    <div v-if="scoreStat" class="stat-panel">
      <h4 class="panel-title">成绩统计</h4>
      <div class="stat-grid">
        <div class="stat-card">
          <div class="stat-label">参考人数</div>
          <div class="stat-value">{{ scoreStat.totalCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均分</div>
          <div class="stat-value">{{ Number(scoreStat.avgScore).toFixed(1) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最高分</div>
          <div class="stat-value text-success">{{ scoreStat.maxScore }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">最低分</div>
          <div class="stat-value text-danger">{{ scoreStat.minScore }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">及格人数</div>
          <div class="stat-value">{{ scoreStat.passCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">及格率</div>
          <div class="stat-value">{{ scoreStat.passRate }}%</div>
        </div>
      </div>

      <div class="charts-grid">
        <div class="chart-card">
          <div ref="barChartRef" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <div ref="pieChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="答题详情" class="responsive-dialog" destroy-on-close>
      <div v-if="detailData.record" style="margin-bottom: 16px">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="学生">{{ detailData.record.realName }}</el-descriptions-item>
          <el-descriptions-item label="得分">
            <span :class="{ 'text-danger': Number(detailData.record.score) < 60, 'text-success': Number(detailData.record.score) >= 60 }">
              {{ detailData.record.score }}
            </span>
            / {{ detailData.record.totalScore }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="recordStatusType(detailData.record.status)" size="small">
              {{ recordStatusText(detailData.record.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="responsive-table">
        <el-table :data="detailData.items" border size="small">
          <el-table-column type="index" label="序号" width="50" align="center" />
          <el-table-column prop="content" label="题目内容" min-width="150" show-overflow-tooltip />
          <el-table-column prop="userAnswer" label="作答" width="90" align="center" />
          <el-table-column prop="correctAnswer" label="答案" width="90" align="center" />
          <el-table-column prop="score" label="得分" width="60" align="center" />
          <el-table-column prop="totalScore" label="分值" width="60" align="center" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getRecordPage, getRecordDetail, getScoreStats, getRecordAnswers, exportExcel, exportCsv, exportPdf } from '../../api/record'
import { getExamPage } from '../../api/exam'
import { getPaperDetail } from '../../api/paper'

const filterExamId = ref(null)
const tableData = ref([])
const examList = ref([])
const scoreStat = ref(null)
const detailVisible = ref(false)
const detailData = reactive({ record: null, items: [] })
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const recordStatusText = (status) => {
  const map = { 0: '考试中', 1: '已提交', 2: '已阅卷' }
  return map[status] ?? '未知'
}

const recordStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: '' }
  return map[status] ?? 'info'
}

const loadData = async () => {
  const params = { current: pagination.current, size: pagination.size }
  if (filterExamId.value) {
    params.examId = filterExamId.value
  }
  const res = await getRecordPage(params)
  tableData.value = res.data.records
  pagination.total = res.data.total
}

const loadExams = async () => {
  const res = await getExamPage({ current: 1, size: 200 })
  examList.value = res.data.records
}

const loadStats = async () => {
  if (!filterExamId.value) {
    scoreStat.value = null
    return
  }
  try {
    const res = await getScoreStats(filterExamId.value)
    scoreStat.value = res.data[0] || null
    await nextTick()
    renderCharts()
  } catch {
    scoreStat.value = null
  }
}

const renderCharts = () => {
  if (!scoreStat.value) return
  renderBarChart()
  renderPieChart()
}

const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }
  const stat = scoreStat.value
  const ranges = ['0-59', '60-69', '70-79', '80-89', '90-100']
  barChart.setOption({
    title: { text: '分数段分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ranges },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: ranges.map(() => 0),
      itemStyle: { color: '#409eff' },
      barWidth: '40%'
    }]
  })
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const stat = scoreStat.value
  const passCount = stat.passCount || 0
  const failCount = (stat.totalCount || 0) - passCount
  pieChart.setOption({
    title: { text: '及格率统计', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: [
        { value: passCount, name: '及格', itemStyle: { color: '#67c23a' } },
        { value: failCount, name: '不及格', itemStyle: { color: '#f56c6c' } }
      ],
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

const handleDetail = async (row) => {
  try {
    const [recordRes, answersRes] = await Promise.all([
      getRecordDetail(row.id),
      getRecordAnswers(row.id)
    ])
    const record = recordRes.data
    const answerMap = {}
    ;(answersRes.data || []).forEach(a => {
      answerMap[a.questionId] = a
    })

    const paperRes = await getPaperDetail(record.paperId)
    const questions = paperRes.data.questions || []

    detailData.record = record
    detailData.items = questions.map(q => {
      const ans = answerMap[q.id] || {}
      return {
        content: q.content,
        userAnswer: ans.answer || '-',
        correctAnswer: q.answer,
        score: ans.score ?? 0,
        totalScore: q.score
      }
    })
    detailVisible.value = true
  } catch {
    ElMessage.error('加载详情失败')
  }
}

const downloadFile = (blob, fileName) => {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', fileName)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const getFilenameFromHeader = (response) => {
  const disposition = response.headers['content-disposition']
  if (disposition) {
    const matches = disposition.match(/filename="?([^"]+)"?/)
    if (matches && matches[1]) {
      return decodeURIComponent(matches[1])
    }
  }
  return null
}

const handleExport = async (type) => {
  try {
    let data
    let fileName
    const examId = filterExamId.value

    let response
    switch (type) {
      case 'excel':
        response = await exportExcel(examId)
        data = response.data
        fileName = getFilenameFromHeader(response) || `成绩统计_${Date.now()}.xlsx`
        break
      case 'csv':
        response = await exportCsv(examId)
        data = response.data
        fileName = getFilenameFromHeader(response) || `成绩统计_${Date.now()}.csv`
        break
      case 'pdf':
        response = await exportPdf(examId)
        data = response.data
        fileName = getFilenameFromHeader(response) || `成绩统计_${Date.now()}.pdf`
        break
      default:
        return
    }

    downloadFile(data, fileName)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

watch(filterExamId, () => {
  pagination.current = 1
  loadData()
  loadStats()
})

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadExams()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (barChart) barChart.dispose()
  if (pieChart) pieChart.dispose()
  window.removeEventListener('resize', handleResize)
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

.table-card {
  border-radius: 8px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-success {
  color: #67c23a;
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
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
  margin-bottom: 8px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.item-exam {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-score-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.score-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 8px;
}

.score-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.score-value {
  display: block;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.item-submit {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.item-actions {
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.stat-panel {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.panel-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.chart-container {
  height: 280px;
}

@media screen and (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 240px;
  }
}

@media screen and (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }

  .item-score-row {
    flex-wrap: wrap;
  }

  .score-item {
    min-width: calc(33.33% - 8px);
  }
}
</style>
