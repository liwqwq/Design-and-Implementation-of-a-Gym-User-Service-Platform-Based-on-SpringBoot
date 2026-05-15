import { apiGet } from './http'

export const rankingEndpoint = (period) => {
  if (period === 'week') return '/ranking/week'
  if (period === 'month') return '/ranking/month'
  return '/ranking/total'
}

export const getRankings = (period = 'total') => apiGet(rankingEndpoint(period))

export const normaliseRankingItem = (item = {}) => ({
  id: item.userId,
  name: item.name,
  avatar: item.avatar,
  score: item.totalClasses,
  days: item.checkinDays,
  classes: item.totalClasses,
  points: item.availablePoints || 0
})
