import request from './request'

export function createReview(orderId, rating, content) {
  return request.post('/review', { orderId, rating, content })
}

export function replyReview(id, reply) {
  return request.put(`/review/${id}/reply`, { reply })
}

export function deleteReview(id) {
  return request.delete(`/review/${id}`)
}

export function deleteReply(id) {
  return request.delete(`/review/${id}/reply`)
}

export function getProductReviews(productId, page = 1, size = 10) {
  return request.get(`/review/product/${productId}`, { params: { page, size } })
}

export function getSellerReviews(sellerId, page = 1, size = 10) {
  return request.get(`/review/seller/${sellerId}`, { params: { page, size } })
}

export function getSellerStats(sellerId) {
  return request.get(`/review/seller/${sellerId}/stats`)
}

export function checkCanReview(orderId) {
  return request.get(`/review/check/${orderId}`)
}
