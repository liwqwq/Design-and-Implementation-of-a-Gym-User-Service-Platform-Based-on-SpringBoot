export const formatDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const toDateKey = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '' : formatDateKey(date)
}

export const weekLabels = (lang) => lang === 'en'
  ? ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
  : ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export const buildUpcomingDays = ({ lang = 'zh', t, includeDateInLabel = false, count = 7 } = {}) => {
  const labels = weekLabels(lang)
  const today = new Date()
  return Array.from({ length: count }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() + index)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekday = labels[date.getDay()]
    const plainLabel = index === 0 ? t?.('user.classes.today') || '今天' : index === 1 ? t?.('user.classes.tomorrow') || '明天' : weekday
    return {
      date: formatDateKey(date),
      label: includeDateInLabel && index > 1 ? `${plainLabel} ${month}/${day}` : plainLabel,
      day: `${month}/${day}`
    }
  })
}

export const formatTimeRange = (startTime, endTime) => {
  if (!startTime) return '09:00-10:00'
  try {
    const start = new Date(startTime)
    const end = endTime ? new Date(endTime) : new Date(start.getTime() + 60 * 60 * 1000)
    const pad = (value) => String(value).padStart(2, '0')
    return `${pad(start.getHours())}:${pad(start.getMinutes())}-${pad(end.getHours())}:${pad(end.getMinutes())}`
  } catch (error) {
    return '09:00-10:00'
  }
}

export const formatRelativeDateTime = (value, { lang = 'zh', t } = {}) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const startOfTarget = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const days = Math.round((startOfTarget - startOfToday) / (1000 * 60 * 60 * 24))
  const hm = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  if (days === 0) return `${t?.('user.checkin.today') || 'Today'} ${hm}`
  if (days === 1) return `${t?.('user.checkin.tomorrow') || 'Tomorrow'} ${hm}`
  const localeTag = lang === 'zh' ? 'zh-CN' : 'en-US'
  return `${new Intl.DateTimeFormat(localeTag, { month: 'short', day: 'numeric' }).format(date)} ${hm}`
}
