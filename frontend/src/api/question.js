import request from '../utils/request'

export function getQuestionPage(params) {
  return request.get('/question/page', { params })
}

export function getQuestionDetail(id) {
  return request.get(`/question/${id}`)
}

export function addQuestion(data) {
  return request.post('/question', data)
}

export function updateQuestion(id, data) {
  return request.put(`/question/${id}`, data)
}

export function deleteQuestion(id) {
  return request.delete(`/question/${id}`)
}
