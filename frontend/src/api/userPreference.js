import request from '../utils/request'

export function getUserPreference() {
  return request.get('/user-preference')
}

export function saveUserPreference(data) {
  return request.put('/user-preference', data)
}

export function resetUserPreference() {
  return request.post('/user-preference/reset')
}
