import { defineStore } from 'pinia'
import { getUserPreference, saveUserPreference, resetUserPreference } from '../api/userPreference'
import { setLocale, getLocale } from '../locales'

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

export const usePreferencesStore = defineStore('preferences', {
  state: () => ({
    preferences: { ...DEFAULT_PREFERENCES },
    loaded: false
  }),

  getters: {
    theme: (state) => state.preferences.theme || 'light',
    language: (state) => state.preferences.language || 'zh-CN',
    sidebarCollapsed: (state) => {
      const stored = localStorage.getItem('preferences_sidebar_collapsed')
      if (stored !== null) {
        return stored === '1'
      }
      return state.preferences.sidebarCollapsed === 1
    }
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
        applySidebar(prefs.sidebarCollapsed === 1)
        this.preferences.sidebarCollapsed = prefs.sidebarCollapsed
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
      if (savedSidebar) localPrefs.sidebarCollapsed = savedSidebar === '1' ? 1 : 0

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
      const toSave = { ...this.preferences, ...newPrefs }
      this.applyPreferences(toSave)

      try {
        await saveUserPreference({
          theme: toSave.theme,
          language: toSave.language,
          sidebarCollapsed: toSave.sidebarCollapsed,
          extraConfig: toSave.extraConfig
        })
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
      applySidebar(collapsed)
      this.preferences.sidebarCollapsed = collapsed ? 1 : 0
    },

    async resetToDefault() {
      try {
        await resetUserPreference()
        this.applyPreferences({ ...DEFAULT_PREFERENCES })
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
