import request from '../utils/request'

export function getExamPage(params) {
  return request.get('/exam/page', { params })
}

export function getExamDetail(id) {
  return request.get(`/exam/${id}`)
}

export function addExam(data) {
  return request.post('/exam', data)
}

export function updateExam(id, data) {
  return request.put(`/exam/${id}`, data)
}

export function deleteExam(id) {
  return request.delete(`/exam/${id}`)
}
