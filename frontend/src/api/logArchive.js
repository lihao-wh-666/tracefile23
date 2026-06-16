import request from '../utils/request'

export function getStoragePolicy() {
  return request.get('/log-archive/policy')
}

export function updateStoragePolicy(data) {
  return request.put('/log-archive/policy', data)
}

export function getStorageStatistics() {
  return request.get('/log-archive/statistics')
}

export function createArchiveTask(params) {
  return request.post('/log-archive/task', null, { params })
}

export function executeArchiveTask(batchId) {
  return request.post(`/log-archive/task/${batchId}/execute`)
}

export function listArchiveTasks(params) {
  return request.get('/log-archive/task', { params })
}

export function getArchiveTaskDetail(batchId) {
  return request.get(`/log-archive/task/${batchId}`)
}

export function verifyArchiveIntegrity(batchId) {
  return request.get(`/log-archive/verify/${batchId}`)
}

export function traceLogAcrossLevels(params) {
  return request.get('/log-archive/trace', { params })
}

export function exportArchivedLogs(batchId) {
  return request.get(`/log-archive/export/${batchId}`, {
    responseType: 'blob'
  })
}

export function manualTriggerAutoArchive() {
  return request.post('/log-archive/auto-run')
}
