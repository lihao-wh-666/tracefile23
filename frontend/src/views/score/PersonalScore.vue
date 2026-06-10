<template>
  <div class="page-container">
    <h2 class="page-title">个人成绩台账</h2>

    <el-card shadow="hover" class="stat-card">
      <div class="stat-title">成绩概览</div>
      <div class="stat-grid">
        <div class="stat-item stat-item-primary">
          <div class="stat-icon">
            <el-icon :size="28"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ personalStat.maxScore || 0 }}</div>
            <div class="stat-label">最高分</div>
            <div class="stat-sub">{{ personalStat.maxScoreExamName || '-' }}</div>
          </div>
        </div>
        <div class="stat-item stat-item-success">
          <div class="stat-icon">
            <el-icon :size="28"><Histogram /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ Number(personalStat.avgScore || 0).toFixed(1) }}</div>
            <div class="stat-label">平均分</div>
            <div class="stat-sub">共{{ personalStat.totalExamCount || 0 }}次考试</div>
          </div>
        </div>
        <div class="stat-item stat-item-warning">
          <div class="stat-icon">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ Number(personalStat.accuracyRate || 0).toFixed(1) }}%</div>
            <div class="stat-label">正确率</div>
            <div class="stat-sub">{{ personalStat.correctQuestionCount || 0 }}/{{ personalStat.totalQuestionCount || 0 }}题</div>
          </div>
        </div>
        <div class="stat-item stat-item-danger">
          <div class="stat-icon">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ personalStat.wrongQuestionCount || 0 }}</div>
            <div class="stat-label">错题数</div>
            <div class="stat-sub">累计错题</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="hover" class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="考试记录" name="records">
          <div class="toolbar">
            <el-dropdown @command="handleExportRecords">
              <el-button type="primary" :icon="Download">
                导出成绩
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
                  <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="responsive-table mobile-hidden">
            <el-table :data="recordList" border stripe style="width: 100%">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="examName" label="考试名称" min-width="180" />
              <el-table-column prop="score" label="得分" width="90" align="center">
                <template #default="{ row }">
                  <span :class="scoreClass(row.score, row.totalScore)">
                    {{ row.score }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="总分" width="80" align="center" />
              <el-table-column label="排名" width="80" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.rank" :type="rankType(row.rank)" size="small">
                    第{{ row.rank }}名
                  </el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="recordStatusType(row.status)" size="small">
                    {{ recordStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="提交时间" min-width="160" />
              <el-table-column label="用时" width="90" align="center">
                <template #default="{ row }">
                  {{ row.duration ? formatDuration(row.duration) : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mobile-list mobile-only">
            <div class="record-item" v-for="(item, index) in recordList" :key="item.id">
              <div class="record-header">
                <span class="record-index">{{ (recordPagination.current - 1) * recordPagination.size + index + 1 }}</span>
                <span class="record-name">{{ item.examName }}</span>
                <el-tag v-if="item.rank" :type="rankType(item.rank)" size="small">
                  第{{ item.rank }}名
                </el-tag>
              </div>
              <div class="record-score-row">
                <div class="score-block">
                  <span class="score-num" :class="scoreClass(item.score, item.totalScore)">
                    {{ item.score }}
                  </span>
                  <span class="score-total">/{{ item.totalScore }}</span>
                </div>
                <div class="score-meta">
                  <div class="meta-item">
                    <el-tag :type="recordStatusType(item.status)" size="small">
                      {{ recordStatusText(item.status) }}
                    </el-tag>
                  </div>
                  <div class="meta-item">
                    用时: {{ item.duration ? formatDuration(item.duration) : '-' }}
                  </div>
                </div>
              </div>
              <div class="record-time">{{ item.submitTime }}</div>
            </div>
          </div>

          <el-pagination
            v-model:current-page="recordPagination.current"
            v-model:page-size="recordPagination.size"
            :total="recordPagination.total"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            class="pagination-wrap"
            @current-change="loadRecords"
            @size-change="loadRecords"
          />
        </el-tab-pane>

        <el-tab-pane label="错题明细" name="wrong">
          <div class="toolbar">
            <el-dropdown @command="handleExportWrong">
              <el-button type="primary" :icon="Download">
                导出错题
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
                  <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="responsive-table mobile-hidden">
            <el-table :data="wrongList" border stripe style="width: 100%">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="examName" label="来源考试" min-width="140" />
              <el-table-column prop="type" label="题型" width="80" align="center">
                <template #default="{ row }">
                  {{ questionTypeText(row.type) }}
                </template>
              </el-table-column>
              <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
              <el-table-column prop="userAnswer" label="你的答案" width="100" align="center">
                <template #default="{ row }">
                  <span class="wrong-answer">{{ row.userAnswer || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="correctAnswer" label="正确答案" width="100" align="center">
                <template #default="{ row }">
                  <span class="correct-answer">{{ row.correctAnswer || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="做错时间" min-width="160" />
            </el-table>
          </div>

          <div class="mobile-list mobile-only">
            <div class="wrong-item" v-for="(item, index) in wrongList" :key="item.questionId + '-' + item.recordId">
              <div class="wrong-header">
                <span class="wrong-index">{{ (wrongPagination.current - 1) * wrongPagination.size + index + 1 }}</span>
                <el-tag size="small" type="info">{{ questionTypeText(item.type) }}</el-tag>
                <span class="wrong-exam">{{ item.examName }}</span>
              </div>
              <div class="wrong-content">{{ item.content }}</div>
              <div class="wrong-answers">
                <div class="answer-row">
                  <span class="answer-label">你的答案：</span>
                  <span class="wrong-answer">{{ item.userAnswer || '-' }}</span>
                </div>
                <div class="answer-row">
                  <span class="answer-label">正确答案：</span>
                  <span class="correct-answer">{{ item.correctAnswer || '-' }}</span>
                </div>
              </div>
              <div class="wrong-time">{{ item.submitTime }}</div>
            </div>
          </div>

          <el-pagination
            v-model:current-page="wrongPagination.current"
            v-model:page-size="wrongPagination.size"
            :total="wrongPagination.total"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            class="pagination-wrap"
            @current-change="loadWrongQuestions"
            @size-change="loadWrongQuestions"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Trophy, Histogram, CircleCheck, Warning, Download, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyStat, getMyRecordList, getMyWrongQuestions, exportMyRecordsExcel, exportMyRecordsCsv, exportMyWrongQuestionsExcel, exportMyWrongQuestionsCsv } from '../../api/record'

const activeTab = ref('records')
const personalStat = ref({})
const recordList = ref([])
const wrongList = ref([])

const recordPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const wrongPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const recordStatusText = (status) => {
  const map = { 0: '考试中', 1: '已提交', 2: '已阅卷' }
  return map[status] ?? '未知'
}

const recordStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
}

const questionTypeText = (type) => {
  const map = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '问答' }
  return map[type] ?? '未知'
}

const rankType = (rank) => {
  if (rank === 1) return 'danger'
  if (rank <= 3) return 'warning'
  if (rank <= 10) return 'success'
  return 'info'
}

const scoreClass = (score, totalScore) => {
  if (totalScore && score !== undefined) {
    const rate = score / totalScore
    if (rate >= 0.9) return 'score-excellent'
    if (rate >= 0.7) return 'score-good'
    if (rate >= 0.6) return 'score-pass'
    return 'score-fail'
  }
  return ''
}

const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const min = Math.floor(seconds / 60)
  const sec = seconds % 60
  if (min > 0) {
    return `${min}分${sec}秒`
  }
  return `${sec}秒`
}

const loadStat = async () => {
  try {
    const res = await getMyStat()
    personalStat.value = res.data || {}
  } catch (e) {
    console.error('加载成绩统计失败', e)
  }
}

const loadRecords = async () => {
  try {
    const res = await getMyRecordList({
      current: recordPagination.current,
      size: recordPagination.size
    })
    recordList.value = res.data.records || []
    recordPagination.total = res.data.total || 0
  } catch (e) {
    console.error('加载考试记录失败', e)
  }
}

const loadWrongQuestions = async () => {
  try {
    const res = await getMyWrongQuestions({
      current: wrongPagination.current,
      size: wrongPagination.size
    })
    wrongList.value = res.data.records || []
    wrongPagination.total = res.data.total || 0
  } catch (e) {
    console.error('加载错题失败', e)
  }
}

const handleTabChange = (tab) => {
  if (tab === 'records') {
    if (recordList.value.length === 0) {
      loadRecords()
    }
  } else if (tab === 'wrong') {
    if (wrongList.value.length === 0) {
      loadWrongQuestions()
    }
  }
}

const downloadFile = (blob, filename) => {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
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

const handleExportRecords = async (type) => {
  try {
    let response
    let filename
    if (type === 'excel') {
      response = await exportMyRecordsExcel()
      filename = getFilenameFromHeader(response) || '我的考试成绩.xlsx'
    } else {
      response = await exportMyRecordsCsv()
      filename = getFilenameFromHeader(response) || '我的考试成绩.csv'
    }
    downloadFile(response.data, filename)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败，请重试')
  }
}

const handleExportWrong = async (type) => {
  try {
    let response
    let filename
    if (type === 'excel') {
      response = await exportMyWrongQuestionsExcel()
      filename = getFilenameFromHeader(response) || '我的错题明细.xlsx'
    } else {
      response = await exportMyWrongQuestionsCsv()
      filename = getFilenameFromHeader(response) || '我的错题明细.csv'
    }
    downloadFile(response.data, filename)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败，请重试')
  }
}

onMounted(() => {
  loadStat()
  loadRecords()
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.stat-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.stat-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.stat-item-primary {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
}

.stat-item-success {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.stat-item-warning {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
}

.stat-item-danger {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  color: #409eff;
}

.stat-item-primary .stat-icon {
  color: #409eff;
}

.stat-item-success .stat-icon {
  color: #67c23a;
}

.stat-item-warning .stat-icon {
  color: #e6a23c;
}

.stat-item-danger .stat-icon {
  color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.stat-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.tab-card {
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-pass {
  color: #e6a23c;
  font-weight: bold;
}

.score-fail {
  color: #f56c6c;
  font-weight: bold;
}

.wrong-answer {
  color: #f56c6c;
  font-weight: 500;
}

.correct-answer {
  color: #67c23a;
  font-weight: 500;
}

.mobile-list {
  margin-top: 8px;
}

.record-item, .wrong-item {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.record-header, .wrong-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.record-index, .wrong-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-name, .wrong-exam {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.score-block {
  display: flex;
  align-items: baseline;
}

.score-num {
  font-size: 24px;
  font-weight: bold;
}

.score-total {
  font-size: 14px;
  color: #909399;
}

.score-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.meta-item {
  font-size: 12px;
  color: #606266;
}

.record-time, .wrong-time {
  font-size: 12px;
  color: #909399;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.wrong-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.wrong-answers {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 8px;
}

.answer-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  margin-bottom: 4px;
}

.answer-row:last-child {
  margin-bottom: 0;
}

.answer-label {
  color: #606266;
  flex-shrink: 0;
}

@media screen and (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-item {
    padding: 16px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }
}
</style>
