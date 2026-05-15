import { apiGet, apiPost } from './http'

export const getCheckinStatus = () => apiGet('/checkin/status')
export const checkInToday = () => apiPost('/checkin')
export const getMonthlyCheckinRecords = (year, month) => apiGet('/checkin/records/month', { params: { year, month } })
