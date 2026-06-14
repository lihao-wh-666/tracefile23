import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'
import jaJP from './ja-JP'

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP
}

const getDefaultLocale = () => {
  const saved = localStorage.getItem('preferences_language')
  if (saved && messages[saved]) {
    return saved
  }
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: getDefaultLocale(),
  fallbackLocale: 'zh-CN',
  messages
})

export const setLocale = (locale) => {
  if (messages[locale]) {
    i18n.global.locale.value = locale
    localStorage.setItem('preferences_language', locale)
  }
}

export const getLocale = () => {
  return i18n.global.locale.value
}

export default i18n
