import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import Router from './router'
import App from './App.vue'
import i18n from './locales'
import './styles/responsive.css'
import './styles/theme.css'
import errorPlugin from './utils/errorPlugin'
import { usePreferencesStore } from './store/preferences'

const pinia = createPinia()
const app = createApp(App)
app.use(ElementPlus)
app.use(pinia)

const preferencesStore = usePreferencesStore()
preferencesStore.initLocalPreferences()

app.use(Router)
app.use(i18n)
app.use(errorPlugin)
app.mount('#app')
