// Placeholder gradients for product cards (used by ProductDetail)
const placeholderColors = [
  'var(--placeholder-pink)', 'var(--placeholder-cyan)', 'var(--placeholder-orange)',
  'var(--placeholder-purple)', 'var(--placeholder-green)', 'var(--placeholder-red)',
  'var(--placeholder-blue)', 'var(--placeholder-yellow)'
]

export function getProductColor(productId) {
  if (typeof productId === 'string') {
    let hash = 0
    for (let i = 0; i < productId.length; i++) {
      hash = productId.charCodeAt(i) + ((hash << 5) - hash)
    }
    return placeholderColors[Math.abs(hash) % placeholderColors.length]
  }
  return placeholderColors[productId % placeholderColors.length]
}

export function conditionText(c) {
  const map = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显痕迹' }
  return map[c] || ''
}
