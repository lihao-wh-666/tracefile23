<template>
  <div class="layout-container">
    <div class="mobile-header mobile-only">
      <div class="menu-toggle" @click="toggleSidebar">
        <el-icon :size="24"><Menu /></el-icon>
      </div>
      <div class="mobile-logo">在线考试系统</div>
      <el-dropdown @command="handleCommand" class="mobile-user">
        <el-icon :size="22"><UserFilled /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="sidebar-overlay" :class="{ active: sidebarOpen }" @click="closeSidebar"></div>

    <el-aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="logo mobile-hidden">在线考试系统</div>
      <div class="mobile-only sidebar-logo">
        <span>在线考试系统</span>
        <el-icon @click="closeSidebar" class="close-icon"><Close /></el-icon>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/question">
          <el-icon><Document /></el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/paper">
          <el-icon><Notebook /></el-icon>
          <span>试卷管理</span>
        </el-menu-item>
        <el-menu-item index="/exam">
          <el-icon><EditPen /></el-icon>
          <span>考试管理</span>
        </el-menu-item>
        <el-menu-item index="/score">
          <el-icon><DataAnalysis /></el-icon>
          <span>成绩统计</span>
        </el-menu-item>
        <el-menu-item index="/personal-score">
          <el-icon><User /></el-icon>
          <span>个人成绩台账</span>
        </el-menu-item>
        <el-menu-item index="/user" v-if="userStore.userInfo?.role === 1">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="desktop-header mobile-hidden">
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            {{ userStore.userInfo?.username || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { HomeFilled, Document, Notebook, EditPen, DataAnalysis, User, ArrowDown, Menu, Close, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const sidebarOpen = ref(false)

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

const handleMenuSelect = () => {
  if (window.innerWidth <= 768) {
    closeSidebar()
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  }
}

const handleResize = () => {
  if (window.innerWidth > 768) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: linear-gradient(135deg, #1e3c72, #2a5298);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 1001;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.menu-toggle {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.menu-toggle:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.mobile-logo {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.mobile-user {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.mobile-user:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.sidebar-overlay.active {
  opacity: 1;
  visibility: visible;
}

.sidebar {
  width: 220px;
  background-color: #304156;
  overflow: hidden;
  transition: transform 0.3s ease;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  background-color: #263445;
}

.close-icon {
  cursor: pointer;
  font-size: 20px;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-icon:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.desktop-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  height: 60px;
  padding: 0 20px;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #333;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 16px;
}

@media screen and (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: 260px;
    transform: translateX(-100%);
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .main-container {
    width: 100%;
  }

  .main-content {
    padding-top: 72px;
    padding-left: 12px;
    padding-right: 12px;
  }
}

@media screen and (min-width: 769px) and (max-width: 1024px) {
  .sidebar {
    width: 200px;
  }

  .logo {
    font-size: 16px;
  }
}

@media screen and (max-width: 768px) {
  .main-content {
    padding: 68px 12px 16px;
  }
}
</style>
