import request from '../utils/request'

export function getRecordPage(params) {
  return request.get('/record/page', { params })
}

export function getRecordDetail(id) {
  return request.get(`/record/${id}`)
}

export function getCurrentExam() {
  return request.get('/record/current')
}

export function startExam(examId) {
  return request.post(`/record/start/${examId}`)
}

export function submitExam(data) {
  return request.post('/record/submit', data)
}

export function saveAnswer(data) {
  return request.post('/record/save-answer', data)
}

export function saveAnswers(data) {
  return request.post('/record/save-answers', data)
}

export function pauseExam(data) {
  return request.post('/record/pause', data)
}

export function resumeExam(data) {
  return request.post('/record/resume', data)
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

export function exportExcel(examId) {
  const params = {}
  if (examId) params.examId = examId
  return request.get('/record/export/excel', { params, responseType: 'blob' })
}

export function exportCsv(examId) {
  const params = {}
  if (examId) params.examId = examId
  return request.get('/record/export/csv', { params, responseType: 'blob' })
}

export function exportPdf(examId) {
  const params = {}
  if (examId) params.examId = examId
  return request.get('/record/export/pdf', { params, responseType: 'blob' })
}
