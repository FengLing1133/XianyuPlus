import request from './request'

export function getNotifications(page = 1, size = 20) {
  return request.get('/notification', { params: { page, size } })
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markAsRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/notification/read-all')
}

export function deleteNotification(id) {
  return request.delete(`/notification/${id}`)
}
