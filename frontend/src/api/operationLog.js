import request from '../utils/request'

export function getOperationLogPage(params) {
  return request.get('/operation-log/page', { params })
}

export function getOperationLogDetail(id) {
  return request.get(`/operation-log/${id}`)
}

export function getOperationLogStatistics(params) {
  return request.get('/operation-log/statistics', { params })
}

export function verifyOperationLogIntegrity(params) {
  return request.get('/operation-log/verify-integrity', { params })
}

export function exportOperationLog(params) {
  return request.get('/operation-log/export', {
    params,
    responseType: 'blob'
  })
}
