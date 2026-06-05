import axios from 'axios'
import errorHandler from './errorHandler'
import { ERROR_CODE } from './errorCode'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json'
    return config
  },
  error => {
    return errorHandler.handleError(error, {
      showToast: true,
      throwError: true
    })
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== ERROR_CODE.SUCCESS) {
      return errorHandler.handleError(res, {
        showToast: true,
        throwError: true
      })
    }
    return res
  },
  error => {
    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      return errorHandler.handleError({
        code: ERROR_CODE.SYSTEM_ERROR,
        message: '请求超时，请检查网络连接'
      }, { showToast: true, throwError: true })
    }

    if (!error.response) {
      return errorHandler.handleError({
        code: ERROR_CODE.SYSTEM_ERROR,
        message: '网络异常，请检查网络连接'
      }, { showToast: true, throwError: true })
    }

    const status = error.response.status
    const errorData = error.response.data || {}

    switch (status) {
      case 400:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.BAD_REQUEST,
          message: errorData.msg || '请求参数错误'
        }, { showToast: true, throwError: true })
      case 401:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.UNAUTHORIZED,
          message: errorData.msg || '未授权，请先登录'
        }, { showToast: true, throwError: true })
      case 403:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.FORBIDDEN,
          message: errorData.msg || '无权限访问'
        }, { showToast: true, throwError: true })
      case 404:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.NOT_FOUND,
          message: errorData.msg || '资源不存在'
        }, { showToast: true, throwError: true })
      case 500:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.SYSTEM_ERROR,
          message: errorData.msg || '服务器内部错误'
        }, { showToast: true, throwError: true })
      default:
        return errorHandler.handleError({
          code: errorData.code || ERROR_CODE.FAIL,
          message: errorData.msg || `请求失败 (${status})`
        }, { showToast: true, throwError: true })
    }
  }
)

export default request
