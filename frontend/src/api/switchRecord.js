import request from '../utils/request'

export function recordSwitch(data) {
  return request.post('/switch-record/record', data)
}

export function incrementWarningCount(recordId) {
  return request.post('/switch-record/warning', null, { params: { recordId } })
}

export function getSwitchRecordList(recordId) {
  return request.get(`/switch-record/${recordId}/list`)
}

export function getSwitchStatistics(recordId) {
  return request.get(`/switch-record/${recordId}/statistics`)
}

export function getSwitchRecordPage(params) {
  return request.get('/switch-record/page', { params })
}
