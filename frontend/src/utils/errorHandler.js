import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { ERROR_CODE, getErrorInfo, getErrorMessage, isAuthError, isSystemError, isParamError } from './errorCode'
import router from '../router'
import logger from './logger'

class ErrorHandler {
  constructor() {
    this.toastDuration = 3000
    this.notificationDuration = 5000
  }

  handleError(error, options = {}) {
    const {
      showToast = true,
      showNotification = false,
      showDialog = false,
      showDetail = false,
      redirectOnAuth = true,
      throwError = true,
      logError = true
    } = options

    const errorInfo = this.parseError(error)

    if (logError) {
      logger.logApiError(errorInfo)
    }

    if (this.handleAuthError(errorInfo, redirectOnAuth)) {
      return
    }

    const displayMessage = showDetail && errorInfo.detail
      ? `${errorInfo.message} (${errorInfo.detail})`
      : errorInfo.message

    if (showToast) {
      this.showErrorToast(displayMessage)
    }

    if (showNotification) {
      this.showErrorNotification(displayMessage)
    }

    if (showDialog) {
      this.showErrorDialog(displayMessage, errorInfo.detail)
    }

    if (throwError) {
      return Promise.reject(errorInfo)
    }

    return errorInfo
  }

  parseError(error) {
    if (!error) {
      return {
        code: ERROR_CODE.SYSTEM_ERROR,
        message: '未知错误',
        detail: '发生了未知错误，请稍后重试',
        category: 'system',
        traceId: null,
        timestamp: new Date().toISOString()
      }
    }

    if (error.response) {
      const data = error.response.data || {}
      const info = getErrorInfo(data.code || error.response.status)
      return {
        code: data.code || error.response.status,
        message: data.msg || data.message || info.message,
        detail: data.detail || info.detail,
        category: data.category || info.category,
        traceId: data.traceId || error.response.headers?.['x-trace-id'],
        timestamp: data.timestamp || new Date().toISOString(),
        url: error.config?.url,
        method: error.config?.method?.toUpperCase()
      }
    }

    if (error.code && error.message) {
      const info = getErrorInfo(error.code)
      return {
        code: error.code,
        message: error.message || info.message,
        detail: error.detail || info.detail,
        category: error.category || info.category,
        traceId: error.traceId,
        timestamp: error.timestamp || new Date().toISOString(),
        fieldErrors: error.fieldErrors
      }
    }

    if (error.message) {
      return {
        code: ERROR_CODE.FAIL,
        message: error.message,
        detail: null,
        category: 'system',
        traceId: null,
        timestamp: new Date().toISOString()
      }
    }

    if (typeof error === 'string') {
      return {
        code: ERROR_CODE.FAIL,
        message: error,
        detail: null,
        category: 'system',
        traceId: null,
        timestamp: new Date().toISOString()
      }
    }

    return {
      code: ERROR_CODE.SYSTEM_ERROR,
      message: '系统异常',
      detail: '系统内部错误，请联系管理员',
      category: 'system',
      traceId: null,
      timestamp: new Date().toISOString()
    }
  }

  handleAuthError(errorInfo, redirect = true) {
    if (isAuthError(errorInfo.code)) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (redirect) {
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
        this.showWarningToast('登录已过期，请重新登录')
      }
      return true
    }
    return false
  }

  handleFieldErrors(fieldErrors) {
    if (!fieldErrors) return null
    return Object.entries(fieldErrors).map(([field, message]) => ({
      field,
      message
    }))
  }

  showErrorToast(message) {
    ElMessage.error({
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showSuccessToast(message) {
    ElMessage.success({
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showWarningToast(message) {
    ElMessage.warning({
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showInfoToast(message) {
    ElMessage.info({
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showErrorNotification(message, title = '错误') {
    ElNotification.error({
      title: title,
      message: message,
      duration: this.notificationDuration,
      showClose: true
    })
  }

  showSuccessNotification(message, title = '成功') {
    ElNotification.success({
      title: title,
      message: message,
      duration: this.notificationDuration,
      showClose: true
    })
  }

  showWarningNotification(message, title = '警告') {
    ElNotification.warning({
      title: title,
      message: message,
      duration: this.notificationDuration,
      showClose: true
    })
  }

  showErrorDialog(message, detail = null, title = '错误提示') {
    const content = detail ? `${message}\n\n详细信息: ${detail}` : message
    return ElMessageBox.alert(content, title, {
      confirmButtonText: '确定',
      type: 'error',
      dangerouslyUseHTMLString: false
    })
  }

  showConfirmDialog(message, title = '提示', options = {}) {
    return ElMessageBox.confirm(message, title, {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      type: options.type || 'warning',
      distinguishCancelAndClose: options.distinguishCancelAndClose || false
    })
  }

  showInputDialog(message, title = '请输入', options = {}) {
    return ElMessageBox.prompt(message, title, {
      confirmButtonText: options.confirmText || '确定',
      cancelButtonText: options.cancelText || '取消',
      inputType: options.inputType || 'text',
      inputPattern: options.inputPattern || null,
      inputErrorMessage: options.inputErrorMessage || '输入格式不正确'
    })
  }

  handleResponse(response) {
    if (!response) {
      return this.handleError('响应数据为空', { throwError: false })
    }

    const { code, msg, data, traceId, timestamp, detail } = response

    if (code === ERROR_CODE.SUCCESS) {
      return {
        success: true,
        code,
        message: msg,
        data,
        traceId,
        timestamp
      }
    }

    const errorInfo = {
      code,
      message: msg || getErrorMessage(code),
      detail,
      traceId,
      timestamp,
      fieldErrors: data?.fieldErrors
    }

    return this.handleError(errorInfo)
  }

  handleGlobalError(error, vm, info) {
    logger.logRenderError(error, vm, info)
    this.showErrorNotification(
      '页面渲染发生错误，请刷新页面或联系管理员',
      '系统错误'
    )
  }

  handleResourceError(event) {
    logger.logResourceError(event)
    const target = event.target
    if (target?.tagName === 'IMG') {
      this.showWarningToast('图片加载失败')
    } else if (target?.tagName === 'SCRIPT') {
      this.showErrorNotification(
        '脚本资源加载失败，部分功能可能无法正常使用',
        '资源加载错误'
      )
    } else if (target?.tagName === 'LINK') {
      this.showWarningToast('样式资源加载失败，页面显示可能异常')
    }
  }

  handleUnhandledRejection(event) {
    logger.logPromiseError(event.reason)
    this.showErrorToast('操作失败，请稍后重试')
  }

  handleScriptError(message, source, lineno, colno, error) {
    logger.logScriptError(message, source, lineno, colno, error)
    if (!message.includes('Script error')) {
      this.showErrorNotification(
        '发生脚本错误，部分功能可能异常',
        '系统错误'
      )
    }
  }

  handleRouterError(error, to, from) {
    logger.logRouterError(error, to, from)
    if (error?.name === 'NavigationDuplicated') {
      return
    }
    this.showErrorToast('页面导航失败')
  }

  handleNetworkError(error) {
    this.showErrorNotification(
      '网络连接异常，请检查网络设置',
      '网络错误'
    )
    logger.error('网络错误', {
      type: 'NETWORK_ERROR',
      message: error.message,
      code: error.code
    })
  }

  shouldShowDetail(code) {
    return isSystemError(code) || isParamError(code)
  }
}

export default new ErrorHandler()
