import request from '../utils/request'

export function getRecordPage(params) {
  return request.get('/record/page', { params })
}

export function getRecordDetail(id) {
  return request.get(`/record/${id}`)
}

export function startExam(examId) {
  return request.post(`/record/start/${examId}`)
}

export function submitExam(data) {
  return request.post('/record/submit', data)
}

export function getScoreStats(examId) {
  return request.get(`/record/stats/${examId}`)
}

export function getRecordAnswers(recordId) {
  return request.get(`/record/${recordId}/answers`)
}

export function getMyStat() {
  return request.get('/record/my/stat')
}

export function getMyRecordList(params) {
  return request.get('/record/my/list', { params })
}

export function getMyWrongQuestions(params) {
  return request.get('/record/my/wrong-questions', { params })
}
