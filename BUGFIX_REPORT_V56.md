# V56 Count Display & Private Coach Data Fix

## Problem
On the user course booking page, the private coach summary displayed 6 coaches while only 4 coach cards were rendered. This was caused by the frontend using `coaches.length` for the summary but slicing the visible private coach list to four records.

## Fixes
- The private coach list now renders all unique coach records returned by the backend instead of truncating to four.
- The private coach summary count now uses the same computed list that is actually displayed.
- The group class summary now counts only group classes, avoiding future mismatch if private classes are ever included in the same local list.
- Backend seed repair now ensures every coach has at least one private course, so displaying all coaches will not create empty or misleading coach detail modals.

## Scope
Only the user-side course booking count/list consistency and related private-course seed repair were changed. Existing UI style, accounts, and business modules were kept unchanged.
