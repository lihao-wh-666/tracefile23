import request from '../utils/request'

export function getSystemConfigPage(params) {
  return request.get('/system-config/page', { params })
}

export function getSystemConfigList() {
  return request.get('/system-config/list')
}

export function getSystemConfigById(id) {
  return request.get(`/system-config/${id}`)
}

export function getSystemConfigByKey(configKey) {
  return request.get(`/system-config/key/${configKey}`)
}

export function getSystemConfigValueByKey(configKey) {
  return request.get(`/system-config/value/${configKey}`)
}

export function addSystemConfig(data) {
  return request.post('/system-config', data)
}

export function updateSystemConfig(data) {
  return request.put('/system-config', data)
}

export function updateSystemConfigByKey(configKey, configValue) {
  return request.put('/system-config/update-by-key', null, { params: { configKey, configValue } })
}

export function deleteSystemConfig(id) {
  return request.delete(`/system-config/${id}`)
}

export function refreshSystemConfigCache() {
  return request.post('/system-config/refresh-cache')
}
