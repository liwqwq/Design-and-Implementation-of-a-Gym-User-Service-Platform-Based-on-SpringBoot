# V48 Bug Check and Stability Fixes

This version keeps the original color style and previous UI fixes. It focuses on hidden state and refresh bugs found during static review and build verification.

## Fixed

1. Booking state stability
   - Course booking flags now fall back to server class `isBooked` values if `/api/bookings/my` is temporarily unavailable.
   - GET request de-duplication remains disabled for booking and class data to avoid stale responses overriding the newest state.
   - Booking status checks are now case-insensitive in both class view and coach student calculations.

2. User page data stability
   - Points, membership/profile, and coach dashboard data no longer reset to `0` or empty arrays when a single API request fails temporarily.
   - Existing displayed data is preserved until fresh valid data is returned.

3. Community interaction consistency
   - Post like state is now user-specific via `likedBy`, avoiding one user's like status affecting other users.
   - Team join/leave state is now user-specific via `memberUsernames`, avoiding global joined/left state across accounts.
   - Team creators cannot accidentally leave their own team.

4. Build output
   - Frontend build completed successfully.
   - New `frontend/dist` files were copied into `src/main/resources/static` for Spring Boot jar deployment.

## Verified

- `npm run build` completed successfully.
- Java controller and store were syntax-checked with local compile stubs because Maven is not installed in the sandbox.
