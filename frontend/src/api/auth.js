import request from '../utils/request'

export function getPublicKey() {
  return request.get('/auth/public-key')
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function sendCode(data) {
  return request.post('/auth/send-code', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}

export function logout() {
  return request.post('/auth/logout')
}
