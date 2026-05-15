export const courseIcon = (name = '') => {
  const icons = {
    '力量训练': '🏋️',
    '瑜伽放松': '🧘',
    '动感单车': '🚴',
    '跑步': '🏃',
    '游泳': '🏊',
    '普拉提': '🧘‍♀️',
    '搏击': '🥊',
    '拳击': '🥊',
    '舞蹈': '💃',
    '有氧操': '🤸'
  }
  return icons[name] || '🏋️'
}

export const displayInstructorName = (name, t) => {
  const value = String(name || '').trim()
  if (!value || value === '教练') return t?.('common.trainerPending') || 'Trainer pending'
  return value
}

export const remainingSeats = (course = {}) => {
  if (course.spotsLeft !== undefined && course.spotsLeft !== null) {
    return Math.max(0, Number(course.spotsLeft) || 0)
  }
  const capacity = Number(course.capacity) || 0
  const booked = Number(course.bookedCount) || 0
  return capacity > 0 ? Math.max(0, capacity - booked) : 0
}
