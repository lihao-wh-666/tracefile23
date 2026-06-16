<template>
  <div class="log-masking-container">
    <el-card class="header-card">
      <div class="header-content">
        <div>
        <h2 class="page-title">
          <el-icon :size="24"><Lock /></el-icon>
          {{ t('logMasking.title') }}
        </h2>
        <p class="page-desc">{{ t('logMasking.description') }}</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="DocumentAdd" @click="loadSampleData">
            {{ t('logMasking.loadSample') }}
          </el-button>
          <el-button :icon="RefreshLeft" @click="resetAll">
            {{ t('common.reset') }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="main-row">
      <el-col :span="24" :lg="12">
        <el-card class="input-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Edit /></el-icon>
                {{ t('logMasking.input') }}
              </span>
              <div class="card-actions">
                <el-radio-group v-model="inputMode" size="small">
                  <el-radio-button value="text">
                    <el-icon><Document /></el-icon>
                    {{ t('logMasking.textInput') }}
                  </el-radio-button>
                  <el-radio-button value="file">
                    <el-icon><Upload /></el-icon>
                    {{ t('logMasking.fileInput') }}
                  </el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>

          <div v-if="inputMode === 'text'" class="text-input-area">
            <div class="format-selector">
              <span class="label">{{ t('logMasking.format') }}:</span>
              <el-select v-model="selectedFormat" size="small" style="width: 150px">
                <el-option label="自动检测" value="auto" />
                <el-option label="JSON" value="json" />
                <el-option label="文本" value="text" />
                <el-option label="CSV" value="csv" />
              </el-select>
              <span v-if="detectedFormat && selectedFormat === 'auto'" class="detected-format">
                {{ t('logMasking.detected') }}: <el-tag size="small" type="info">{{ detectedFormat }}</el-tag>
              </span>
            </div>
            <el-input
              v-model="inputContent"
              type="textarea"
              :placeholder="t('logMasking.placeholder')"
              :rows="18"
              resize="vertical"
              @input="handleContentChange"
            />
          </div>

          <div v-else class="file-input-area">
            <el-upload
              ref="uploadRef"
              class="upload-area"
              drag
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              accept=".log,.txt,.json,.csv,.xml"
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="upload-text">{{ t('logMasking.dragFile') }}</div>
              <template #tip>
                <div class="upload-tip">{{ t('logMasking.supportedFormats') }}: .log, .txt, .json, .csv</div>
              </template>
            </el-upload>
            <div v-if="selectedFile" class="selected-file-info">
              <el-icon><Document /></el-icon>
              <span>{{ selectedFile.name }}</span>
              <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
              <el-button type="text" @click="clearFile">{{ t('common.clear') }}</el-button>
            </div>
            <div class="format-selector" style="margin-top: 16px">
              <span class="label">{{ t('logMasking.format') }}:</span>
              <el-select v-model="selectedFormat" size="small" style="width: 150px">
                <el-option label="自动检测" value="auto" />
                <el-option label="JSON" value="json" />
                <el-option label="文本" value="text" />
                <el-option label="CSV" value="csv" />
              </el-select>
            </div>
          </div>

          <div class="action-buttons">
            <el-button type="primary" :loading="processing" :disabled="!canProcess" @click="processMasking">
              <el-icon><Lock /></el-icon>
              {{ t('logMasking.process') }}
            </el-button>
            <el-button :loading="previewing" :disabled="!canProcess" @click="previewMasking">
              <el-icon><View /></el-icon>
              {{ t('logMasking.preview') }}
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="24" :lg="12">
        <el-card class="output-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Check /></el-icon>
                {{ t('logMasking.output') }}
              </span>
              <div class="card-actions">
                <el-button size="small" :icon="Download" :disabled="!hasResult" @click="downloadResult">
                  {{ t('common.download') }}
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="!hasResult" class="empty-state">
            <el-empty :description="t('logMasking.noResult')">
              <template #image>
                <el-icon :size="64" color="#dcdfe6"><Lock /></el-icon>
              </template>
            </el-empty>
          </div>

          <div v-else class="result-content">
            <div v-if="processingInfo" class="processing-info">
              <el-alert :title="t('logMasking.processSuccess')" type="success" :closable="false" show-icon>
                <template #default>
                  <div class="info-grid">
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.detectedFormat') }}:</span>
                      <el-tag size="small">{{ processingInfo.detectedFormat }}</el-tag>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.originalSize') }}:</span>
                      <span>{{ processingInfo.originalSize }} {{ t('common.bytes') }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.maskedSize') }}:</span>
                      <span>{{ processingInfo.maskedSize }} {{ t('common.bytes') }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.changedLines') }}:</span>
                      <el-tag size="small" type="warning">{{ processingInfo.changedLines }}</el-tag>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.totalChanges') }}:</span>
                      <el-tag size="small" type="danger">{{ processingInfo.totalChanges }}</el-tag>
                    </div>
                    <div class="info-item">
                      <span class="info-label">{{ t('logMasking.processTime') }}:</span>
                      <span>{{ processingInfo.processTime }}</span>
                    </div>
                  </div>
                </template>
              </el-alert>
            </div>

            <div class="output-textarea-wrapper">
              <el-input
                v-model="outputContent"
                type="textarea"
                :rows="16"
                readonly
                resize="vertical"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="showComparison" class="comparison-card">
      <template #header>
        <div class="card-header">
        <span class="card-title">
          <el-icon><DataLine /></el-icon>
          {{ t('logMasking.comparison') }}
        </span>
        <el-button size="small" @click="showComparison = false">
          {{ t('common.close') }}
        </el-button>
        </div>
      </template>

      <div class="comparison-content">
        <el-table :data="comparisonData" stripe style="width: 100%">
          <el-table-column prop="lineNumber" :label="t('logMasking.lineNumber')" width="100" align="center" />
          <el-table-column :label="t('logMasking.original')" min-width="300">
            <template #default="{ row }">
              <div class="original-text">{{ row.original }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="t('logMasking.masked')" min-width="300">
            <template #default="{ row }">
              <div class="masked-text">
                <span v-html="highlightMask(row.masked)" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="changeCount" :label="t('logMasking.changeCount')" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="danger">{{ row.changeCount }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><InfoFilled /></el-icon>
            {{ t('logMasking.sensitiveKeys') }}
          </span>
          <el-tag size="small" type="info">{{ t('logMasking.total') }}: {{ sensitiveKeys.length }}</el-tag>
        </div>
      </template>
      <div class="sensitive-keys-container">
        <el-tag
          v-for="key in sensitiveKeys"
          :key="key"
          size="small"
          type="warning"
          class="sensitive-key-tag"
        >
          {{ key }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock, DocumentAdd, RefreshLeft, Edit, Document, Upload,
  UploadFilled, Check, Download, View, DataLine, InfoFilled
} from '@element-plus/icons-vue'
import {
  getMaskingConfig,
  maskLogContent,
  maskLogFile,
  previewMasking as apiPreviewMasking,
  downloadMaskedFile
} from '../../api/logMasking'

const { t } = useI18n()

const inputMode = ref('text')
const selectedFormat = ref('auto')
const inputContent = ref('')
const outputContent = ref('')
const detectedFormat = ref('')
const selectedFile = ref(null)
const processing = ref(false)
const previewing = ref(false)
const hasResult = ref(false)
const showComparison = ref(false)
const taskId = ref('')
const comparisonData = ref([])
const sensitiveKeys = ref([])
const processingInfo = ref(null)
const uploadRef = ref(null)

const sampleData = `{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "message": "User login attempt",
  "user": {
    "id": 123,
    "username": "admin",
    "password": "MySecretPass123!",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlIjoxLCJpYXQiOjE3MDUzNDIwMDAsImV4cCI6MTcwNTQyODQwMH0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
  },
  "request": {
    "method": "POST",
    "url": "/api/auth/login",
    "headers": {
      "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
      "X-API-Key": "sk_demo_abcdefghijklmnopqrstuvwxyz1234567890"
    },
    "body": {
      "username": "admin",
      "password": "MySecretPass123!"
    }
  },
  "database": {
    "connection": "mysql://root:root123@localhost:3306/exam_db",
    "query": "SELECT * FROM users WHERE username = 'admin'"
  },
  "api_key": "sk_test_abcdefghijklmnopqrstuvwxyz"
}`

const canProcess = computed(() => {
  if (inputMode.value === 'text') {
    return inputContent.value.trim().length > 0
  } else {
    return selectedFile.value !== null
  }
})

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const highlightMask = (text) => {
  if (!text) return ''
  return text.replace(/\*\*\*/g, '<span class="mask-highlight">***</span>')
}

const handleContentChange = () => {
  if (selectedFormat.value === 'auto' && inputContent.value.trim()) {
    const content = inputContent.value.trim()
    if ((content.startsWith('{') && content.endsWith('}')) ||
        (content.startsWith('[') && content.endsWith(']'))) {
      try {
        JSON.parse(content)
        detectedFormat.value = 'json'
        return
      } catch (e) {}
    }
    const lines = content.split('\n')
    if (lines.length > 1) {
      const firstLine = lines[0]
      if (firstLine.includes(',') && !firstLine.includes(' ')) {
        detectedFormat.value = 'csv'
        return
      }
    }
    detectedFormat.value = 'text'
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const clearFile = () => {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

const loadSampleData = () => {
  inputContent.value = sampleData
  inputMode.value = 'text'
  selectedFormat.value = 'auto'
  handleContentChange()
}

const resetAll = () => {
  inputContent.value = ''
  outputContent.value = ''
  selectedFile.value = null
  hasResult.value = false
  showComparison.value = false
  processingInfo.value = null
  detectedFormat.value = ''
  taskId.value = ''
  comparisonData.value = []
  uploadRef.value?.clearFiles()
}

const processMasking = async () => {
  if (!canProcess.value) return

  processing.value = true
  try {
    let result
    if (inputMode.value === 'text') {
      result = await maskLogContent(inputContent.value, selectedFormat.value)
    } else {
      result = await maskLogFile(selectedFile.value, selectedFormat.value)
    }

    if (result.code === 200) {
      const data = result.data
      outputContent.value = data.maskedContent
      hasResult.value = true
      taskId.value = data.taskId
      detectedFormat.value = data.detectedFormat

      processingInfo.value = {
        detectedFormat: data.detectedFormat,
        originalSize: data.originalSize,
        maskedSize: data.maskedSize,
        changedLines: data.comparison.changedLines,
        totalChanges: data.comparison.totalChanges,
        processTime: data.processTime
      }

      comparisonData.value = data.comparison.differences || []
      showComparison.value = comparisonData.value.length > 0

      ElMessage.success(t('logMasking.processSuccess'))
    } else {
      ElMessage.error(result.message || t('logMasking.processFailed'))
    }
  } catch (error) {
    ElMessage.error(t('logMasking.processFailed'))
  } finally {
    processing.value = false
  }
}

const previewMasking = async () => {
  if (!canProcess.value) return

  previewing.value = true
  try {
    const content = inputMode.value === 'text'
      ? inputContent.value
      : await readFileContent(selectedFile.value)

    const result = await apiPreviewMasking(content, selectedFormat.value)

    if (result.code === 200) {
      const data = result.data
      outputContent.value = data.masked
      hasResult.value = true
      detectedFormat.value = data.detectedFormat

      comparisonData.value = data.comparison.differences || []
      showComparison.value = comparisonData.value.length > 0

      processingInfo.value = {
        detectedFormat: data.detectedFormat,
        originalSize: data.comparison.originalSize,
        maskedSize: data.comparison.maskedSize,
        changedLines: data.comparison.changedLines,
        totalChanges: data.comparison.totalChanges,
        processTime: new Date().toLocaleString()
      }

      ElMessage.success(t('logMasking.previewSuccess'))
    }
  } catch (error) {
    ElMessage.error(t('logMasking.previewFailed'))
  } finally {
    previewing.value = false
  }
}

const readFileContent = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = reject
    reader.readAsText(file)
  })
}

const downloadResult = async () => {
  if (!taskId.value) {
    const blob = new Blob([outputContent.value], { type: 'text/plain' })
    downloadBlob(blob, 'masked-log.txt')
    return
  }

  try {
    const result = await downloadMaskedFile(taskId.value)
    if (result instanceof Blob) {
      downloadBlob(result, `masked-log-${Date.now()}.txt`)
    }
  } catch (error) {
    const blob = new Blob([outputContent.value], { type: 'text/plain' })
    downloadBlob(blob, 'masked-log.txt')
  }
}

const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success(t('logMasking.downloadSuccess'))
}

const loadConfig = async () => {
  try {
    const result = await getMaskingConfig()
    if (result.code === 200) {
      sensitiveKeys.value = Array.from(result.data.sensitiveKeys || [])
    }
  } catch (error) {
    console.error('Load config failed:', error)
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.log-masking-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-card {
  margin-bottom: 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.page-desc {
  margin: 8px 0 0;
  color: var(--text-regular);
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.main-row {
  margin-top: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.format-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.label {
  font-weight: 500;
  color: var(--text-regular);
}

.detected-format {
  margin-left: 8px;
}

.text-input-area {
  display: flex;
  flex-direction: column;
}

.file-input-area {
  display: flex;
  flex-direction: column;
}

.upload-area {
  margin-bottom: 16px;
}

.upload-icon {
  font-size: 48px;
  color: var(--color-primary);
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: var(--text-regular);
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: var(--text-secondary);
}

.selected-file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: var(--bg-tertiary);
  border-radius: 8px;
}

.file-size {
  color: var(--text-secondary);
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.processing-info {
  margin-bottom: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  color: var(--text-regular);
}

.output-textarea-wrapper {
  flex: 1;
}

.comparison-card {
  margin-top: 0;
}

.comparison-content {
  max-height: 400px;
  overflow-y: auto;
}

.original-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-all;
}

.masked-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.mask-highlight) {
  background-color: #fef08a;
  color: #dc2626;
  font-weight: 600;
  padding: 0 2px;
  border-radius: 2px;
}

.config-card {
  margin-top: 0;
}

.sensitive-keys-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sensitive-key-tag {
  margin: 0;
}

@media screen and (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
