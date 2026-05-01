function showConfirm(title, message, { confirmText = '确认', cancelText = '取消' } = {}) {
  return new Promise((resolve) => {
    const overlay = document.createElement('div')
    overlay.className = 'modal-overlay'

    const box = document.createElement('div')
    box.className = 'modal-confirm'
    box.innerHTML = `
      <div class="modal-header">${title}</div>
      <div class="modal-body">${message}</div>
      <div class="modal-footer">
        <button class="btn-pill btn-cancel">${cancelText}</button>
        <button class="btn-pill btn-pill-primary btn-confirm">${confirmText}</button>
      </div>
    `

    overlay.appendChild(box)
    document.body.appendChild(overlay)

    const close = (result) => {
      overlay.remove()
      resolve(result)
    }

    box.querySelector('.btn-cancel').onclick = () => close(false)
    box.querySelector('.btn-confirm').onclick = () => close(true)
    overlay.onclick = (e) => { if (e.target === overlay) close(false) }

    // Add styles if not already present
    if (!document.getElementById('modal-styles')) {
      const style = document.createElement('style')
      style.id = 'modal-styles'
      style.textContent = `
        .modal-overlay {
          position: fixed; inset: 0; background: rgba(0,0,0,0.4);
          display: flex; align-items: center; justify-content: center;
          z-index: 10000; animation: fadeIn 0.2s ease;
        }
        .modal-confirm {
          background: #fff; border-radius: 20px; padding: 24px;
          max-width: 400px; width: 90%; box-shadow: 0 16px 48px rgba(0,0,0,0.15);
          animation: scaleIn 0.2s ease;
        }
        .modal-header { font-size: 17px; font-weight: 600; color: var(--color-foreground, #831843); margin-bottom: 10px; }
        .modal-body { font-size: 14px; color: var(--text-secondary, #64748B); margin-bottom: 24px; line-height: 1.6; }
        .modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
        .btn-cancel {
          background: #f5f5f5; color: #666; border-radius: 50px;
          padding: 8px 20px; font-size: 14px; border: none; cursor: pointer;
        }
        .btn-cancel:hover { background: #eee; }
        .btn-confirm { padding: 8px 20px; font-size: 14px; }
        @keyframes fadeIn { from { opacity: 0 } to { opacity: 1 } }
        @keyframes scaleIn { from { transform: scale(0.95); opacity: 0 } to { transform: scale(1); opacity: 1 } }
      `
      document.head.appendChild(style)
    }
  })
}

export const Dialog = {
  confirm(options) {
    if (typeof options === 'string') {
      return showConfirm('提示', options)
    }
    return showConfirm(options.title || '提示', options.message, options)
  }
}
