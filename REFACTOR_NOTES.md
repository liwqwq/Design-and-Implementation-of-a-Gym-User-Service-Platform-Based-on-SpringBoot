# Refactor Notes

This version keeps the existing product behaviour and UI style, but reorganises core frontend code into reusable modules.

## What changed

- Centralised session handling in `frontend/src/services/session.js`.
- Centralised authenticated API requests in `frontend/src/services/http.js`.
- Split domain API calls into service modules:
  - `checkinService.js`
  - `classBookingService.js`
  - `pointsService.js`
  - `rankingService.js`
  - `userService.js`
- Moved repeated date/time logic into `frontend/src/utils/date.js`.
- Moved course display metadata into `frontend/src/utils/courseMeta.js`.
- Moved user navigation configuration into `frontend/src/config/userNavigation.js`.
- Refactored major user pages to use service modules instead of raw repeated `axios` code:
  - `user-dashboard.vue`
  - `user-classes.vue`
  - `user-profile.vue`
  - `user-points-mall.vue`
  - `user-checkin.vue`
  - `user-ranking.vue`
  - `UserLayout.vue`
  - `router/index.js`

## Behaviour kept

- User dashboard still shows the same overview and booking entry points.
- Class booking still supports group/private courses and booking/cancellation.
- Profile page only manages personal information, complaints, QR code and existing bookings.
- Points mall still supports product filtering and exchange history.
- Check-in and ranking still use the same backend APIs.

## Quality improvements

- Repeated authentication header code has been removed from the refactored pages.
- Mock fallback booking data has been removed from the core booking pages.
- API endpoint names are centralised, so later backend changes are easier to maintain.
- Date labels and course time formatting use shared utility functions.
