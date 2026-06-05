<template>
  <div v-loading="loading" class="exam-take">
    <div class="exam-header">
      <div class="header-left">
        <h3>{{ examInfo.name }}</h3>
        <span class="student-info">考生：{{ userStore.userInfo?.realName || userStore.userInfo?.username || '' }}</span>
      </div>
      <div class="header-right">
        <span class="timer" :class="{ 'timer-warning': remainingMs < 300000 }">
          <el-icon><Clock /></el-icon>
          {{ formatTime(remainingMs) }}
        </span>
      </div>
    </div>

    <div class="exam-body">
      <div class="question-sidebar" :class="{ 'sidebar-open': sidebarOpen }">
        <div class="sidebar-header">
          <span class="sidebar-title">题目列表</span>
          <span class="answered-count">已答 {{ answeredCount }}/{{ questions.length }}</span>
        </div>
        <div class="question-grid">
          <div
            v-for="(q, idx) in questions"
            :key="q.id"
            class="question-item"
            :class="{ active: currentIndex === idx, answered: isAnswered(q.id) }"
            @click="selectQuestion(idx)"
          >
            {{ idx + 1 }}
          </div>
        </div>
        <div class="sidebar-footer mobile-only">
          <el-button type="primary" block @click="toggleSidebar">收起列表</el-button>
        </div>
      </div>

      <div class="sidebar-overlay mobile-only" :class="{ active: sidebarOpen }" @click="toggleSidebar"></div>

      <div class="question-content" v-if="currentQuestion">
        <div class="content-header">
          <div class="question-title">
            <el-tag size="small" type="info">{{ typeLabel(currentQuestion.type) }}</el-tag>
            <span class="question-score">（{{ currentQuestion.score }}分）</span>
          </div>
          <span class="question-index">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <el-button class="toggle-sidebar-btn mobile-only" size="small" @click="toggleSidebar">
            <el-icon><List /></el-icon>
            题目列表
          </el-button>
        </div>

        <div class="question-text">{{ currentQuestion.content }}</div>

        <div class="question-options">
          <template v-if="currentQuestion.type === 1">
            <el-radio-group v-model="answers[currentQuestion.id]" class="options-group">
              <el-radio v-for="opt in getOptions(currentQuestion)" :key="opt.key" :value="opt.key" class="option-item">
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text">{{ opt.text }}</span>
              </el-radio>
            </el-radio-group>
          </template>

          <template v-else-if="currentQuestion.type === 2">
            <el-checkbox-group v-model="answers[currentQuestion.id]" class="options-group">
              <el-checkbox v-for="opt in getOptions(currentQuestion)" :key="opt.key" :value="opt.key" class="option-item">
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text">{{ opt.text }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </template>

          <template v-else-if="currentQuestion.type === 3">
            <el-radio-group v-model="answers[currentQuestion.id]" class="options-group judge-group">
              <el-radio value="对" class="option-item judge-item">对</el-radio>
              <el-radio value="错" class="option-item judge-item">错</el-radio>
            </el-radio-group>
          </template>

          <template v-else-if="currentQuestion.type === 4">
            <el-input
              v-model="answers[currentQuestion.id]"
              placeholder="请输入答案"
              size="large"
              class="text-input"
            />
          </template>

          <template v-else-if="currentQuestion.type === 5">
            <el-input
              v-model="answers[currentQuestion.id]"
              type="textarea"
              :rows="6"
              placeholder="请输入答案"
              class="textarea-input"
            />
          </template>
        </div>

        <div class="question-nav">
          <el-button :disabled="currentIndex === 0" @click="prevQuestion">
            <el-icon><ArrowLeft /></el-icon>
            上一题
          </el-button>
          <el-button type="danger" @click="handleSubmit" class="submit-btn">
            <el-icon><Check /></el-icon>
            交卷
          </el-button>
          <el-button :disabled="currentIndex === questions.length - 1" @click="nextQuestion">
            下一题
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, List, ArrowLeft, ArrowRight, Check } from '@element-plus/icons-vue'
import { startExam, submitExam } from '../../api/record'
import { getExamDetail } from '../../api/exam'
import { getPaperDetail } from '../../api/paper'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const examId = Number(route.params.id)
const loading = ref(true)
const questions = ref([])
const currentIndex = ref(0)
const answers = reactive({})
const remainingMs = ref(0)
const sidebarOpen = ref(false)
const examInfo = reactive({ name: '', endTime: '' })
let timerHandle = null
let submitted = false

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

const answeredCount = computed(() => {
  return questions.value.filter(q => isAnswered(q.id)).length
})

const typeLabel = (type) => {
  const map = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' }
  return map[type] || '未知'
}

const getOptions = (q) => {
  const opts = []
  if (q.optionA) opts.push({ key: 'A', text: q.optionA })
  if (q.optionB) opts.push({ key: 'B', text: q.optionB })
  if (q.optionC) opts.push({ key: 'C', text: q.optionC })
  if (q.optionD) opts.push({ key: 'D', text: q.optionD })
  return opts
}

const isAnswered = (questionId) => {
  const ans = answers[questionId]
  if (Array.isArray(ans)) return ans.length > 0
  return !!ans
}

const formatTime = (ms) => {
  const total = Math.floor(ms / 1000)
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const startTimer = (endTime) => {
  const end = new Date(endTime).getTime()
  const tick = () => {
    remainingMs.value = Math.max(0, end - Date.now())
    if (remainingMs.value <= 0 && !submitted) {
      clearInterval(timerHandle)
      doSubmit(true)
    }
  }
  tick()
  timerHandle = setInterval(tick, 1000)
}

const initAnswers = (list) => {
  list.forEach(q => {
    if (q.type === 2) {
      answers[q.id] = []
    } else {
      answers[q.id] = ''
    }
  })
}

const buildSubmitData = () => {
  const answerList = questions.value.map(q => {
    let ans = answers[q.id]
    if (Array.isArray(ans)) {
      ans = [...ans].sort().join('')
    }
    return { questionId: q.id, answer: ans || '' }
  })
  return { examId, answers: answerList }
}

const doSubmit = async (auto) => {
  if (submitted) return
  submitted = true
  if (timerHandle) clearInterval(timerHandle)
  try {
    const data = buildSubmitData()
    const res = await submitExam(data)
    const record = res.data
    await ElMessageBox.alert(
      `考试已提交！\n得分：${record.score} / ${record.totalScore}`,
      auto ? '考试时间已到' : '提交成功',
      { confirmButtonText: '查看成绩', type: 'success' }
    )
    router.push('/score')
  } catch {
    submitted = false
  }
}

const handleSubmit = async () => {
  const unanswered = questions.value.filter(q => !isAnswered(q.id)).length
  const msg = unanswered > 0
    ? `还有 ${unanswered} 题未作答，确定要交卷吗？`
    : '确定要交卷吗？'
  await ElMessageBox.confirm(msg, '交卷确认', { type: 'warning' })
  await doSubmit(false)
}

const selectQuestion = (idx) => {
  currentIndex.value = idx
  if (window.innerWidth <= 768) {
    sidebarOpen.value = false
  }
}

const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const init = async () => {
  try {
    const recordRes = await startExam(examId)
    const record = recordRes.data

    const examRes = await getExamDetail(examId)
    Object.assign(examInfo, examRes.data)

    const paperRes = await getPaperDetail(record.paperId)
    questions.value = paperRes.data.questions || []
    initAnswers(questions.value)

    startTimer(examInfo.endTime)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '加载考试失败')
    router.push('/exam')
  } finally {
    loading.value = false
  }
}

onMounted(init)

onBeforeUnmount(() => {
  if (timerHandle) clearInterval(timerHandle)
})
</script>

<style scoped>
.exam-take {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #1e3c72, #2a5298);
  border-radius: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #fff;
  font-weight: 600;
}

.student-info {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.timer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 22px;
  font-weight: bold;
  color: #fff;
  font-family: 'Courier New', monospace;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 8px;
}

.timer-warning {
  color: #ffd700;
  animation: pulse 1s infinite;
  background: rgba(255, 215, 0, 0.2);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.exam-body {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

.question-sidebar {
  width: 200px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.sidebar-header {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.sidebar-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  display: block;
  margin-bottom: 4px;
}

.answered-count {
  font-size: 12px;
  color: #909399;
}

.question-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  flex: 1;
  align-content: start;
}

.question-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  transition: all 0.2s;
  background: #fff;
}

.question-item:hover {
  border-color: #409eff;
  color: #409eff;
}

.question-item.active {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.question-item.answered {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #67c23a;
}

.question-item.active.answered {
  background: #67c23a;
  border-color: #67c23a;
  color: #fff;
}

.sidebar-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

.sidebar-overlay.active {
  display: block;
}

.question-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.content-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-score {
  font-size: 14px;
  color: #e6a23c;
  font-weight: bold;
}

.question-index {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}

.toggle-sidebar-btn {
  margin-left: 0;
}

.question-text {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 24px;
  color: #303133;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.question-options {
  margin-bottom: 32px;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.option-key {
  font-weight: bold;
  color: #409eff;
  margin-right: 8px;
  min-width: 20px;
}

.option-text {
  flex: 1;
  line-height: 1.6;
}

.judge-group {
  flex-direction: row;
  gap: 16px;
}

.judge-item {
  flex: 1;
  justify-content: center;
  padding: 16px;
  font-size: 16px;
  font-weight: 500;
}

.text-input {
  max-width: 400px;
}

.textarea-input {
  max-width: 600px;
}

.question-nav {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
  justify-content: space-between;
  flex-wrap: wrap;
}

.submit-btn {
  order: 2;
}

@media screen and (max-width: 1024px) {
  .question-sidebar {
    width: 180px;
  }

  .question-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .exam-take {
    min-height: calc(100vh - 136px);
  }

  .exam-header {
    padding: 12px 16px;
    margin-bottom: 12px;
  }

  .header-left h3 {
    font-size: 16px;
  }

  .timer {
    font-size: 18px;
    padding: 6px 12px;
  }

  .exam-body {
    position: relative;
  }

  .question-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: 280px;
    z-index: 100;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    border-radius: 0;
    padding: 20px;
  }

  .question-sidebar.sidebar-open {
    transform: translateX(0);
  }

  .question-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
  }

  .question-content {
    width: 100%;
    padding: 16px;
  }

  .content-header {
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 12px;
  }

  .question-index {
    order: 3;
    width: 100%;
    text-align: right;
    margin-left: 0;
  }

  .question-text {
    font-size: 15px;
    padding: 12px;
    margin-bottom: 20px;
  }

  .judge-group {
    flex-direction: column;
  }

  .text-input,
  .textarea-input {
    max-width: 100%;
  }

  .question-nav {
    gap: 8px;
  }

  .question-nav .el-button {
    flex: 1;
    min-width: calc(33.33% - 6px);
  }

  .submit-btn {
    order: 0;
  }
}

@media screen and (max-width: 480px) {
  .exam-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    align-self: flex-end;
  }

  .question-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .question-nav .el-button {
    min-width: calc(50% - 4px);
  }

  .submit-btn {
    order: 2;
    width: 100%;
  }
}
</style>
