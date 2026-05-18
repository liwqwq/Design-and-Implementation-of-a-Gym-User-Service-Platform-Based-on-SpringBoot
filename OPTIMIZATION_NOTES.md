# v46 Optimization Notes

This version keeps the original v45 color/UI style and focuses on code stability and responsiveness.

## Frontend
- Added GET request de-duplication to avoid repeated identical requests during page switching and data refresh.
- Removed unnecessary class reload after points redemption so mall actions no longer affect class data.
- Added booking request guards to prevent double-click duplicate booking/cancel requests.
- Added disabled button state while booking or redemption is processing.
- Made social, ranking, and check-in loaders keep existing data during temporary API failures instead of clearing the page.
- Reduced comment preloading overhead on the community page by limiting reply preload work for long post lists.

## Backend
- Added capacity checks before booking a course.
- Made booking status checks case-insensitive for more robust cancellation/duplicate detection.
- Ensured booking endpoints repair/load operational seed data before returning results.

## Build
- Frontend production build has been regenerated.
- Built files have been copied to `src/main/resources/static` for Spring Boot deployment.
