export const getStoredUser = () => {
  const rawUser = localStorage.getItem('user')
  if (!rawUser) return null
  try {
    return JSON.parse(rawUser)
  } catch (error) {
    console.warn('Invalid local user profile:', error)
    return null
  }
}

export const getCurrentUsername = (fallback = '') => {
  const user = getStoredUser()
  return user?.username || localStorage.getItem('username') || fallback
}

export const getUserType = () => localStorage.getItem('userType') || ''

export const getToken = () => localStorage.getItem('token') || ''

export const isSignedInAs = (role) => Boolean(getToken()) && getUserType() === role

export const clearSession = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
}

export const saveSession = ({ token, username, userType, user, coachId }) => {
  clearSession()
  if (token) localStorage.setItem('token', token)
  if (username) localStorage.setItem('username', username)
  if (userType) localStorage.setItem('userType', userType)
  if (user) localStorage.setItem('user', JSON.stringify(user))
  if (coachId) localStorage.setItem('coachId', coachId)
}
