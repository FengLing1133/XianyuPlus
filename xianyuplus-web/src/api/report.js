import request from './request'

export function createReport(productId, reason, description) {
  return request.post('/report', { productId, reason, description })
}

export function checkReported(productId) {
  return request.get(`/report/product/${productId}/check`)
}

export function getReports(status, page = 1, size = 10) {
  return request.get('/admin/report', { params: { status, page, size } })
}

export function handleReport(id, status, adminNote) {
  return request.put(`/admin/report/${id}/handle`, { status, adminNote })
}
