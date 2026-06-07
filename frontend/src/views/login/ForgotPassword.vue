<template>
  <div class="forgot-container">
    <div class="login-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>
    <el-card class="forgot-card" shadow="always">
      <div class="forgot-header">
        <div class="forgot-icon">
          <el-icon :size="48"><Lock /></el-icon>
        </div>
        <h2 class="forgot-title">找回密码</h2>
        <p class="forgot-subtitle">重置您的密码</p>
      </div>
      <el-form ref="formRef" :model="forgotForm" :rules="rules" label-width="0" class="forgot-form">
        <el-form-item prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
            size="large"
            class="forgot-input"
          />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input-wrapper">
            <el-input
              v-model="forgotForm.code"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              size="large"
              class="forgot-input code-input"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="codeCountdown > 0"
              @click="handleSendCode"
              class="send-code-btn"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            :prefix-icon="Lock"
            size="large"
            show-password
            @input="checkPasswordStrength"
            class="forgot-input"
          />
          <div class="password-strength" v-if="forgotForm.newPassword">
            <div class="strength-bar">
              <div class="strength-fill" :class="strengthClass" :style="{ width: strengthWidth }"></div>
            </div>
            <span class="strength-text">{{ strengthText }}</span>
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            class="forgot-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="forgot-btn"
            :loading="loading"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
      <div class="forgot-footer">
        <p>想起密码？<router-link to="/login" class="login-link">返回登录</router-link></p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Message, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPublicKey, sendCode, resetPassword } from '../../api/auth'
import { JSEncrypt } from 'jsencrypt'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const publicKey = ref('')
const codeCountdown = ref(0)
const strengthWidth = ref('0%')
const strengthClass = ref('')
const strengthText = ref('')

const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const checkPasswordStrength = () => {
  const password = forgotForm.newPassword
  let strength = 0

  if (password.length >= 6) strength += 25
  if (password.length >= 10) strength += 25
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength += 25
  if (/[0-9]/.test(password)) strength += 25
  if (/[^a-zA-Z0-9]/.test(password)) strength += 25

  strength = Math.min(strength, 100)

  strengthWidth.value = strength + '%'

  if (strength < 40) {
    strengthClass.value = 'weak'
    strengthText.value = '弱'
  } else if (strength < 70) {
    strengthClass.value = 'medium'
    strengthText.value = '中'
  } else {
    strengthClass.value = 'strong'
    strengthText.value = '强'
  }
}

const loadPublicKey = async () => {
  try {
    const res = await getPublicKey()
    publicKey.value = res.data.publicKey
  } catch (err) {
    console.error('获取公钥失败', err)
  }
}

const encryptPassword = (password) => {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(publicKey.value)
  return encrypt.encrypt(password)
}

const handleSendCode = async () => {
  if (!forgotForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  const emailRule = rules.email.find(r => r.type === 'email')
  if (emailRule && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(forgotForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    await sendCode({ email: forgotForm.email })
    ElMessage.success('验证码已发送，请查看控制台（测试环境）')

    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '发送失败，请稍后重试')
  }
}

const handleResetPassword = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const encryptedNewPassword = encryptPassword(forgotForm.newPassword)
    const encryptedConfirmPassword = encryptPassword(forgotForm.confirmPassword)

    await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: encryptedNewPassword,
      confirmPassword: encryptedConfirmPassword
    })

    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '重置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPublicKey()
})
</script>

<style scoped>
.forgot-container {
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

.forgot-card {
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

.forgot-header {
  text-align: center;
  margin-bottom: 36px;
}

.forgot-icon {
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

.forgot-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.forgot-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.forgot-form {
  margin-bottom: 24px;
}

.forgot-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 16px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.forgot-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.forgot-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  white-space: nowrap;
  border-radius: 10px;
  font-weight: 500;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
}

.send-code-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 3px;
  transition: all 0.3s;
}

.strength-fill.weak {
  background: #f56c6c;
}

.strength-fill.medium {
  background: #e6a23c;
}

.strength-fill.strong {
  background: #67c23a;
}

.strength-text {
  font-size: 12px;
  color: #909399;
  min-width: 20px;
}

.forgot-btn {
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

.forgot-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.forgot-btn:active {
  transform: translateY(0);
}

.forgot-footer {
  text-align: center;
}

.forgot-footer p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-link {
  color: #409eff;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}

@media screen and (max-width: 768px) {
  .forgot-container {
    padding: 16px;
  }

  .forgot-card {
    padding: 30px 20px;
    border-radius: 12px;
  }

  .forgot-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
  }

  .forgot-icon .el-icon {
    font-size: 32px;
  }

  .forgot-title {
    font-size: 22px;
  }

  .forgot-subtitle {
    font-size: 12px;
  }

  .forgot-header {
    margin-bottom: 28px;
  }

  .forgot-btn {
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
  .forgot-container {
    padding: 12px;
    align-items: flex-start;
    padding-top: 8vh;
  }

  .forgot-card {
    padding: 24px 16px;
  }

  .forgot-icon {
    width: 56px;
    height: 56px;
  }

  .forgot-icon .el-icon {
    font-size: 28px;
  }

  .forgot-title {
    font-size: 20px;
  }

  .code-input-wrapper {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }
}

@media screen and (min-width: 769px) and (max-width: 1024px) {
  .forgot-card {
    max-width: 400px;
  }
}
</style>
