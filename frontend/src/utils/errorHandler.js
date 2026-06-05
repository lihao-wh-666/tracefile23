import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { ERROR_CODE, getErrorMessage } from './errorCode'
import router from '../router'

class ErrorHandler {
  constructor() {
    this.toastDuration = 3000
  }

  handleError(error, options = {}) {
    const {
      showToast = true,
      showNotification = false,
      showDialog = false,
      throwError = true
    } = options

    const errorInfo = this.parseError(error)

    if (this.handleAuthError(errorInfo)) {
      return
    }

    if (showToast) {
      this.showErrorToast(errorInfo.message)
    }

    if (showNotification) {
      this.showErrorNotification(errorInfo.message)
    }

    if (showDialog) {
      this.showErrorDialog(errorInfo.message)
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
        message: '未知错误'
      }
    }

    if (error.response) {
      const data = error.response.data || {}
      return {
        code: data.code || error.response.status,
        message: data.msg || data.message || getErrorMessage(error.response.status)
      }
    }

    if (error.code && error.message) {
      return error
    }

    if (error.message) {
      return {
        code: ERROR_CODE.FAIL,
        message: error.message
      }
    }

    if (typeof error === 'string') {
      return {
        code: ERROR_CODE.FAIL,
        message: error
      }
    }

    return {
      code: ERROR_CODE.SYSTEM_ERROR,
      message: '系统异常'
    }
  }

  handleAuthError(errorInfo) {
    const authCodes = [
      ERROR_CODE.UNAUTHORIZED,
      ERROR_CODE.TOKEN_INVALID,
      ERROR_CODE.TOKEN_EXPIRED,
      ERROR_CODE.TOKEN_EMPTY
    ]

    if (authCodes.includes(errorInfo.code)) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      this.showErrorToast('登录已过期，请重新登录')
      return true
    }

    return false
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

  showErrorNotification(message, title = '错误') {
    ElNotification.error({
      title: title,
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showSuccessNotification(message, title = '成功') {
    ElNotification.success({
      title: title,
      message: message,
      duration: this.toastDuration,
      showClose: true
    })
  }

  showErrorDialog(message, title = '错误提示') {
    return ElMessageBox.alert(message, title, {
      confirmButtonText: '确定',
      type: 'error'
    })
  }

  showConfirmDialog(message, title = '提示') {
    return ElMessageBox.confirm(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  }

  handleResponse(response) {
    if (!response) {
      return this.handleError('响应数据为空', { throwError: false })
    }

    const { code, msg, data } = response

    if (code === ERROR_CODE.SUCCESS) {
      return {
        success: true,
        code,
        message: msg,
        data
      }
    }

    const errorInfo = {
      code,
      message: msg || getErrorMessage(code)
    }

    return this.handleError(errorInfo)
  }
}

export default new ErrorHandler()
