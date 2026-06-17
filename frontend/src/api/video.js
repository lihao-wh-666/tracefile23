import request from '../utils/request'

export function getVideoPage(params) {
  return request.get('/video/page', { params })
}

export function getVideoManagePage(params) {
  return request.get('/video/manage-page', { params })
}

export function getVideoDetail(id) {
  return request.get(`/video/${id}`)
}

export function getVideoInfo(id) {
  return request.get(`/video/info/${id}`)
}

export function addVideo(data) {
  return request.post('/video', data)
}

export function updateVideo(id, data) {
  return request.put('/video', data, { params: { id } })
}

export function deleteVideo(id) {
  return request.delete(`/video/${id}`)
}

export function uploadVideo(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/video/upload', formData, {
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    },
    timeout: 600000
  })
}

export function uploadVideoCover(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/video/upload-cover', formData, {
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}

export function getVideoCategories() {
  return request.get('/video-category/list')
}

export function getRelatedVideos(videoId, limit = 6) {
  return request.get(`/video/related/${videoId}`, { params: { limit } })
}

export function getHotVideos(limit = 10) {
  return request.get('/video/hot', { params: { limit } })
}
