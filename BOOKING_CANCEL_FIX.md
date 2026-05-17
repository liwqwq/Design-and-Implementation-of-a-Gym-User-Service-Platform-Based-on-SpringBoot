# Booking Cancel Fix (v47)

This version fixes the issue where clicking Cancel Booking briefly changed the button to Book Now and then reverted back to Cancel Booking.

Changes:
- Demo seed bookings are no longer recreated after the current user cancels one.
- `/api/bookings/my` now treats booking statuses case-insensitively.
- Mutable GET requests such as `/api/bookings/my` and `/api/classes` bypass in-flight request reuse to avoid stale booking state.
- The class list is re-flagged from the latest `bookedIds` after booking refresh.
- Frontend production assets were rebuilt and synced to `src/main/resources/static`.
