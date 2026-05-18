# Login Stability Fix V51

## Problem
After switching roles or logging in/out repeatedly, the login button could stay in the "logging in" state for several seconds and eventually show a backend timeout message.

## Cause
The login request could be affected by two issues:

1. During local frontend development, the request first went through the Vite `/api` proxy. If the proxy connection waited while Spring Boot was already available on port 8080, the login page appeared stuck.
2. The login endpoints also called the full operational data repair method. When many page data requests were still finishing in the background, this could block the lightweight login request.

## Changes
- User/admin/coach login APIs no longer run the heavy operational data repair flow on every login.
- The frontend now tries the direct Spring Boot 8080 API first for login/register requests when running on a Vite dev port.
- Login request timeout is shorter and the login button state is guarded by a request id, so old login responses cannot keep the button stuck.
- The rebuilt frontend files have been synchronized to `src/main/resources/static` for jar deployment.

## Scope
This fix only changes login stability and request handling. It does not change business features, accounts, database seed content, or UI layout.
