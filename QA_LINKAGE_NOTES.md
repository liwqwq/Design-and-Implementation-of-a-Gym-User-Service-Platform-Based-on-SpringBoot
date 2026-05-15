# QA linkage and login UI update notes

## Linkage fixes

- Admin class management now loads `/api/admin/classes` instead of the user-facing `/api/classes`, so it can see all group/private classes, including inactive and pending courses.
- Admin class management now supports editing course name, coach, capacity, time, location and status.
- User class booking now only shows active group classes, avoiding pending/inactive/admin-hidden courses appearing to members.
- Coach-created classes are saved as active classes, so they can appear in admin management and user booking immediately.
- User complaints no longer hard-code `coach_1`. If the user has a confirmed booking, the complaint is routed to the coach of the latest booked class; otherwise it falls back to the demo coach.
- User complaint history now displays coach-processed complaints as resolved rather than showing raw `processed` status.
- Logout/session clearing no longer wipes the saved language preference.

## Login UI fixes

- Rebuilt the user, admin and coach login pages using a shared visual system.
- Added a two-column landing layout, role cards, test account cards and consistent input/button styling.
- Added missing i18n text for admin and coach login intro copy.
