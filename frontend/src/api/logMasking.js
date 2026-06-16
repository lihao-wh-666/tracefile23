import request from '../utils/request'

export function getMaskingConfig() {
  return request({
    url: '/log-masking/config',
    method: 'get'
  })
}

export function maskLogContent(content, format = 'auto') {
  return request({
    url: '/log-masking/mask',
    method: 'post',
    data: { content, format }
  })
}

export function maskLogFile(file, format = 'auto') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('format', format)
  return request({
    url: '/log-masking/mask-file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function compareLogs(original, masked) {
  return request({
    url: '/log-masking/compare',
    method: 'post',
    data: { original, masked }
  })
}

export function previewMasking(content, format = 'auto') {
  return request({
    url: '/log-masking/preview',
    method: 'post',
    data: { content, format }
  })
}

export function downloadMaskedFile(taskId) {
  return request({
    url: `/log-masking/download/${taskId}`,
    method: 'get',
    responseType: 'blob'
  })
}
