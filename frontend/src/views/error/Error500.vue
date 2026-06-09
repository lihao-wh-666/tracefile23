<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-code">500</div>
      <div class="error-message">服务器错误</div>
      <div class="error-detail">服务器遇到了一些问题，请稍后再试</div>
      <div class="error-info" v-if="errorInfo">
        <div class="info-item" v-if="errorInfo.traceId">
          <span class="label">Trace ID:</span>
          <span class="value">{{ errorInfo.traceId }}</span>
          <el-button link size="small" @click="copyTraceId">复制</el-button>
        </div>
        <div class="info-item" v-if="errorInfo.detail">
          <span class="label">详情:</span>
          <span class="value">{{ errorInfo.detail }}</span>
        </div>
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="reload">刷新页面</el-button>
        <el-button @click="goHome">返回首页</el-button>
        <el-button type="danger" plain @click="exportLogs">导出日志</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import logger from '../../utils/logger'
import errorHandler from '../../utils/errorHandler'

const props = defineProps({
  errorInfo: {
    type: Object,
    default: null
  }
})

const router = useRouter()

const reload = () => {
  window.location.reload()
}

const goHome = () => {
  router.push('/dashboard')
}

const copyTraceId = () => {
  if (props.errorInfo?.traceId) {
    navigator.clipboard.writeText(props.errorInfo.traceId)
    errorHandler.showSuccessToast('Trace ID 已复制')
  }
}

const exportLogs = () => {
  logger.exportLogs()
  errorHandler.showSuccessToast('日志已导出')
}
</script>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.error-container {
  text-align: center;
  color: #fff;
  padding: 40px;
  max-width: 600px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.error-message {
  font-size: 32px;
  font-weight: 500;
  margin-bottom: 16px;
}

.error-detail {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 30px;
}

.error-info {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 30px;
  text-align: left;
  backdrop-filter: blur(10px);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  font-size: 14px;
}

.info-item .label {
  font-weight: 500;
  opacity: 0.8;
}

.info-item .value {
  flex: 1;
  word-break: break-all;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.error-actions .el-button {
  padding: 12px 32px;
  font-size: 16px;
}
</style>
