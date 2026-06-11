import { useUserStore } from '../store/user'
import { getSystemConfigValueByKey } from '../api/systemConfig'
import { ElMessageBox } from 'element-plus'

let timeoutTimer = null
let warningTimer = null
let lastActivityTime = Date.now()
let isWarningShown = false
let sessionTimeoutMinutes = 30
const WARNING_BEFORE_MINUTES = 1

const events = [
  'mousedown',
  'mousemove',
  'keypress',
  'scroll',
  'touchstart',
  'click'
]

function updateActivityTime() {
  lastActivityTime = Date.now()
  isWarningShown = false
}

function setupActivityListeners() {
  events.forEach(event => {
    document.addEventListener(event, updateActivityTime, { passive: true })
  })
}

function removeActivityListeners() {
  events.forEach(event => {
    document.removeEventListener(event, updateActivityTime)
  })
}

async function fetchTimeoutConfig() {
  try {
    const res = await getSystemConfigValueByKey('login.timeout.minutes')
    if (res && res.data) {
      sessionTimeoutMinutes = parseInt(res.data) || 30
    }
  } catch (e) {
    console.error('Failed to fetch session timeout config:', e)
    sessionTimeoutMinutes = 30
  }
}

function checkSessionTimeout() {
  const userStore = useUserStore()
  if (!userStore.token) {
    stopSessionTimeoutCheck()
    return
  }

  const now = Date.now()
  const elapsedMinutes = (now - lastActivityTime) / (1000 * 60)
  const timeoutMs = sessionTimeoutMinutes * 60 * 1000
  const warningMs = (sessionTimeoutMinutes - WARNING_BEFORE_MINUTES) * 60 * 1000

  if (elapsedMinutes >= sessionTimeoutMinutes) {
    handleTimeout()
    return
  }

  if (elapsedMinutes >= sessionTimeoutMinutes - WARNING_BEFORE_MINUTES && !isWarningShown) {
    showWarning()
  }
}

function showWarning() {
  isWarningShown = true
  const remainingSeconds = Math.max(0, sessionTimeoutMinutes * 60 - Math.floor((Date.now() - lastActivityTime) / 1000))
  
  ElMessageBox.confirm(
    `您的登录状态将在 ${Math.ceil(remainingSeconds / 60)} 分钟后过期，是否继续保持登录？`,
    '登录即将过期',
    {
      confirmButtonText: '保持登录',
      cancelButtonText: '退出登录',
      type: 'warning',
      showClose: false,
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  ).then(() => {
    updateActivityTime()
    isWarningShown = false
  }).catch(() => {
    handleTimeout()
  })
}

function handleTimeout() {
  stopSessionTimeoutCheck()
  const userStore = useUserStore()
  ElMessageBox.alert(
    '您的登录状态已过期，请重新登录。',
    '登录已过期',
    {
      confirmButtonText: '重新登录',
      type: 'error',
      showClose: false,
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  ).then(() => {
    userStore.logout()
  }).catch(() => {
    userStore.logout()
  })
}

export function startSessionTimeoutCheck() {
  stopSessionTimeoutCheck()
  
  lastActivityTime = Date.now()
  isWarningShown = false
  
  setupActivityListeners()
  fetchTimeoutConfig()
  
  timeoutTimer = setInterval(checkSessionTimeout, 10000)
}

export function stopSessionTimeoutCheck() {
  if (timeoutTimer) {
    clearInterval(timeoutTimer)
    timeoutTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
  removeActivityListeners()
  isWarningShown = false
}

export function resetSessionTimeout() {
  lastActivityTime = Date.now()
  isWarningShown = false
}

export function updateSessionTimeoutConfig(minutes) {
  sessionTimeoutMinutes = minutes || 30
  lastActivityTime = Date.now()
  isWarningShown = false
}
