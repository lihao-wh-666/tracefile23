import request from '../utils/request'

export function getPublicKey() {
  return request.get('/auth/public-key')
}

export function login(data) {
  return request.post('/auth/login', data)
}
