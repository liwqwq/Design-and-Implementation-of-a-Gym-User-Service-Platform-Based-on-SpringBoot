# V55 Private Booking Click Fix

## Fixed issue
The private coach course modal displayed available private courses, but clicking the `预约 / Book` button could appear to have no effect because some seeded private courses were already treated as full.

## Changes
- Increased default private-course capacity from 1 to 3 for demo usability.
- Added a backend repair step that recalculates private-course `bookedCount` from confirmed bookings and keeps at least one private-course slot available.
- Added a safety check in the booking API: if an existing private course is accidentally full because of old state data, the API expands its capacity instead of returning `课程已满`.
- Updated the private-course modal button state so truly full courses show `已满 / Full` and are disabled instead of looking clickable.
- Changed the MySQL state key to `fitlife-v55-private-booking-state` so old v49-v54 state data will not keep the private courses stuck as full.

## Build
- `npm run build` passed.
- The generated frontend files were copied into `src/main/resources/static`.
