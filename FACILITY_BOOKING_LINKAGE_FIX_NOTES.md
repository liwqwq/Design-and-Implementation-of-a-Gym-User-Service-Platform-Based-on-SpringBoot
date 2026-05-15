# Facility Booking Linkage Fix Notes

This update fixes the linkage between the coach facility booking panel and the add-class form.

## Fixed issues

1. The reserved facility list no longer hides valid bookings just because the reserved time has already passed on the same day. This prevents a booked time slot from disappearing from the venue card while not appearing in “My Reserved Facilities”.
2. The add-class venue selector now uses the coach's confirmed facility bookings as the source of truth.
3. The selected facility booking id is submitted to the backend when creating a class.
4. The backend validates the exact reserved booking before allowing a class to be published.
5. The reserved facility list now shows bookings that have already been used by a class, but disables deletion for those records to avoid data inconsistency.
6. Newly created facility bookings are returned by the booking API and added to the frontend list immediately, then refreshed from the backend.

## User flow after this fix

1. Coach opens `Classes -> Book Facility`.
2. Coach clicks a venue time slot and confirms booking.
3. The time slot disappears from the venue card because it is no longer available.
4. The same booking appears under “My Reserved Facilities”.
5. Coach opens `Add Class`.
6. The venue dropdown only shows confirmed, unused facility bookings.
7. Selecting a booking automatically fills the date, start time, end time, and venue name.
8. If a facility booking is deleted before it is used by a class, the slot returns to available status.
