import request from '../utils/request'

export function getUserInfo() {
  return request.get('/user/info')
}

export function addUser(data) {
  return request.post('/user', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}
