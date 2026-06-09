import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import Router from './router'
import App from './App.vue'
import './styles/responsive.css'
import errorPlugin from './utils/errorPlugin'

const app = createApp(App)
app.use(ElementPlus)
app.use(createPinia())
app.use(Router)
app.use(errorPlugin)
app.mount('#app')
