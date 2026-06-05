import request from '../utils/request'

export function getSubjectPage(params) {
  return request.get('/subject/page', { params })
}

export function getSubjectList() {
  return request.get('/subject/list')
}

export function addSubject(data) {
  return request.post('/subject', data)
}

export function updateSubject(data) {
  return request.put('/subject', data)
}

export function deleteSubject(id) {
  return request.delete(`/subject/${id}`)
}
