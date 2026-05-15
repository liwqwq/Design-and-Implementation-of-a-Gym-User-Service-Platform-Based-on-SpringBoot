import { apiGet, apiPost } from './http'

export const getMyPoints = () => apiGet('/points/my')
export const getPointProducts = () => apiGet('/points/products')
export const getExchangeHistory = () => apiGet('/points/history')
export const exchangePointProduct = (productId) => apiPost(`/points/exchange/${productId}`, {})

const normaliseText = (value) => String(value || '').trim().toLowerCase()

export const isCouponProduct = (product = {}) => {
  const discountType = normaliseText(product.discountType)
  const category = normaliseText(product.category)
  return discountType === 'coupon' || ['优惠券', 'coupon', 'coupons'].includes(category)
}

export const isVipProduct = (product = {}) => {
  const category = normaliseText(product.category)
  return product.isVip === true || category.includes('vip') || category === 'vip专属'
}

export const isEquipmentProduct = (product = {}) => {
  const discountType = normaliseText(product.discountType)
  const category = normaliseText(product.category)
  return !isVipProduct(product) && (discountType === 'product' || ['运动装备', '训练辅助', '营养补剂', '健身服饰', '运动配件', 'equipment', 'training accessories', 'supplements', 'apparel', 'sports accessories'].includes(category))
}
