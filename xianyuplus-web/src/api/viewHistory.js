import request from './request'

export function recordView(productId) {
  return request.post('/view-history', { productId })
}

export function getViewHistory(page = 1, size = 20) {
  return request.get('/view-history', { params: { page, size } })
}

export function clearViewHistory() {
  return request.delete('/view-history')
}

export function deleteViewHistory(id) {
  return request.delete(`/view-history/${id}`)
}
