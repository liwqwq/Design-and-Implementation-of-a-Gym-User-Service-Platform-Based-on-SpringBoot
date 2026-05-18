# v54 Booking and Private Course Stability Fix

## Fixed issues

1. **Group class booking timeout / button stuck in processing**
   - Removed nested booking-list and store-level save locks in the booking and cancellation endpoints.
   - The booking mutation is now completed inside the booking lock, while MySQL persistence runs after the lock is released.
   - This avoids request deadlock when `/api/classes`, `/api/bookings/my`, and `/api/bookings` are triggered close together.

2. **Private coach course modal sometimes showing no courses**
   - Added a loading state for the private coach course modal.
   - The modal now fetches `/api/trainers/{id}/courses` with a bounded timeout and falls back to local/default private course rows instead of leaving the section blank.

3. **Booking state refresh stability**
   - `classViews` now copies class rows before computing booking flags, avoiding class-list -> booking-list lock nesting.
   - `myBookings` now copies matching booking rows before resolving class details, avoiding booking-list -> class-list lock nesting.

## Build

- Frontend build completed with `npm run build`.
- Built files were synced to `src/main/resources/static`.
- Maven is not installed in the sandbox, so the jar still needs to be packaged locally with `mvn clean package -DskipTests`.
