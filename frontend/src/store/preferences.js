import { defineStore } from 'pinia'
import { getUserPreference, saveUserPreference } from '../api/userPreference'
import { setLocale } from '../locales'

const DEFAULT_PREFERENCES = {
  theme: 'light',
  language: 'zh-CN',
  sidebarCollapsed: 0,
  extraConfig: null
}

const applyTheme = (theme) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
  localStorage.setItem('preferences_theme', theme)
}

const applySidebar = (collapsed) => {
  localStorage.setItem('preferences_sidebar_collapsed', collapsed ? '1' : '0')
}

const normalizeSidebar = (val) => {
  if (typeof val === 'boolean') return val ? 1 : 0
  if (val === '1' || val === 'true') return 1
  if (val === '0' || val === 'false') return 0
  return val ? 1 : 0
}

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    preferences: { ...DEFAULT_PREFERENCES },
    loaded: false
  }),

  getters: {
    theme: (state) => state.preferences.theme || 'light',
    language: (state) => state.preferences.language || 'zh-CN',
    sidebarCollapsed: (state) => state.preferences.sidebarCollapsed === 1
  },

  actions: {
    applyPreferences(prefs) {
      if (prefs.theme) {
        applyTheme(prefs.theme)
        this.preferences.theme = prefs.theme
      }
      if (prefs.language) {
        setLocale(prefs.language)
        this.preferences.language = prefs.language
      }
      if (prefs.sidebarCollapsed !== undefined && prefs.sidebarCollapsed !== null) {
        const normalized = normalizeSidebar(prefs.sidebarCollapsed)
        applySidebar(normalized === 1)
        this.preferences.sidebarCollapsed = normalized
      }
      if (prefs.extraConfig !== undefined) {
        this.preferences.extraConfig = prefs.extraConfig
      }
    },

    initLocalPreferences() {
      const savedTheme = localStorage.getItem('preferences_theme')
      const savedLanguage = localStorage.getItem('preferences_language')
      const savedSidebar = localStorage.getItem('preferences_sidebar_collapsed')

      const localPrefs = { ...DEFAULT_PREFERENCES }
      if (savedTheme) localPrefs.theme = savedTheme
      if (savedLanguage) localPrefs.language = savedLanguage
      if (savedSidebar !== null) localPrefs.sidebarCollapsed = savedSidebar === '1' ? 1 : 0

      this.applyPreferences(localPrefs)
      return localPrefs
    },

    async fetchPreferences() {
      try {
        this.initLocalPreferences()
        const res = await getUserPreference()
        if (res.data) {
          this.applyPreferences(res.data)
        }
        this.loaded = true
        return this.preferences
      } catch (err) {
        console.error('Failed to fetch preferences:', err)
        this.loaded = true
        return this.preferences
      }
    },

    async updatePreferences(newPrefs) {
      const merged = { ...this.preferences, ...newPrefs }
      const toSave = {
        theme: merged.theme,
        language: merged.language,
        sidebarCollapsed: normalizeSidebar(merged.sidebarCollapsed),
        extraConfig: merged.extraConfig
      }
      this.applyPreferences(toSave)

      try {
        await saveUserPreference(toSave)
        return true
      } catch (err) {
        console.error('Failed to save preferences:', err)
        throw err
      }
    },

    setTheme(theme) {
      applyTheme(theme)
      this.preferences.theme = theme
    },

    setLanguage(language) {
      setLocale(language)
      this.preferences.language = language
    },

    setSidebarCollapsed(collapsed) {
      const normalized = normalizeSidebar(collapsed)
      applySidebar(normalized === 1)
      this.preferences.sidebarCollapsed = normalized
    },

    async resetToDefault() {
      const defaults = { ...DEFAULT_PREFERENCES }
      this.applyPreferences(defaults)
      try {
        await saveUserPreference(defaults)
        return true
      } catch (err) {
        console.error('Failed to reset preferences:', err)
        throw err
      }
    },

    clearOnLogout() {
      localStorage.removeItem('preferences_theme')
      localStorage.removeItem('preferences_language')
      localStorage.removeItem('preferences_sidebar_collapsed')
      this.preferences = { ...DEFAULT_PREFERENCES }
      this.loaded = false
    }
  }
})
