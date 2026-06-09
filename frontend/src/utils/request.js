import axios from 'axios'
import errorHandler from './errorHandler'
import { ERROR_CODE } from './errorCode'
import logger from './logger'

function generateTraceId() {
  return 'web-' + Date.now().toString(36) + Math.random().toString(36).substr(2, 8)
}

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
    const traceId = generateTraceId()
    config.headers['X-Trace-Id'] = traceId
    config.traceId = traceId

    const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData
    if (!isFormData) {
      config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json'
    } else {
      delete config.headers['Content-Type']
    }

    logger.debug('API请求开始', {
      type: 'API_REQUEST',
      url: config.url,
      method: config.method?.toUpperCase(),
      traceId: config.traceId
    })

    return config
  },
  error => {
    logger.error('API请求配置错误', {
      type: 'API_REQUEST_ERROR',
      message: error.message
    })
    return errorHandler.handleError(error, {
      showToast: true,
      throwError: true
    })
  }
)

request.interceptors.response.use(
  response => {
    const traceId = response.headers['x-trace-id'] || response.config.traceId

    logger.debug('API请求成功', {
      type: 'API_RESPONSE',
      url: response.config.url,
      method: response.config.method?.toUpperCase(),
      traceId,
      status: response.status
    })

    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== ERROR_CODE.SUCCESS) {
      return errorHandler.handleError({
        ...res,
        traceId: res.traceId || traceId
      }, {
        showToast: true,
        throwError: true
      })
    }
    return res
  },
  error => {
    const traceId = error.config?.traceId || error.response?.headers?.['x-trace-id']

    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      return errorHandler.handleError({
        code: ERROR_CODE.SYSTEM_ERROR,
        message: '请求超时，请检查网络连接',
        traceId
      }, { showToast: true, throwError: true })
    }

    if (!error.response) {
      return errorHandler.handleError({
        code: ERROR_CODE.SYSTEM_ERROR,
        message: '网络异常，请检查网络连接',
        traceId
      }, { showToast: true, throwError: true })
    }

    const status = error.response.status
    const errorData = error.response.data || {}

    const errorMap = {
      400: { code: errorData.code || ERROR_CODE.BAD_REQUEST, message: errorData.msg || '请求参数错误' },
      401: { code: errorData.code || ERROR_CODE.UNAUTHORIZED, message: errorData.msg || '未授权，请先登录' },
      403: { code: errorData.code || ERROR_CODE.FORBIDDEN, message: errorData.msg || '无权限访问' },
      404: { code: errorData.code || ERROR_CODE.NOT_FOUND, message: errorData.msg || '资源不存在' },
      500: { code: errorData.code || ERROR_CODE.SYSTEM_ERROR, message: errorData.msg || '服务器内部错误' }
    }

    const mapped = errorMap[status] || {
      code: errorData.code || ERROR_CODE.FAIL,
      message: errorData.msg || `请求失败 (${status})`
    }

    return errorHandler.handleError({
      code: mapped.code,
      message: mapped.message,
      detail: errorData.detail,
      traceId: errorData.traceId || traceId,
      fieldErrors: errorData.data?.fieldErrors
    }, { showToast: true, throwError: true })
  }
)

export default request
