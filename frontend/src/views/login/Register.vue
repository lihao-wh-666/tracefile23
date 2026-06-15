<template>
  <div class="register-container">
    <div class="login-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>
    <el-card class="register-card" shadow="always">
      <div class="register-header">
        <div class="register-icon">
          <el-icon :size="48"><User /></el-icon>
        </div>
        <h2 class="register-title">用户注册</h2>
        <p class="register-subtitle">创建您的账户</p>
      </div>
      <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="0" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20位）"
            :prefix-icon="User"
            size="large"
            class="register-input"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            size="large"
            class="register-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            :prefix-icon="Lock"
            size="large"
            show-password
            @input="checkPasswordStrength"
            class="register-input"
          />
          <div class="password-strength" v-if="registerForm.password">
            <div class="strength-bar">
              <div class="strength-fill" :class="strengthClass" :style="{ width: strengthWidth }"></div>
            </div>
            <span class="strength-text">{{ strengthText }}</span>
          </div>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            class="register-input"
          />
        </el-form-item>
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意<a href="#" class="agreement-link">《用户协议》</a>和<a href="#" class="agreement-link">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <p>已有账户？<router-link to="/login" class="login-link">立即登录</router-link></p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPublicKey, register } from '../../api/auth'
import { JSEncrypt } from 'jsencrypt'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const publicKey = ref('')
const strengthWidth = ref('0%')
const strengthClass = ref('')
const strengthText = ref('')

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const checkPasswordStrength = () => {
  const password = registerForm.password
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

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const encryptedPassword = encryptPassword(registerForm.password)
    const encryptedConfirmPassword = encryptPassword(registerForm.confirmPassword)

    await register({
      username: registerForm.username,
      email: registerForm.email,
      password: encryptedPassword,
      confirmPassword: encryptedConfirmPassword
    })

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (err) {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPublicKey()
})
</script>

<style scoped>
.register-container {
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

.register-card {
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

.register-header {
  text-align: center;
  margin-bottom: 36px;
}

.register-icon {
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

.register-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.register-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.register-form {
  margin-bottom: 24px;
}

.register-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 16px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}

.register-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.register-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
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

.agreement-link {
  color: #409eff;
  text-decoration: none;
}

.agreement-link:hover {
  text-decoration: underline;
}

.register-btn {
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

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
}

.register-footer p {
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
  .register-container {
    padding: 16px;
  }

  .register-card {
    padding: 30px 20px;
    border-radius: 12px;
  }

  .register-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
  }

  .register-icon .el-icon {
    font-size: 32px;
  }

  .register-title {
    font-size: 22px;
  }

  .register-subtitle {
    font-size: 12px;
  }

  .register-header {
    margin-bottom: 28px;
  }

  .register-btn {
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
  .register-container {
    padding: 12px;
    align-items: flex-start;
    padding-top: 8vh;
  }

  .register-card {
    padding: 24px 16px;
  }

  .register-icon {
    width: 56px;
    height: 56px;
  }

  .register-icon .el-icon {
    font-size: 28px;
  }

  .register-title {
    font-size: 20px;
  }
}

@media screen and (min-width: 769px) and (max-width: 1024px) {
  .register-card {
    max-width: 400px;
  }
}
</style>
