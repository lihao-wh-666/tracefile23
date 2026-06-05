import request from '../utils/request'

export function getPaperPage(params) {
  return request.get('/paper/page', { params })
}

export function getPaperDetail(id) {
  return request.get(`/paper/${id}`)
}

export function addPaper(data) {
  return request.post('/paper', data)
}

export function updatePaper(id, data) {
  return request.put(`/paper/${id}`, data)
}

export function deletePaper(id) {
  return request.delete(`/paper/${id}`)
}

export function publishPaper(id) {
  return request.put(`/paper/${id}/publish`)
}
