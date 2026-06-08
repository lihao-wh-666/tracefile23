import request from '../utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(data) {
  return request.put('/user/password', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getUserPage(params) {
  return request.get('/user/page', { params })
}

export function getUserDetail(id) {
  return request.get(`/user/${id}`)
}

export function addUser(data) {
  return request.post('/user', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/user/${id}/status`, null, { params: { status } })
}
