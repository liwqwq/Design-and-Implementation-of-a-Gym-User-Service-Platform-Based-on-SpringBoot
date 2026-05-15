import { apiGet, apiPost, apiPut } from './http'

export const getUserProfile = () => apiGet('/user/profile')
export const updateUserProfile = (profile) => apiPut('/user/profile', profile)
export const getCheckinQrToken = () => apiGet('/user/checkin-qr-token')
export const submitComplaint = (payload) => apiPost('/complaints', payload)
export const getUserComplaints = () => apiGet('/complaints/user')

export const getUserMembership = () => apiGet('/user/membership')
export const purchaseVipMembership = (payload) => apiPost('/user/membership/purchase-vip', payload)
