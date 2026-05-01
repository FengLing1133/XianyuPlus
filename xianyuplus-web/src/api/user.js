import request from './request'

export function getSellerProfile(id) {
  return request.get(`/user/${id}/profile`)
}

export function getSellerStats(id) {
  return request.get(`/user/${id}/stats`)
}

export function getSellerProducts(id, page = 1, size = 12) {
  return request.get(`/user/${id}/products`, { params: { page, size } })
}
