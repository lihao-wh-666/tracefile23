<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-code">404</div>
      <div class="error-message">页面不存在</div>
      <div class="error-detail">您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    goHome()
  }
}
</script>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-container {
  text-align: center;
  color: #fff;
  padding: 40px;
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
  margin-bottom: 40px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.error-actions .el-button {
  padding: 12px 32px;
  font-size: 16px;
}
</style>
