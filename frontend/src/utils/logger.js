import { getErrorCategory, isSystemError, isAuthError } from './errorCode'

const LOG_LEVEL = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3,
  FATAL: 4
}

const currentLevel = LOG_LEVEL.INFO

const MAX_LOG_SIZE = 100
const LOG_STORAGE_KEY = 'app_error_logs'

class Logger {
  constructor() {
    this.logs = this.loadLogs()
  }

  loadLogs() {
    try {
      const saved = localStorage.getItem(LOG_STORAGE_KEY)
      return saved ? JSON.parse(saved) : []
    } catch (e) {
      return []
    }
  }

  saveLogs() {
    try {
      if (this.logs.length > MAX_LOG_SIZE) {
        this.logs = this.logs.slice(-MAX_LOG_SIZE)
      }
      localStorage.setItem(LOG_STORAGE_KEY, JSON.stringify(this.logs))
    } catch (e) {
      console.warn('Failed to save logs:', e)
    }
  }

  formatLog(level, message, extra = {}) {
    return {
      timestamp: new Date().toISOString(),
      level,
      message,
      userId: localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')).id : 'anonymous',
      page: window.location.href,
      userAgent: navigator.userAgent,
      ...extra
    }
  }

  addLog(log) {
    this.logs.push(log)
    this.saveLogs()
  }

  debug(message, extra) {
    if (currentLevel <= LOG_LEVEL.DEBUG) {
      const log = this.formatLog('DEBUG', message, extra)
      console.debug('[DEBUG]', message, extra)
      this.addLog(log)
    }
  }

  info(message, extra) {
    if (currentLevel <= LOG_LEVEL.INFO) {
      const log = this.formatLog('INFO', message, extra)
      console.info('[INFO]', message, extra)
      this.addLog(log)
    }
  }

  warn(message, extra) {
    if (currentLevel <= LOG_LEVEL.WARN) {
      const log = this.formatLog('WARN', message, extra)
      console.warn('[WARN]', message, extra)
      this.addLog(log)
    }
  }

  error(message, extra) {
    if (currentLevel <= LOG_LEVEL.ERROR) {
      const log = this.formatLog('ERROR', message, extra)
      console.error('[ERROR]', message, extra)
      this.addLog(log)
    }
  }

  fatal(message, extra) {
    if (currentLevel <= LOG_LEVEL.FATAL) {
      const log = this.formatLog('FATAL', message, extra)
      console.error('[FATAL]', message, extra)
      this.addLog(log)
    }
  }

  logApiError(error) {
    const extra = {
      type: 'API_ERROR',
      code: error.code,
      detail: error.detail,
      traceId: error.traceId,
      url: error.url,
      method: error.method
    }
    const category = getErrorCategory(error.code)
    if (isSystemError(error.code)) {
      this.error(`API系统错误: ${error.message}`, extra)
    } else if (isAuthError(error.code)) {
      this.warn(`API认证错误: ${error.message}`, extra)
    } else {
      this.warn(`API错误: ${error.message}`, extra)
    }
  }

  logRenderError(error, vm, info) {
    this.fatal('页面渲染错误', {
      type: 'RENDER_ERROR',
      error: error.message,
      stack: error.stack,
      component: vm?.$options?.name,
      info
    })
  }

  logResourceError(error) {
    this.error('资源加载错误', {
      type: 'RESOURCE_ERROR',
      src: error.target?.src,
      tag: error.target?.tagName,
      resourceUrl: error.target?.href
    })
  }

  logPromiseError(reason) {
    this.error('未捕获的Promise错误', {
      type: 'PROMISE_ERROR',
      reason: reason?.message || reason,
      stack: reason?.stack
    })
  }

  logScriptError(message, source, lineno, colno, error) {
    this.fatal('脚本错误', {
      type: 'SCRIPT_ERROR',
      message,
      source,
      lineno,
      colno,
      stack: error?.stack
    })
  }

  logRouterError(error, to, from) {
    this.error('路由错误', {
      type: 'ROUTER_ERROR',
      message: error.message,
      from: from?.path,
      to: to?.path
    })
  }

  getLogs(level = null) {
    if (level) {
      return this.logs.filter(log => log.level === level)
    }
    return [...this.logs]
  }

  clearLogs() {
    this.logs = []
    localStorage.removeItem(LOG_STORAGE_KEY)
  }

  exportLogs() {
    const data = JSON.stringify(this.logs, null, 2)
    const blob = new Blob([data], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `error-logs-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
  }
}

export default new Logger()
