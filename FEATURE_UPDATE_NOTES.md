# Update Notes - Facility Cancel and VIP Payment

## Coach facility booking
- Added a delete action under the coach facility booking panel.
- Deleting an unused facility booking removes the corresponding `facility_bookings` record.
- The released time slot becomes available again in the facility booking grid.
- If a facility booking has already been used to publish a class, the backend blocks deletion to prevent class/facility mismatch.

## User VIP purchase
- Added a VIP upgrade card on the user dashboard.
- Added a new demo payment page at `/user/vip-payment`.
- The payment page supports monthly, quarterly, and yearly VIP plans.
- Confirming payment simulates a successful payment and updates the user's membership in the local database.
- The updated membership can be reflected in user profile, admin membership management, and coach-side student membership display.
