import request from '../utils/request'

export function getVideoCategoryPage(params) {
  return request.get('/video-category/page', { params })
}

export function getVideoCategoryList() {
  return request.get('/video-category/list')
}

export function getVideoCategoryDetail(id) {
  return request.get(`/video-category/${id}`)
}

export function addVideoCategory(data) {
  return request.post('/video-category', data)
}

export function updateVideoCategory(data) {
  return request.put('/video-category', data)
}

export function deleteVideoCategory(id) {
  return request.delete(`/video-category/${id}`)
}
