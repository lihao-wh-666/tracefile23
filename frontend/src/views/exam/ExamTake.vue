<template>
  <div v-loading="loading" class="exam-take">
    <div v-if="isPaused" class="pause-overlay">
      <div class="pause-content">
        <el-icon class="pause-icon"><VideoPause /></el-icon>
        <h3>考试已暂停</h3>
        <p>点击下方按钮继续答题</p>
        <el-button type="primary" size="large" @click="handleResume">
          <el-icon><VideoPlay /></el-icon>
          继续答题
        </el-button>
      </div>
    </div>

    <div class="exam-header">
      <div class="header-left">
        <h3>{{ examInfo.name }}</h3>
        <span class="student-info">考生：{{ userStore.userInfo?.realName || userStore.userInfo?.username || '' }}</span>
      </div>
      <div class="header-right">
        <span class="save-status" :class="saveStatusClass">
          <el-icon v-if="saveStatus === 'saving'"><Loading /></el-icon>
          <el-icon v-else-if="saveStatus === 'saved'"><CircleCheckFilled /></el-icon>
          <el-icon v-else><WarningFilled /></el-icon>
          {{ saveStatusText }}
        </span>
        <span class="timer" :class="{ 'timer-warning': remainingMs < 300000 }">
          <el-icon><Clock /></el-icon>
          {{ formatTime(remainingMs) }}
        </span>
        <el-button v-if="!isPaused" type="warning" @click="handlePause">
          <el-icon><VideoPause /></el-icon>
          暂停
        </el-button>
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
            <el-radio-group v-model="answers[currentQuestion.id]" class="options-group" @change="handleAnswerChange(currentQuestion.id)">
              <el-radio v-for="opt in getOptions(currentQuestion)" :key="opt.key" :value="opt.key" class="option-item">
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text">{{ opt.text }}</span>
              </el-radio>
            </el-radio-group>
          </template>

          <template v-else-if="currentQuestion.type === 2">
            <el-checkbox-group v-model="answers[currentQuestion.id]" class="options-group" @change="handleAnswerChange(currentQuestion.id)">
              <el-checkbox v-for="opt in getOptions(currentQuestion)" :key="opt.key" :value="opt.key" class="option-item">
                <span class="option-key">{{ opt.key }}</span>
                <span class="option-text">{{ opt.text }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </template>

          <template v-else-if="currentQuestion.type === 3">
            <el-radio-group v-model="answers[currentQuestion.id]" class="options-group judge-group" @change="handleAnswerChange(currentQuestion.id)">
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
              @blur="handleAnswerChange(currentQuestion.id)"
            />
          </template>

          <template v-else-if="currentQuestion.type === 5">
            <el-input
              v-model="answers[currentQuestion.id]"
              type="textarea"
              :rows="6"
              placeholder="请输入答案"
              class="textarea-input"
              @blur="handleAnswerChange(currentQuestion.id)"
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock, List, ArrowLeft, ArrowRight, Check, VideoPause, VideoPlay,
  Loading, CircleCheckFilled, WarningFilled
} from '@element-plus/icons-vue'
import {
  startExam, submitExam, saveAnswer, saveAnswers,
  pauseExam, resumeExam, getCurrentExam, getRecordAnswers, getExamQuestions
} from '../../api/record'
import { getExamDetail } from '../../api/exam'
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
const recordInfo = reactive({ id: null, status: 0, totalPauseTime: 0 })
const isPaused = ref(false)
const saveStatus = ref('saved')
const submitted = ref(false)

let timerHandle = null
let autoSaveHandle = null
let debounceTimer = null

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

const answeredCount = computed(() => {
  return questions.value.filter(q => isAnswered(q.id)).length
})

const saveStatusClass = computed(() => {
  return {
    'status-saving': saveStatus.value === 'saving',
    'status-saved': saveStatus.value === 'saved',
    'status-error': saveStatus.value === 'error'
  }
})

const saveStatusText = computed(() => {
  const map = {
    saving: '保存中...',
    saved: '已保存',
    error: '保存失败'
  }
  return map[saveStatus.value] || ''
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

const startTimer = (endTimeStr, pauseTimeMs = 0) => {
  const end = new Date(endTimeStr).getTime() + pauseTimeMs
  const tick = () => {
    if (isPaused.value) return
    remainingMs.value = Math.max(0, end - Date.now())
    if (remainingMs.value <= 0 && !submitted.value) {
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
      if (!answers[q.id]) answers[q.id] = []
    } else {
      if (!answers[q.id]) answers[q.id] = ''
    }
  })
}

const restoreAnswers = (savedAnswers) => {
  if (!savedAnswers || savedAnswers.length === 0) return
  savedAnswers.forEach(item => {
    const q = questions.value.find(q => q.id === item.questionId)
    if (q) {
      if (q.type === 2 && item.answer) {
        answers[item.questionId] = item.answer.split('')
      } else {
        answers[item.questionId] = item.answer || ''
      }
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

const buildSaveAllData = () => {
  const answerList = questions.value.map(q => {
    let ans = answers[q.id]
    if (Array.isArray(ans)) {
      ans = [...ans].sort().join('')
    }
    return { questionId: q.id, answer: ans || '' }
  })
  return { recordId: recordInfo.id, answers: answerList }
}

const doSubmit = async (auto) => {
  if (submitted.value) return
  submitted.value = true
  if (timerHandle) clearInterval(timerHandle)
  if (autoSaveHandle) clearInterval(autoSaveHandle)
  try {
    const data = buildSubmitData()
    const res = await submitExam(data)
    const record = res.data
    await ElMessageBox.alert(
      `考试已提交！\n得分：${record.score} / ${record.totalScore}`,
      auto ? '考试时间已到' : '提交成功',
      { confirmButtonText: '查看成绩', type: 'success' }
    )
    const role = userStore.userInfo?.role
    if (role === 1 || role === 2) {
      router.push('/score')
    } else {
      router.push('/personal-score')
    }
  } catch {
    submitted.value = false
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

const handleAnswerChange = async (questionId) => {
  if (isPaused.value || submitted.value) return

  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    await doSaveSingleAnswer(questionId)
  }, 800)
}

const doSaveSingleAnswer = async (questionId) => {
  if (!recordInfo.id) return
  saveStatus.value = 'saving'
  try {
    let ans = answers[questionId]
    if (Array.isArray(ans)) {
      ans = [...ans].sort().join('')
    }
    await saveAnswer({
      recordId: recordInfo.id,
      questionId,
      answer: ans || ''
    })
    saveStatus.value = 'saved'
  } catch {
    saveStatus.value = 'error'
  }
}

const doSaveAllAnswers = async () => {
  if (!recordInfo.id || isPaused.value || submitted.value) return
  saveStatus.value = 'saving'
  try {
    await saveAnswers(buildSaveAllData())
    saveStatus.value = 'saved'
  } catch {
    saveStatus.value = 'error'
  }
}

const startAutoSave = () => {
  if (autoSaveHandle) clearInterval(autoSaveHandle)
  autoSaveHandle = setInterval(() => {
    if (!isPaused.value && !submitted.value) {
      doSaveAllAnswers()
    }
  }, 30000)
}

const handlePause = async () => {
  if (!recordInfo.id) return
  try {
    await ElMessageBox.confirm(
      '暂停后考试计时器将停止，确定要暂停吗？',
      '暂停确认',
      { type: 'warning' }
    )
    await doSaveAllAnswers()
    const res = await pauseExam({ recordId: recordInfo.id })
    Object.assign(recordInfo, res.data)
    isPaused.value = true
    ElMessage.success('考试已暂停')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '暂停失败')
    }
  }
}

const handleResume = async () => {
  if (!recordInfo.id) return
  try {
    const res = await resumeExam({ recordId: recordInfo.id })
    Object.assign(recordInfo, res.data)
    isPaused.value = false
    const pauseTimeMs = (recordInfo.totalPauseTime || 0) * 1000
    if (examInfo.endTime) {
      if (timerHandle) clearInterval(timerHandle)
      startTimer(examInfo.endTime, pauseTimeMs)
    }
    ElMessage.success('已恢复答题')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '恢复失败')
  }
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

const handleBeforeUnload = (e) => {
  if (!submitted.value) {
    doSaveAllAnswers()
    e.preventDefault()
    e.returnValue = '您有未提交的答题，确定要离开吗？进度已自动保存。'
  }
}

const init = async () => {
  try {
    let recordRes
    let record

    const currentRes = await getCurrentExam()
    if (currentRes.data && currentRes.data.examId === examId) {
      record = currentRes.data
    } else {
      recordRes = await startExam(examId)
      record = recordRes.data
    }

    Object.assign(recordInfo, record)
    isPaused.value = record.status === 3

    const examRes = await getExamDetail(record.examId || examId)
    Object.assign(examInfo, examRes.data)

    const questionsRes = await getExamQuestions(record.id)
    questions.value = questionsRes.data || []
    initAnswers(questions.value)

    try {
      const answersRes = await getRecordAnswers(record.id)
      restoreAnswers(answersRes.data)
    } catch {}

    const pauseTimeMs = (record.totalPauseTime || 0) * 1000
    startTimer(examInfo.endTime, pauseTimeMs)
    startAutoSave()

    window.addEventListener('beforeunload', handleBeforeUnload)
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
  if (autoSaveHandle) clearInterval(autoSaveHandle)
  if (debounceTimer) clearTimeout(debounceTimer)
  window.removeEventListener('beforeunload', handleBeforeUnload)
  if (!submitted.value) {
    doSaveAllAnswers()
  }
})
</script>

<style scoped>
.exam-take {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 120px);
  position: relative;
}

.pause-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pause-content {
  background: #fff;
  padding: 48px 64px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.pause-icon {
  font-size: 64px;
  color: #e6a23c;
  margin-bottom: 16px;
}

.pause-content h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.pause-content p {
  margin: 0 0 24px 0;
  color: #909399;
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.save-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.save-status.status-saving {
  color: #a0cfff;
}

.save-status.status-saved {
  color: #67c23a;
}

.save-status.status-error {
  color: #f56c6c;
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

  .header-right {
    gap: 8px;
  }

  .save-status {
    font-size: 12px;
    padding: 4px 8px;
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

  .pause-content {
    padding: 32px 24px;
    margin: 0 16px;
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
