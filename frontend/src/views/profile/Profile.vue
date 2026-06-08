<template>
  <div class="profile-container">
    <h2 class="page-title">个人中心</h2>

    <el-card shadow="hover" class="profile-card">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="基本信息" name="info">
          <div class="info-section">
            <div class="avatar-section">
              <el-avatar :size="100" :src="getAvatarUrl(userStore.userInfo?.avatar)">
                <el-icon :size="40"><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</div>
              <div class="user-role">
                <el-tag :type="roleTagMap[userStore.userInfo?.role]" size="small">
                  {{ roleMap[userStore.userInfo?.role] }}
                </el-tag>
              </div>
            </div>

            <el-form ref="formRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="注册时间">
                <el-input :value="userStore.userInfo?.createTime" disabled />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="头像设置" name="avatar">
          <div class="avatar-upload-section">
            <div class="current-avatar">
              <div class="section-title">当前头像</div>
              <el-avatar :size="150" :src="getAvatarUrl(userStore.userInfo?.avatar)" class="avatar-preview">
                <el-icon :size="60"><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="upload-section">
              <div class="section-title">上传新头像</div>
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <div class="upload-trigger">
                  <el-icon :size="40" class="upload-icon"><Plus /></el-icon>
                  <div class="upload-text">点击上传头像</div>
                  <div class="upload-tip">支持 JPG、PNG、GIF 格式，大小不超过 5MB</div>
                </div>
              </el-upload>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="账户安全" name="security">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="120px" class="password-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              <div class="form-tip">密码长度不能少于 6 位</div>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '../../store/user'
import { getPublicKey } from '../../api/auth'
import { encryptPassword } from '../../utils/rsa'
import { changePassword } from '../../api/user'

const userStore = useUserStore()
const activeTab = ref('info')
const formRef = ref(null)
const passwordFormRef = ref(null)
const profileLoading = ref(false)
const passwordLoading = ref(false)
const publicKey = ref('')

const roleMap = { 1: '管理员', 2: '教师', 3: '学生' }
const roleTagMap = { 1: 'danger', 2: 'warning', 3: 'info' }

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/api')) return avatar
  return '/api' + avatar
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const initProfileForm = () => {
  if (userStore.userInfo) {
    profileForm.username = userStore.userInfo.username || ''
    profileForm.realName = userStore.userInfo.realName || ''
    profileForm.phone = userStore.userInfo.phone || ''
    profileForm.email = userStore.userInfo.email || ''
  }
}

const handleUpdateProfile = async () => {
  await formRef.value.validate()
  profileLoading.value = true
  try {
    await userStore.updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email
    })
    ElMessage.success('个人信息更新成功')
  } finally {
    profileLoading.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options) => {
  try {
    await userStore.uploadAvatar(options.file)
    ElMessage.success('头像上传成功')
  } catch (err) {
    ElMessage.error(err?.message || '头像上传失败')
  }
}

const fetchPublicKey = async () => {
  const res = await getPublicKey()
  publicKey.value = res.data?.publicKey || ''
}

const handleChangePassword = async () => {
  await passwordFormRef.value.validate()
  passwordLoading.value = true
  try {
    const encryptedOldPassword = encryptPassword(publicKey.value, passwordForm.oldPassword)
    const encryptedNewPassword = encryptPassword(publicKey.value, passwordForm.newPassword)
    await changePassword({
      oldPassword: encryptedOldPassword,
      newPassword: encryptedNewPassword,
      confirmPassword: encryptedNewPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  initProfileForm()
  fetchPublicKey()
})
</script>

<style scoped>
.profile-container {
  width: 100%;
}

.page-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.profile-card {
  border-radius: 8px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.info-section {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 180px;
  padding-top: 20px;
}

.user-name {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.user-role {
  margin-top: 8px;
}

.profile-form {
  flex: 1;
  max-width: 500px;
}

.avatar-upload-section {
  display: flex;
  gap: 60px;
  padding: 20px 0;
}

.current-avatar,
.upload-section {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.avatar-preview {
  border: 2px solid #ebeef5;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 200px;
  padding: 20px;
  text-align: center;
}

.upload-icon {
  color: #8c939d;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.password-form {
  max-width: 500px;
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

@media screen and (max-width: 768px) {
  .info-section {
    flex-direction: column;
    gap: 24px;
  }

  .avatar-section {
    min-width: auto;
    padding-top: 0;
  }

  .avatar-upload-section {
    flex-direction: column;
    gap: 30px;
  }

  .upload-trigger {
    width: 100%;
    height: 180px;
  }

  .profile-form,
  .password-form {
    max-width: 100%;
  }
}
</style>
