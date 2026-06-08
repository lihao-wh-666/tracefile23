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

export function importQuestions(file, subjectId) {
  const formData = new FormData()
  formData.append('file', file)
  if (subjectId) {
    formData.append('subjectId', subjectId)
  }
  return request.post('/question/import', formData)
}

export function downloadTemplate() {
  return request.get('/question/template', {
    responseType: 'blob'
  })
}
