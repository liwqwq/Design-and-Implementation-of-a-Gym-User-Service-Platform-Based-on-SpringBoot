# Gym User Service Platform

Gym User Service Platform is a gym user service platform built on **Spring Boot + Vue 3 + MySQL**. The project includes three interfaces: **user interface, coach interface, and administrator interface**, designed around functions such as course booking, personal training appointment, venue reservation, VIP membership, points mall, daily check-in, community interaction, leaderboard, and back-end management.

By default, this project runs locally, with the database connection address being `localhost:3306/gym_db`. It will not connect to a cloud database.

---

## 1. tech stack

| Module | Technology |
|---|---|
| Backend | Spring Boot 2.7.15, Spring Security, JWT, Spring Data JPA |
| Database | MySQL, local database name `gym_db` |
| Front-end | Vue 3, Vue Router, Vue I18n, Element Plus, Axios, Vite |
| Build and Run | Maven / Maven Wrapper, Node.js, npm |

---

## 2.  Overview of final version features

### 2.1 User side

The user end is designed for daily use by regular members, with its primary functions including:

- User registration, login, password recovery, password reset
- Home page information display: points, continuous check-ins, quick entry, course recommendations, VIP access
- Course Reservation Center: group lesson reservation, private lesson reservation, reservation cancellation
- Personal Center: Personal Profile, Scheduled Courses, Course Cancellation, Complaints and Suggestions, Points Record
- VIP purchase page: selection and simulated payment for monthly, quarterly, and annual VIP packages
- Points Mall: Product Categories, Points Redemption, Inventory Changes, Redemption Records
- Daily check-in: Today's check-in, consecutive check-in days, monthly calendar, point rewards, medal display
- Community interaction: posting updates, liking, commenting, reporting, joining/leaving groups
- Leaderboards: Overall Leaderboard, Weekly Leaderboard, Monthly Leaderboard
- Switch between Chinese and English

### 2.2 Coach's side

The coach terminal is used for managing courses, venues, and student information. Its main functions include:

- Coach login
- Coach homepage: Today's courses, pending reminders, course statistics
- Course management: View courses, list courses, edit courses, delete courses
- Venue reservation: View available reservation time slots, reserve venues, view personally reserved venues, and cancel unused venue reservations
- When listing courses, the venue dropdown box only displays the time slots of the venue that have been reserved by the current coach and are not occupied by any courses
- View student information
- Coach mailbox: view user complaints and course evaluations, handle complaints and respond to evaluations
- Coach's personal information, qualifications, professional field, notification settings
- Switch between Chinese and English

### 2.3 Administrator Interface

The administrator terminal is used for system backend management, with main functions including:

- Administrator login
- User management: add, edit, and delete users
- Member management: Add new members, edit members, delete members, and perform member status statistics
- Course management: View, edit, and delete courses, and synchronize with user-side appointments and coach-side course data
- Product management: directly manage the products in the user-end points mall
  - Add, edit, and delete products
  - Set Chinese name, English name, Chinese description, and English description
  - Set product categories, product icons, point prices, inventory, and on/off-shelf statuses
  - When switching between Chinese and English, the product name and description in the corresponding language should be displayed first
- Community management: Viewing community content, handling reports
- Switch between Chinese and English

---

## 3.  Three-terminal linkage description

The system focuses on the data linkage between the user terminal, coach terminal, and administrator terminal:

```text
User-end course reservation
  -> Synchronized change of course participants on the coach's end
  -> Synchronization changes of course data on the administrator side

Cancel reservation on the user side
  -> The number of students in the course decreases synchronously on both the coach and administrator ends

Booking a venue on the coach's side
  -> Remove the corresponding venue time slot from the available booking list
  -> Instructors can only choose the venue time slots that they have already booked when listing courses on the platform

The coach deletes unused venue reservations
  -> The corresponding venue time slot is restored to a bookable state

List courses on the coach's platform
  -> Display the course in the user's course reservation center
  -> The administrator's course management displays the course

Customer complaints/reviews
  -> The Coach Mailbox on the coach's end can be viewed and processed

Modify courses on the administrator side
  -> Synchronized changes in course information between the user and coach terminals

User-end clock-in, redeem goods, purchase VIP
  -> Dynamic changes in user points and membership status

Add/edit/delete points mall products on the administrator side
  -> The product list, inventory, point price, and names in both Chinese and English in the user's point mall synchronously change
```

---

## 4.  Local database configuration

The database configuration is located at:

```text
src/main/resources/application.yml
```

Default configuration:

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gym_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:lnj031212}
```

Meaning:

```text
Database address: localhost:3306
Database name: gym_db
Default account: root
Default password: lnj031212
```

If your MySQL password is not `lnj031212`, you can temporarily set it before starting the backend:

```cmd
set MYSQL_USERNAME=root
Set MYSQL_PASSWORD to your MySQL password
```

If you have previously set an old password in the same CMD, for example:

```cmd
set MYSQL_PASSWORD=zl123456
```

Please close and reopen CMD, or reset it to your current password.

---

## 5.  Create a database before the first run

Open the Command Prompt (CMD) and enter:

```cmd
mysql -u root -p
```

After entering your MySQL password, execute:

```sql
CREATE DATABASE IF NOT EXISTS gym_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

The backend connection parameters include `createDatabaseIfNotExist=true`, which usually allows for automatic database creation if it doesn't exist. However, it is recommended to manually create the database for the first time to ensure that the MySQL account and password are correct.

---

## 6. Start backend

Execute in the root directory of the project, which contains ` pom. xml `:

```cmd
mvn spring-boot:run
```

Default backend address:

```text
http://localhost:8080
```

If the computer does not have global Maven, you can try:

```cmd
mvnw.cmd spring-boot:run
```

---

## 7. Start the front-end

Open another CMD:

```cmd
cd frontend
npm install
npm run dev
```

Default front-end address:

```text
http://localhost:3000
```

Vite will proxy `/app ` requests to the backend:

```text
http://localhost:8080
```

This project recommends using:

```text
Backend: http://localhost:8080
front-end http://localhost:3000
```

That is to say, the backend and frontend are started separately.

---

## 8. Access Portal

|Port | Address|
|---|---|
|User end| http://localhost:3000/user/login  |
|Coach end| http://localhost:3000/coach/login  |
|Administrator end| http://localhost:3000/admin/login  |

---

## 9. test account

###User account

```text
fitness_pro / 123456
gym_master / 123456
yoga_lover / 123456
```

###Coach account

```text
coach_1 / 123456
coach_2 / 123456
coach_3 / 123456
coach_4 / 123456
```

###Administrator account

```text
admin / admin123
```

Explanation: When the backend is started, it will check the demo login account to ensure that the 'role' of 'fitnessspro', 'gym_master', and 'yoga after' is' USER 'and the password is' 123456', in order to avoid administrators changing their login identity during testing and being unable to log in.

---

## 10. Data initialization instructions

When the backend starts, it will check whether the basic data exists:

-If the database is empty, it will initialize data such as demo users, coaches, administrators, courses, venues, point mall products, community posts, etc.
-If the database already has data, it will not be forcibly cleared by default.
-The basic data of the venue will be automatically supplemented to avoid missing areas on the venue reservation page.
-The products in the points mall will automatically be supplemented with English names and descriptions.
-The old version of regular products will be automatically synchronized to the product table of the points mall, ensuring that the administrator's product management and the user's points mall use the same set of data.

If you need to regenerate the presentation data, you can set the following settings before starting the backend:

```cmd
set RESET_DEMO_DATA=true
mvn spring-boot:run
```

Do not set 'RESIT.DEMO-DATA=true' normally, otherwise the data added, edited, or deleted on the administrator side may be reset.

---

## 11. Main directory structure

```text
Gym User Service Platform-final/
├── pom.xml
├── build.gradle
├── README.md
├── scripts/
│   └── repair_demo_user_login.sql
├── src/main/java/com/gym/service/
│∝ - config/# Security configuration, initialization data, cross domain configuration
│∝ - Controller/# Backend Interface
│∝ - entity/# database entity
│∝ - Repository/# JPA Data Access Layer
│∝ - Service/# Business Logic Layer
│└ - Utility/# JWT tool class
├── src/main/resources/
│∝ - Application. yml # Local database and backend port configuration
│   └── application.properties
└── frontend/
├── package.json
├── package-lock.json
├── vite.config.js
└── src/
∝ - router/# Front end router
∝ - i18n/# Chinese English Language Pack
∝ - Services/# Front end API Request Encapsulation
∝ - config/# User side navigation configuration
∝ - Components/# Common Components
└ - Views/# User, Coach, and Administrator Pages
```

---

## 12. Frequently Asked Questions

###12.1 MySQL password cannot log in

If executed:

```cmd
mysql -u root -p
```

Unable to log in by entering 'lnj031212', but can log in by entering the old password, indicating that the MySQL root password on your computer has not been changed. You can first use the old password to enter MySQL, and then execute:

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'lnj031212';
FLUSH PRIVILEGES;
```

###12.2 Backend startup failed, prompting database connection error

check

```text
1. Has the MySQL service been started
2. Is the root password consistent with application.yml or MYVNet PASSWORD
3. Does the gym_db database exist
4. Are there any old MYVNet PASSWORD environment variables remaining in the same CMD
```

If the old environment variables have been set, you can close CMD and reopen it, or reset:

```cmd
set MYSQL_PASSWORD=lnj031212
```

###12.3 Front end page displays old content

The browser may cache old pages. After launching the new version, it is recommended to follow:

```text
Ctrl + F5
```

###12.4 Only starting the backend access 8080 does not show the new version of the frontend

The current project recommends running with front-end and back-end separation:

```text
Backend: http://localhost:8080
front-end http://localhost:3000
```

If only the backend is launched and accessed` http://localhost:8080 `You may not be able to see the latest frontend page because the current source code package does not use 'src/main/resources/static' as the main frontend entry.

###12.5 User account suddenly unable to log in

If the administrator mistakenly changes the user's' role 'to' VIP 'or other values during testing, the user login may fail. The correct rule is:

```text
Users. role=login identity, can only be User/COACH/ADMIN
Membership=membership level, such as VIP/regular member
```

Can execute:

```sql
USE gym_db;
UPDATE users
SET role = 'USER', active = 1, password = '123456'
WHERE username IN ('fitness_pro', 'gym_master', 'yoga_lover');
```

After the project is launched, these three demo user accounts will also be automatically checked and repaired.

---

## 13. Final inspection record of this version

This version completed the following checks and final fixes before packaging:

```text
1. Checked the front-end relative path import and did not find any missing references.
2. Check zhjson/enjson, JSON syntax is normal.
3. Checked Vue and JS script syntax, no syntax errors were found.
4. Checked the bracket structure of the Java source code and did not find any obvious structural errors.
5. Confirm that the database is still connected to the local localhost: 3306/gym_db.
6. Confirm that the default database password is lnj031212.
7. Confirm that the main routing entry points for the user end, coach end, and administrator end exist.
8. Confirm that the fields between the venue reservation, deletion reservation, and the pull-out box of the course venue have been aligned.
9. Confirm that the VIP payment page and homepage entrance on the user side are retained.
10. Confirm that using the points mall product table for administrator side product management can affect the user side points mall.
11. Fixed the issue of missing English names/descriptions when synchronizing old regular products to the points mall.
12. Align the JWT configuration in application.exe and application.jml to avoid confusion caused by duplicate configurations.
```

Explanation: The current checking environment is unable to download Gradle from the internet, and there is no global Maven available. Therefore, please perform the full backend compilation on your local computer

```cmd
mvn spring-boot:run
```

Please run the front-end fully locally:

```cmd
cd frontend
npm install
npm run dev
```

---

## 14.Team Contribution
Nanjiang Li: Responsible for user profile and personal centre, including user information, membership display, booked class list, cancellation from profile, complaint entry, and related user-side workflow testing.

Junhaotong Zhu: Responsible for VIP membership, points, points mall, administrator product linkage, bilingual product display, daily check-in, points rewards, and related testing/documentation.

Qilong Wang: Responsible for class management, group/private class booking, coach facility reservation, add-class linkage with reserved venues, administrator class management, and class-related testing.

Shurui Guo: Responsible for community, ranking, complaints, reports, coach mailbox, administrator community management, and related interaction/testing functions.
