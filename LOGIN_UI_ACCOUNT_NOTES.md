# Login UI Account Panel Update

This version keeps the polished login layout but restores the complete test-account reference panel on every login page.

## Changed pages

- `frontend/src/views/user/user-login.vue`
- `frontend/src/views/coach/coach-login.vue`
- `frontend/src/views/admin/admin-login.vue`

## New shared component

- `frontend/src/components/auth/TestAccountsPanel.vue`

The component displays the same test accounts on all three portals:

- Users: `fitness_pro`, `gym_master`, `yoga_lover` / `123456`
- Coaches: `coach_1` ~ `coach_4` / `123456`
- Admin: `admin` / `admin123`

The current portal is visually highlighted, so the user login page highlights the user account group, the coach login page highlights the coach group, and the admin login page highlights the admin group.

## Style update

- Enlarged the FitLife brand icon on the login showcase area.
- Added spacing between the FitLife brand pill and the role/topic badge, such as “课程预约 / Class Booking”.
- Kept the unified purple-gradient visual language.

## 2026-05-15 Role-specific test account display fix

The login test account panel now renders only the accounts for the current login role:

- User login page: normal user accounts only.
- Coach login page: coach accounts only.
- Admin login page: admin account only.

This avoids showing unrelated test accounts on each role login screen while keeping the shared component implementation.
