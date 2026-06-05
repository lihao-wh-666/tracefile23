<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <div class="login-icon">
          <el-icon :size="48"><Reading /></el-icon>
        </div>
        <h2 class="login-title">在线考试系统</h2>
        <p class="login-subtitle">随时随地，轻松考试</p>
      </div>
      <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            class="login-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
            class="login-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p>无需下载APP · 浏览器直接访问</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    router.push('/')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3c72, #2a5298);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 400px;
  height: 400px;
  bottom: -150px;
  right: -150px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.5;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 30px;
  border-radius: 16px;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
}

.login-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 16px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
}

.login-footer p {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

@media screen and (max-width: 768px) {
  .login-container {
    padding: 16px;
  }

  .login-card {
    padding: 30px 20px;
    border-radius: 12px;
  }

  .login-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
  }

  .login-icon .el-icon {
    font-size: 32px;
  }

  .login-title {
    font-size: 22px;
  }

  .login-subtitle {
    font-size: 12px;
  }

  .login-header {
    margin-bottom: 28px;
  }

  .login-btn {
    height: 44px;
    font-size: 15px;
  }

  .circle-1,
  .circle-2,
  .circle-3 {
    display: none;
  }
}

@media screen and (max-width: 480px) {
  .login-container {
    padding: 12px;
    align-items: flex-start;
    padding-top: 8vh;
  }

  .login-card {
    padding: 24px 16px;
  }

  .login-icon {
    width: 56px;
    height: 56px;
  }

  .login-icon .el-icon {
    font-size: 28px;
  }

  .login-title {
    font-size: 20px;
  }
}

@media screen and (min-width: 769px) and (max-width: 1024px) {
  .login-card {
    max-width: 400px;
  }
}
</style>
