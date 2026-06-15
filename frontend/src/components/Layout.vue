<template>
  <div class="layout-container">
    <div class="mobile-header mobile-only">
      <div class="menu-toggle" @click="toggleSidebar">
        <el-icon :size="24"><Menu /></el-icon>
      </div>
      <div class="mobile-logo">{{ t('login.title') }}</div>
      <el-dropdown @command="handleCommand" class="mobile-user">
        <el-avatar :size="32" :src="getAvatarUrl(userStore.userInfo?.avatar)">
          <el-icon :size="18"><UserFilled /></el-icon>
        </el-avatar>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">{{ t('menu.profile') }}</el-dropdown-item>
            <el-dropdown-item command="settings">{{ t('menu.settings') }}</el-dropdown-item>
            <el-dropdown-item command="logout" divided>{{ t('profile.logout') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="sidebar-overlay" :class="{ active: sidebarOpen }" @click="closeSidebar"></div>

    <el-aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="logo mobile-hidden">{{ t('login.title') }}</div>
      <div class="mobile-only sidebar-logo">
        <span>{{ t('login.title') }}</span>
        <el-icon @click="closeSidebar" class="close-icon"><Close /></el-icon>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="var(--bg-sidebar)"
        text-color="var(--text-sidebar)"
        active-text-color="var(--text-sidebar-active)"
        router
        @select="handleMenuSelect"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard" v-if="hasRole([1, 2, 3])">
          <el-icon><HomeFilled /></el-icon>
          <span>{{ t('menu.dashboard') }}</span>
        </el-menu-item>
        <el-menu-item index="/question" v-if="hasRole([1, 2])">
          <el-icon><Document /></el-icon>
          <span>{{ t('menu.question') }}</span>
        </el-menu-item>
        <el-menu-item index="/subject" v-if="hasRole([1, 2])">
          <el-icon><Reading /></el-icon>
          <span>{{ t('menu.subject') }}</span>
        </el-menu-item>
        <el-menu-item index="/paper" v-if="hasRole([1, 2])">
          <el-icon><Notebook /></el-icon>
          <span>{{ t('menu.paper') }}</span>
        </el-menu-item>
        <el-menu-item index="/exam" v-if="hasRole([1, 2, 3])">
          <el-icon><EditPen /></el-icon>
          <span>{{ t('menu.exam') }}</span>
        </el-menu-item>
        <el-menu-item index="/score" v-if="hasRole([1, 2])">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ t('menu.score') }}</span>
        </el-menu-item>
        <el-menu-item index="/question-analysis" v-if="hasRole([1, 2])">
          <el-icon><TrendCharts /></el-icon>
          <span>{{ t('menu.questionAnalysis') }}</span>
        </el-menu-item>
        <el-menu-item index="/personal-score" v-if="hasRole([1, 2, 3])">
          <el-icon><User /></el-icon>
          <span>{{ t('menu.personalScore') }}</span>
        </el-menu-item>
        <el-menu-item index="/user" v-if="hasRole([1])">
          <el-icon><UserFilled /></el-icon>
          <span>{{ t('menu.user') }}</span>
        </el-menu-item>
        <el-menu-item index="/system-config" v-if="hasRole([1])">
          <el-icon><Tools /></el-icon>
          <span>{{ t('menu.systemConfig') }}</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>{{ t('menu.profile') }}</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><MagicStick /></el-icon>
          <span>{{ t('menu.settings') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="desktop-header mobile-hidden">
        <div class="header-left">
          <el-dropdown trigger="click" @command="handleQuickTheme" class="theme-quick">
            <span class="header-icon-btn" :title="t('settings.theme')">
              <el-icon :size="20">
                <Sunny v-if="preferencesStore.theme === 'light'" />
                <Moon v-else />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="light">
                  <el-icon><Sunny /></el-icon>
                  <span style="margin-left: 6px">{{ t('settings.themeLight') }}</span>
                </el-dropdown-item>
                <el-dropdown-item command="dark">
                  <el-icon><Moon /></el-icon>
                  <span style="margin-left: 6px">{{ t('settings.themeDark') }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="getAvatarUrl(userStore.userInfo?.avatar)" class="user-avatar">
              <el-icon :size="18"><UserFilled /></el-icon>
            </el-avatar>
            <span class="username-text">{{ userStore.userInfo?.username || t('profile.user') }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">{{ t('menu.profile') }}</el-dropdown-item>
              <el-dropdown-item command="settings">{{ t('menu.settings') }}</el-dropdown-item>
              <el-dropdown-item command="logout" divided>{{ t('profile.logout') }}</el-dropdown-item>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { HomeFilled, Document, Notebook, EditPen, DataAnalysis, User, ArrowDown, Menu, Close, UserFilled, Setting, Reading, Tools, TrendCharts, MagicStick, Sunny, Moon } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { usePreferencesStore } from '../store/preferences'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const preferencesStore = usePreferencesStore()
const sidebarOpen = ref(false)

const hasRole = (roles) => {
  if (!userStore.userInfo?.role) return false
  return roles.includes(userStore.userInfo.role)
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/api')) return avatar
  return '/api' + avatar
}

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
    preferencesStore.clearOnLogout()
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

const handleQuickTheme = async (theme) => {
  preferencesStore.setTheme(theme)
  try {
    await preferencesStore.updatePreferences({ theme })
  } catch (e) {
    console.error('Save theme failed:', e)
  }
}

const handleResize = () => {
  if (window.innerWidth > 768) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  if (preferencesStore.sidebarCollapsed && window.innerWidth > 768) {
    // 桌面端折叠逻辑可扩展
  }
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
  background-color: var(--bg-primary);
}

.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--gradient-header);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 1001;
  box-shadow: var(--shadow-header);
}

.menu-toggle {
  color: var(--text-inverse);
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
  color: var(--text-inverse);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.mobile-user {
  color: var(--text-inverse);
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
  background-color: var(--bg-sidebar);
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
  color: var(--text-inverse);
  font-size: 16px;
  font-weight: 600;
  background-color: var(--bg-sidebar-hover);
}

.close-icon {
  cursor: pointer;
  font-size: 20px;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
  color: var(--text-inverse);
}

.close-icon:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: var(--text-inverse);
  font-size: 18px;
  font-weight: bold;
  background-color: var(--bg-sidebar-hover);
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--bg-sidebar-hover) !important;
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
  justify-content: space-between;
  background-color: var(--bg-header);
  border-bottom: 1px solid var(--border-primary);
  height: 60px;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-quick {
  cursor: pointer;
}

.header-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: var(--text-regular);
  transition: all 0.2s;
}

.header-icon-btn:hover {
  background-color: var(--bg-tertiary);
  color: var(--color-primary);
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-primary);
}

.user-avatar {
  border: 2px solid var(--border-primary);
}

.username-text {
  font-weight: 500;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background-color: var(--bg-primary);
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
    box-shadow: var(--shadow-sidebar);
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
