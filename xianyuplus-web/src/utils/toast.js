let toastTimer = null

function showToast(message, type = 'info') {
  const existing = document.querySelector('.toast')
  if (existing) existing.remove()
  if (toastTimer) clearTimeout(toastTimer)

  const el = document.createElement('div')
  el.className = `toast toast-${type}`
  el.textContent = message
  document.body.appendChild(el)

  toastTimer = setTimeout(() => {
    el.remove()
    toastTimer = null
  }, 2500)
}

export const Toast = {
  success(msg) { showToast(msg, 'success') },
  error(msg) { showToast(msg, 'error') },
  info(msg) { showToast(msg, 'info') }
}
