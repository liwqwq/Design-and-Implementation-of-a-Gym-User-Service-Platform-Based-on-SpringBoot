import { apiGet, apiPost, apiPut } from './http'
import { formatTimeRange } from '../utils/date'

export const confirmedStatuses = new Set(['CONFIRMED', 'PENDING'])

export const getClassList = (date = '') => apiGet('/classes', date ? { params: { date } } : {})
export const getMyBookings = () => apiGet('/bookings/my')
export const createBooking = (classId) => apiPost('/bookings', { classId })
export const cancelBookingByClass = (classId) => apiPut(`/bookings/class/${classId}/cancel`, {})
export const getTrainers = () => apiGet('/trainers')
export const getTrainerCourses = (trainerId) => apiGet(`/trainers/${trainerId}/courses`)

export const isActiveBooking = (booking) => confirmedStatuses.has(booking?.status)

export const bookingCount = (bookings = []) => bookings.filter(isActiveBooking).length

export const mapBookingIdsByClass = (bookings = []) => bookings
  .filter(isActiveBooking)
  .reduce((map, booking) => {
    const classId = booking.classId || booking.classes?.id
    if (classId) map[classId] = booking.id
    return map
  }, {})

export const applyBookingState = (courses = [], bookings = []) => {
  const idMap = mapBookingIdsByClass(bookings)
  return courses.map((course) => {
    const bookingId = idMap[course.id] || course.bookingId || null
    return { ...course, isBooked: Boolean(bookingId || course.isBooked), bookingId }
  })
}

export const normaliseClassCard = (course = {}, trainerFallback = '') => ({
  id: course.id,
  name: course.name,
  nameEn: course.nameEn,
  trainer: course.instructor || course.coachName || trainerFallback,
  time: course.time || formatTimeRange(course.startTime, course.endTime),
  bookedCount: Number(course.bookedCount || 0),
  capacity: Number(course.capacity || 20),
  isBooked: Boolean(course.isBooked),
  bookingId: course.bookingId || null,
  startTime: course.startTime,
  endTime: course.endTime
})

export const normaliseTrainer = (trainer = {}) => ({
  id: trainer.id,
  name: trainer.name,
  avatar: trainer.avatarUrl || trainer.avatar || '👤',
  specialties: trainer.specialty ? trainer.specialty.split(',').map(item => item.trim()).filter(Boolean) : (trainer.specialties || []),
  rating: trainer.rating,
  experienceYears: trainer.experienceYears,
  hourlyRate: trainer.hourlyRate
})

export const normalisePrivateCourse = (course = {}) => ({
  id: course.id,
  name: course.name,
  nameEn: course.nameEn,
  time: course.time || formatTimeRange(course.startTime, course.endTime),
  duration: course.duration,
  durationMinutes: course.durationMinutes,
  bookedCount: Number(course.bookedCount || 0),
  capacity: Number(course.capacity || 1),
  isFull: Boolean(course.isFull || Number(course.bookedCount || 0) >= Number(course.capacity || 1)),
  isBooked: Boolean(course.isBooked),
  bookingId: course.bookingId || null
})
