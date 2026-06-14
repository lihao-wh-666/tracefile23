<template>
  <div class="settings-container">
    <h2 class="page-title">{{ t('settings.title') }}</h2>

    <el-row :gutter="20" class="settings-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="hover" class="settings-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" class="header-icon"><Brush /></el-icon>
              <span>{{ t('settings.appearance') }}</span>
            </div>
          </template>

          <div class="setting-section">
            <div class="setting-label">
              <div class="setting-title">{{ t('settings.theme') }}</div>
              <div class="setting-desc">选择您喜欢的界面主题</div>
            </div>
            <div class="theme-options">
              <div
                class="theme-option"
                :class="{ active: localTheme === 'light' }"
                @click="localTheme = 'light'"
              >
                <div class="theme-preview theme-light">
                  <div class="preview-sidebar"></div>
                  <div class="preview-content">
                    <div class="preview-header"></div>
                    <div class="preview-body">
                      <div class="preview-line"></div>
                      <div class="preview-line short"></div>
                      <div class="preview-line"></div>
                    </div>
                  </div>
                </div>
                <div class="theme-name">
                  <el-radio v-model="localTheme" label="light" class="no-label"></el-radio>
                  <span>{{ t('settings.themeLight') }}</span>
                </div>
              </div>

              <div
                class="theme-option"
                :class="{ active: localTheme === 'dark' }"
                @click="localTheme = 'dark'"
              >
                <div class="theme-preview theme-dark">
                  <div class="preview-sidebar"></div>
                  <div class="preview-content">
                    <div class="preview-header"></div>
                    <div class="preview-body">
                      <div class="preview-line"></div>
                      <div class="preview-line short"></div>
                      <div class="preview-line"></div>
                    </div>
                  </div>
                </div>
                <div class="theme-name">
                  <el-radio v-model="localTheme" label="dark" class="no-label"></el-radio>
                  <span>{{ t('settings.themeDark') }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="setting-section">
            <div class="setting-label">
              <div class="setting-title">{{ t('settings.language') }}</div>
              <div class="setting-desc">选择界面显示语言</div>
            </div>
            <el-radio-group v-model="localLanguage" class="language-group">
              <el-radio-button label="zh-CN">
                <el-icon><ChatDotRound /></el-icon>
                <span style="margin-left: 4px">{{ t('settings.languageZhCN') }}</span>
              </el-radio-button>
              <el-radio-button label="en-US">
                <el-icon><ChatLineSquare /></el-icon>
                <span style="margin-left: 4px">{{ t('settings.languageEnUS') }}</span>
              </el-radio-button>
              <el-radio-button label="ja-JP">
                <el-icon><ChatDotRound /></el-icon>
                <span style="margin-left: 4px">{{ t('settings.languageJaJP') }}</span>
              </el-radio-button>
            </el-radio-group>
          </div>
        </el-card>

        <el-card shadow="hover" class="settings-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" class="header-icon"><Monitor /></el-icon>
              <span>{{ t('settings.interface') }}</span>
            </div>
          </template>

          <div class="setting-section">
            <div class="setting-label">
              <div class="setting-title">{{ t('settings.sidebar') }}</div>
              <div class="setting-desc">调整侧边栏的默认显示方式</div>
            </div>
            <el-switch
              v-model="localSidebarCollapsed"
              :active-text="t('settings.sidebarCollapsed')"
              inline-prompt
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="hover" class="preview-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" class="header-icon"><View /></el-icon>
              <span>{{ t('settings.previewTitle') }}</span>
            </div>
          </template>
          <div class="preview-wrapper" :class="localTheme">
            <div class="preview-window">
              <div class="pw-sidebar">
                <div class="pw-logo"></div>
                <div class="pw-menu">
                  <div class="pw-menu-item active"></div>
                  <div class="pw-menu-item"></div>
                  <div class="pw-menu-item"></div>
                </div>
              </div>
              <div class="pw-main">
                <div class="pw-header">
                  <div class="pw-user"></div>
                </div>
                <div class="pw-content">
                  <div class="pw-card">
                    <div class="pw-title-line"></div>
                    <div class="pw-content-line"></div>
                    <div class="pw-content-line short"></div>
                    <div class="pw-btn"></div>
                  </div>
                  <div class="pw-card" style="margin-top: 12px">
                    <div class="pw-title-line"></div>
                    <div class="pw-grid">
                      <div class="pw-grid-item"></div>
                      <div class="pw-grid-item"></div>
                      <div class="pw-grid-item"></div>
                      <div class="pw-grid-item"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <div class="action-buttons">
          <el-button
            type="primary"
            size="large"
            :loading="saving"
            @click="handleSave"
            class="save-btn"
          >
            <el-icon><Check /></el-icon>
            <span>{{ t('settings.save') }}</span>
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            <span>{{ t('settings.reset') }}</span>
          </el-button>
        </div>

        <el-alert
          v-if="savedTip"
          :title="t('settings.savedTip')"
          type="success"
          :closable="false"
          show-icon
          class="success-alert"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Brush, Monitor, View, Check, RefreshRight,
  ChatDotRound, ChatLineSquare
} from '@element-plus/icons-vue'
import { usePreferencesStore } from '../../store/preferences'

const { t } = useI18n()
const preferencesStore = usePreferencesStore()

const saving = ref(false)
const savedTip = ref(false)

const localTheme = ref(preferencesStore.theme)
const localLanguage = ref(preferencesStore.language)
const localSidebarCollapsed = ref(preferencesStore.sidebarCollapsed)

watch(localTheme, (val) => {
  preferencesStore.setTheme(val)
})

watch(localLanguage, (val) => {
  preferencesStore.setLanguage(val)
})

watch(localSidebarCollapsed, (val) => {
  preferencesStore.setSidebarCollapsed(val)
})

const handleSave = async () => {
  saving.value = true
  savedTip.value = false
  try {
    await preferencesStore.updatePreferences({
      theme: localTheme.value,
      language: localLanguage.value,
      sidebarCollapsed: localSidebarCollapsed.value ? 1 : 0
    })
    savedTip.value = true
    ElMessage.success(t('common.saveSuccess'))
    setTimeout(() => {
      savedTip.value = false
    }, 3000)
  } catch (err) {
    ElMessage.error(err?.message || t('common.fail'))
  } finally {
    saving.value = false
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      t('settings.resetConfirmMessage'),
      t('settings.resetConfirmTitle'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await preferencesStore.resetToDefault()
    localTheme.value = preferencesStore.theme
    localLanguage.value = preferencesStore.language
    localSidebarCollapsed.value = preferencesStore.sidebarCollapsed
    ElMessage.success(t('settings.resetSuccess'))
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error(e?.message || t('common.fail'))
    }
  }
}

onMounted(async () => {
  if (!preferencesStore.loaded) {
    await preferencesStore.fetchPreferences()
    localTheme.value = preferencesStore.theme
    localLanguage.value = preferencesStore.language
    localSidebarCollapsed.value = preferencesStore.sidebarCollapsed
  }
})
</script>

<style scoped>
.settings-container {
  width: 100%;
}

.page-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.settings-card,
.preview-card {
  border-radius: 8px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-primary);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-icon {
  color: var(--color-primary);
}

.setting-section {
  padding: 8px 0;
}

.setting-label {
  margin-bottom: 16px;
}

.setting-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.theme-options {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.theme-option {
  cursor: pointer;
  border: 2px solid var(--border-primary);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s ease;
  background: var(--bg-card);
}

.theme-option:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}

.theme-option.active {
  border-color: var(--color-primary);
  background-color: var(--bg-hover);
}

.theme-preview {
  width: 200px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  margin-bottom: 12px;
  border: 1px solid var(--border-secondary);
}

.theme-light {
  background-color: #f5f7fa;
}

.theme-light .preview-sidebar {
  width: 50px;
  background-color: #304156;
}

.theme-light .preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.theme-light .preview-header {
  height: 20px;
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
}

.theme-light .preview-body {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.theme-light .preview-line {
  height: 8px;
  background-color: #e4e7ed;
  border-radius: 2px;
}

.theme-light .preview-line.short {
  width: 60%;
}

.theme-dark {
  background-color: #141414;
}

.theme-dark .preview-sidebar {
  width: 50px;
  background-color: #0f1419;
}

.theme-dark .preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.theme-dark .preview-header {
  height: 20px;
  background-color: #1d1e1f;
  border-bottom: 1px solid #3c3f41;
}

.theme-dark .preview-body {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.theme-dark .preview-line {
  height: 8px;
  background-color: #3c3f41;
  border-radius: 2px;
}

.theme-dark .preview-line.short {
  width: 60%;
}

.theme-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.no-label :deep(.el-radio__label) {
  display: none;
}

.language-group {
  display: flex;
  flex-wrap: wrap;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}

.save-btn {
  flex: 1;
}

.success-alert {
  margin-top: 16px;
}

.preview-wrapper {
  padding: 20px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.preview-wrapper.light {
  background-color: #f0f2f5;
}

.preview-wrapper.dark {
  background-color: #1a1a1a;
}

.preview-window {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  height: 360px;
}

.pw-sidebar {
  width: 80px;
  background: var(--bg-sidebar);
  padding: 16px 8px;
  transition: background-color 0.3s ease;
}

.pw-logo {
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  margin-bottom: 20px;
}

.pw-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pw-menu-item {
  height: 28px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.pw-menu-item.active {
  background: rgba(64, 158, 255, 0.3);
}

.pw-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

.preview-wrapper.light .pw-main {
  background: #f5f7fa;
}

.preview-wrapper.dark .pw-main {
  background: #141414;
}

.pw-header {
  height: 40px;
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-primary);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 12px;
  transition: all 0.3s ease;
}

.pw-user {
  width: 60px;
  height: 20px;
  background: var(--border-primary);
  border-radius: 10px;
}

.pw-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}

.pw-card {
  background: var(--bg-card);
  border-radius: 6px;
  padding: 12px;
  border: 1px solid var(--border-primary);
  transition: all 0.3s ease;
}

.pw-title-line {
  height: 14px;
  width: 50%;
  background: var(--text-secondary);
  opacity: 0.4;
  border-radius: 2px;
  margin-bottom: 10px;
}

.pw-content-line {
  height: 8px;
  background: var(--border-primary);
  border-radius: 2px;
  margin-bottom: 6px;
}

.pw-content-line.short {
  width: 70%;
}

.pw-btn {
  width: 70px;
  height: 24px;
  background: var(--color-primary);
  border-radius: 4px;
  margin-top: 10px;
  opacity: 0.9;
}

.pw-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.pw-grid-item {
  height: 40px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  border: 1px solid var(--border-secondary);
}

@media screen and (max-width: 768px) {
  .theme-option {
    width: calc(50% - 10px);
  }

  .theme-preview {
    width: 100%;
    height: 100px;
  }

  .preview-window {
    height: 280px;
  }
}
</style>
