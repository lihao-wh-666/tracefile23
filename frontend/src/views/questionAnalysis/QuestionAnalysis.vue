<template>
  <div class="page-container">
    <h2 class="page-title">题目数据分析</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="选择科目" class="search-item">
          <el-select v-model="filterSubjectId" placeholder="请选择科目" clearable style="min-width: 180px" @change="handleSubjectChange">
            <el-option v-for="s in subjectList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择试卷" class="search-item">
          <el-select v-model="filterPaperId" placeholder="请选择试卷" clearable style="min-width: 220px">
            <el-option v-for="p in paperList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="错误率阈值" class="search-item">
          <el-input-number v-model="wrongRateThreshold" :min="1" :max="100" :step="5" suffix="%" style="width: 120px" />
        </el-form-item>
        <el-form-item label="高频错题数" class="search-item">
          <el-input-number v-model="topN" :min="3" :max="50" :step="1" style="width: 100px" />
        </el-form-item>
        <el-form-item class="search-item">
          <el-button type="primary" @click="generateReport" :loading="loading">
            <el-icon><DataAnalysis /></el-icon>
            生成分析报告
          </el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-alert v-if="!reportData" type="info" :closable="false" show-icon class="info-alert">
      <template #title>请选择科目或试卷，点击"生成分析报告"按钮查看数据分析结果</template>
      <template #default>系统将自动计算每道题目的正确率、错误率、得分均值，并识别高频错题</template>
    </el-alert>

    <div v-if="reportData" class="report-container">
      <el-card shadow="hover" class="summary-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">分析概览</span>
            <span class="generate-time">报告生成时间：{{ reportData.generateTime }}</span>
          </div>
        </template>

        <div class="report-header">
          <el-tag v-if="reportData.subjectName" type="success" size="large">{{ reportData.subjectName }}</el-tag>
          <el-tag v-if="reportData.paperName" type="primary" size="large">{{ reportData.paperName }}</el-tag>
        </div>

        <div class="stat-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">题目总数</div>
              <div class="stat-value">{{ reportData.totalQuestionCount }}</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon :size="28"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总答题次数</div>
              <div class="stat-value">{{ reportData.totalAnswerCount }}</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">参与学生数</div>
              <div class="stat-value">{{ reportData.totalUserCount }}</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">整体正确率</div>
              <div class="stat-value" :class="{ 'text-success': overallCorrectRate >= 70, 'text-warning': overallCorrectRate >= 40 && overallCorrectRate < 70, 'text-danger': overallCorrectRate < 40 }">
                {{ overallCorrectRate.toFixed(1) }}%
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
              <el-icon :size="28"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平均得分</div>
              <div class="stat-value">{{ reportData.overallAverageScore }}</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a8edea, #fed6e3)">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">高频错题数</div>
              <div class="stat-value text-danger">{{ reportData.highFrequencyWrongQuestions.length }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <div class="charts-row">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">题目正确率/错误率分布</span>
          </template>
          <div ref="correctRateChartRef" class="chart-container"></div>
        </el-card>

        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">各题型平均正确率</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </div>

      <div class="charts-row">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">各难度级别题目分布</span>
          </template>
          <div ref="difficultyPieChartRef" class="chart-container"></div>
        </el-card>

        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span class="card-title">各难度正确率对比</span>
          </template>
          <div ref="difficultyBarChartRef" class="chart-container"></div>
        </el-card>
      </div>

      <el-card shadow="hover" class="table-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">逐题分析详情</span>
            <div class="table-toolbar">
              <el-input v-model="searchKeyword" placeholder="搜索题目内容..." style="width: 250px" clearable>
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
          </div>
        </template>

        <div class="responsive-table">
          <el-table :data="filteredQuestionList" border stripe style="width: 100%" max-height="500">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="questionId" label="题目ID" width="80" align="center" />
            <el-table-column prop="typeName" label="题型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="difficultyName" label="难度" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="getDifficultyTagType(row.difficulty)" size="small">{{ row.difficultyName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="score" label="分值" width="70" align="center" />
            <el-table-column prop="totalAnswerCount" label="答题次数" width="90" align="center" />
            <el-table-column prop="correctCount" label="正确次数" width="90" align="center" />
            <el-table-column prop="wrongCount" label="错误次数" width="90" align="center" />
            <el-table-column label="正确率" width="100" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="(row.correctRate * 100).toFixed(0)"
                  :color="getCorrectRateColor(row.correctRate)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column label="错误率" width="100" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="(row.wrongRate * 100).toFixed(0)"
                  :color="getWrongRateColor(row.wrongRate)"
                  :stroke-width="8"
                />
              </template>
            </el-table-column>
            <el-table-column prop="averageScore" label="平均得分" width="100" align="center">
              <template #default="{ row }">
                <span :class="{ 'text-success': row.averageScore >= row.score * 0.6, 'text-danger': row.averageScore < row.score * 0.6 }">
                  {{ row.averageScore }}/{{ row.score }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <el-card shadow="hover" class="table-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon style="color: #f56c6c; margin-right: 4px"><WarningFilled /></el-icon>
              高频错题分析（错误率 ≥ {{ wrongRateThreshold }}% 或排名前 {{ topN }}）
            </span>
            <el-tag type="danger" size="small">共 {{ reportData.highFrequencyWrongQuestions.length }} 道</el-tag>
          </div>
        </template>

        <div v-if="reportData.highFrequencyWrongQuestions.length === 0" class="empty-state">
          <el-empty description="暂无高频错题数据" />
        </div>

        <div v-else class="responsive-table">
          <el-table :data="reportData.highFrequencyWrongQuestions" border stripe style="width: 100%">
            <el-table-column prop="wrongRank" label="排名" width="70" align="center">
              <template #default="{ row }">
                <el-badge :value="row.wrongRank" :class="getRankBadgeClass(row.wrongRank)" />
              </template>
            </el-table-column>
            <el-table-column prop="typeName" label="题型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.type)" size="small">{{ row.typeName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="difficultyName" label="难度" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="getDifficultyTagType(row.difficulty)" size="small">{{ row.difficultyName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="score" label="分值" width="70" align="center" />
            <el-table-column prop="totalAnswerCount" label="答题次数" width="90" align="center" />
            <el-table-column prop="wrongCount" label="错误次数" width="90" align="center" />
            <el-table-column label="错误率" width="110" align="center">
              <template #default="{ row }">
                <span class="text-danger font-bold">{{ (row.wrongRate * 100).toFixed(1) }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="wrongReason" label="错误原因分析" min-width="250" />
          </el-table>
        </div>
      </el-card>

      <el-card shadow="hover" class="suggestion-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon style="color: #409eff; margin-right: 4px"><Star /></el-icon>
              优化建议
            </span>
          </div>
        </template>

        <el-timeline>
          <el-timeline-item
            v-for="(suggestion, index) in reportData.optimizationSuggestions"
            :key="index"
            :timestamp="''"
            placement="top"
          >
            <el-card shadow="hover" class="suggestion-item">
              <div class="suggestion-content">
                <el-icon class="suggestion-icon" :color="getSuggestionIconColor(index)"><EditPen /></el-icon>
                <span>{{ suggestion }}</span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  DataAnalysis, Document, EditPen, User, CircleCheck, Star, Warning,
  Search, WarningFilled
} from '@element-plus/icons-vue'
import { generateAnalysisReport } from '../../api/questionAnalysis'
import { getSubjectList } from '../../api/subject'
import { getPaperList } from '../../api/paper'

const filterSubjectId = ref(null)
const filterPaperId = ref(null)
const wrongRateThreshold = ref(60)
const topN = ref(10)
const searchKeyword = ref('')
const loading = ref(false)
const reportData = ref(null)
const subjectList = ref([])
const paperList = ref([])

const correctRateChartRef = ref(null)
const typeChartRef = ref(null)
const difficultyPieChartRef = ref(null)
const difficultyBarChartRef = ref(null)

let correctRateChart = null
let typeChart = null
let difficultyPieChart = null
let difficultyBarChart = null

const overallCorrectRate = computed(() => {
  if (!reportData.value) return 0
  return reportData.value.overallCorrectRate * 100
})

const filteredQuestionList = computed(() => {
  if (!reportData.value || !searchKeyword.value) {
    return reportData.value?.questionAnalysisList || []
  }
  const keyword = searchKeyword.value.toLowerCase()
  return reportData.value.questionAnalysisList.filter(q =>
    q.content.toLowerCase().includes(keyword)
  )
})

const getTypeTagType = (type) => {
  const map = { 1: '', 2: 'warning', 3: 'success', 4: 'info', 5: 'danger' }
  return map[type] || 'info'
}

const getDifficultyTagType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

const getCorrectRateColor = (rate) => {
  if (rate >= 0.8) return '#67c23a'
  if (rate >= 0.6) return '#409eff'
  if (rate >= 0.4) return '#e6a23c'
  return '#f56c6c'
}

const getWrongRateColor = (rate) => {
  if (rate >= 0.6) return '#f56c6c'
  if (rate >= 0.4) return '#e6a23c'
  if (rate >= 0.2) return '#409eff'
  return '#67c23a'
}

const getRankBadgeClass = (rank) => {
  if (rank <= 3) return 'top-three'
  return ''
}

const getSuggestionIconColor = (index) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  return colors[index % colors.length]
}

const loadSubjects = async () => {
  const res = await getSubjectList()
  subjectList.value = res.data || []
}

const loadPapers = async (subjectId = null) => {
  const params = { current: 1, size: 200 }
  if (subjectId) params.subjectId = subjectId
  const res = await getPaperList(params)
  paperList.value = res.data?.records || []
}

const handleSubjectChange = () => {
  filterPaperId.value = null
  loadPapers(filterSubjectId.value)
}

const resetFilter = () => {
  filterSubjectId.value = null
  filterPaperId.value = null
  wrongRateThreshold.value = 60
  topN.value = 10
  searchKeyword.value = ''
  reportData.value = null
  loadPapers()
}

const generateReport = async () => {
  if (!filterSubjectId.value && !filterPaperId.value) {
    ElMessage.warning('请至少选择科目或试卷')
    return
  }

  loading.value = true
  try {
    const params = {
      subjectId: filterSubjectId.value,
      paperId: filterPaperId.value,
      wrongRateThreshold: wrongRateThreshold.value / 100,
      topN: topN.value
    }
    const res = await generateAnalysisReport(params)
    reportData.value = res.data
    await nextTick()
    renderAllCharts()
    ElMessage.success('分析报告生成成功')
  } catch (err) {
    ElMessage.error('生成分析报告失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

const renderAllCharts = () => {
  if (!reportData.value) return
  renderCorrectRateChart()
  renderTypeChart()
  renderDifficultyPieChart()
  renderDifficultyBarChart()
}

const renderCorrectRateChart = () => {
  if (!correctRateChartRef.value) return
  if (!correctRateChart) {
    correctRateChart = echarts.init(correctRateChartRef.value)
  }

  const questions = reportData.value.questionAnalysisList.slice(0, 20)
  const xData = questions.map((q, i) => `第${i + 1}题`)
  const correctRateData = questions.map(q => (q.correctRate * 100).toFixed(1))
  const wrongRateData = questions.map(q => (q.wrongRate * 100).toFixed(1))

  correctRateChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['正确率', '错误率'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [
      {
        name: '正确率',
        type: 'bar',
        stack: 'total',
        data: correctRateData,
        itemStyle: { color: '#67c23a' },
        label: { show: false }
      },
      {
        name: '错误率',
        type: 'bar',
        stack: 'total',
        data: wrongRateData,
        itemStyle: { color: '#f56c6c' },
        label: { show: false }
      }
    ]
  })
}

const renderTypeChart = () => {
  if (!typeChartRef.value) return
  if (!typeChart) {
    typeChart = echarts.init(typeChartRef.value)
  }

  const analysisByType = reportData.value.analysisByType || {}
  const xData = Object.keys(analysisByType)
  const yData = xData.map(type => {
    const questions = analysisByType[type]
    const avgRate = questions.reduce((sum, q) => sum + q.correctRate, 0) / questions.length
    return (avgRate * 100).toFixed(1)
  })

  typeChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [{
      type: 'bar',
      data: yData,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      },
      barWidth: '50%'
    }]
  })
}

const renderDifficultyPieChart = () => {
  if (!difficultyPieChartRef.value) return
  if (!difficultyPieChart) {
    difficultyPieChart = echarts.init(difficultyPieChartRef.value)
  }

  const analysisByDifficulty = reportData.value.analysisByDifficulty || {}
  const data = Object.keys(analysisByDifficulty).map(key => ({
    name: key,
    value: analysisByDifficulty[key].length
  }))

  const colorMap = { '简单': '#67c23a', '中等': '#e6a23c', '困难': '#f56c6c' }

  difficultyPieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}题 ({d}%)'
    },
    legend: {
      bottom: 0
    },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}题\n{d}%'
      },
      data: data.map(item => ({
        ...item,
        itemStyle: { color: colorMap[item.name] || '#409eff' }
      }))
    }]
  })
}

const renderDifficultyBarChart = () => {
  if (!difficultyBarChartRef.value) return
  if (!difficultyBarChart) {
    difficultyBarChart = echarts.init(difficultyBarChartRef.value)
  }

  const analysisByDifficulty = reportData.value.analysisByDifficulty || {}
  const xData = Object.keys(analysisByDifficulty)
  const yData = xData.map(diff => {
    const questions = analysisByDifficulty[diff]
    const avgRate = questions.reduce((sum, q) => sum + q.correctRate, 0) / questions.length
    return (avgRate * 100).toFixed(1)
  })

  const colorMap = { '简单': '#67c23a', '中等': '#e6a23c', '困难': '#f56c6c' }

  difficultyBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [{
      type: 'bar',
      data: yData.map((value, index) => ({
        value,
        itemStyle: {
          color: colorMap[xData[index]] || '#409eff',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      },
      barWidth: '50%'
    }]
  })
}

const handleResize = () => {
  correctRateChart?.resize()
  typeChart?.resize()
  difficultyPieChart?.resize()
  difficultyBarChart?.resize()
}

onMounted(() => {
  loadSubjects()
  loadPapers()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (correctRateChart) correctRateChart.dispose()
  if (typeChart) typeChart.dispose()
  if (difficultyPieChart) difficultyPieChart.dispose()
  if (difficultyBarChart) difficultyBarChart.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
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

.info-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.generate-time {
  font-size: 12px;
  color: #909399;
}

.report-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.summary-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}

.text-success {
  color: #67c23a !important;
}

.text-warning {
  color: #e6a23c !important;
}

.text-danger {
  color: #f56c6c !important;
}

.font-bold {
  font-weight: bold;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  border-radius: 8px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.table-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  gap: 8px;
}

.responsive-table {
  overflow-x: auto;
}

.empty-state {
  padding: 40px 0;
}

.suggestion-card {
  border-radius: 8px;
}

.suggestion-item {
  margin: 0;
  border: none;
  background: #f5f7fa;
}

.suggestion-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  line-height: 1.6;
}

.suggestion-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.top-three .el-badge__content {
  background: linear-gradient(135deg, #ff6b6b, #feca57) !important;
  font-weight: bold;
}

@media screen and (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-card {
    padding: 12px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 18px;
  }

  .chart-container {
    height: 240px;
  }

  .search-form {
    flex-wrap: wrap;
  }

  .search-item {
    flex: 1 1 100%;
  }

  .table-toolbar {
    width: 100%;
    margin-top: 8px;
  }

  .table-toolbar .el-input {
    width: 100% !important;
  }
}

@media screen and (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
