# Admin and Coach Linkage Fix Notes

This version fixes several cross-portal issues found during manual testing:

1. Admin user/member creation no longer redirects to the public user registration page.
   - Member Management uses an in-page modal to create a member account.
   - User Management uses an in-page modal to create a user account.
   - New accounts are saved through `/api/admin/users` and include a membership record.

2. Facility data is expanded and kept available for existing local databases.
   - Added Zone C Strength, Classroom A, Zone B Spin Studio, Zone D Boxing, Zone E Dance, Swimming Pool, and Zone F Cardio.
   - The initializer now checks and adds missing facilities even when demo data is not reset.

3. Coach facility booking is now connected to class creation.
   - Booking a facility creates a confirmed facility booking.
   - The Add Class form only lists the coach's confirmed, unused facility bookings.
   - Selecting a booked facility automatically fills the date, start time, end time, and venue.
   - The backend blocks class creation when the coach has not booked the selected venue/time first.

4. A duplicated `editClass` declaration in the coach class page was removed.

Run normally with:

```cmd
mvn spring-boot:run
```

and in another terminal:

```cmd
cd frontend
npm install
npm run dev
```
