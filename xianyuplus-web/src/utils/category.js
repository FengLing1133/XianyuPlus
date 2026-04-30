// Default emoji for products without images
const productEmojis = ['📦', '🎁', '📚', '🎧', '💻', '👗', '🎮', '🪑', '🏀', '🎸', '📷', '⌚']

// Macaron background colors for product placeholders
const macaronColors = [
  '#fce4ec', '#f3e5f5', '#e8e5f9', '#e3f2fd',
  '#e0f7fa', '#e8f5e9', '#fff9c4', '#fff3e0',
  '#fbe9e7', '#efebe9', '#f1f8e9', '#e8eaf6'
]

export function getProductEmoji(productId) {
  if (typeof productId === 'string') {
    let hash = 0
    for (let i = 0; i < productId.length; i++) {
      hash = productId.charCodeAt(i) + ((hash << 5) - hash)
    }
    return productEmojis[Math.abs(hash) % productEmojis.length]
  }
  return productEmojis[productId % productEmojis.length]
}

export function getProductColor(productId) {
  if (typeof productId === 'string') {
    let hash = 0
    for (let i = 0; i < productId.length; i++) {
      hash = productId.charCodeAt(i) + ((hash << 5) - hash)
    }
    return macaronColors[Math.abs(hash) % macaronColors.length]
  }
  return macaronColors[productId % macaronColors.length]
}

export function conditionText(c) {
  const map = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显痕迹' }
  return map[c] || ''
}
