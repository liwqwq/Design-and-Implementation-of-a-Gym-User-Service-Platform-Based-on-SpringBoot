#Design and Implementation of a Gym User Service Platform Based on SpringBoot

V49 is a MySQL dynamic persistence+rich demonstration data version, with the technical structure maintained as follows:

-Backend: Spring Boot 2.7+Java 8+JDBC+MySQL
-Front end: Vue 3+Vite
-Development and operation: backend 'mvn spring boot: run', frontend 'npm run dev'`
-Production run: ` mvn clean package - DskipTests ` followed by ` java jar target/gym user service platform - mysql-dynamic-1.0.0.jar '`

##1、 MySQL configuration

Default Connection:

```properties
spring.datasource.url=jdbc: mysql://127.0.0.1:3306/fitlife_service?createDatabaseIfNotExist=true...
spring.datasource.username=root
spring.datasource.password=lnj031212
```

If your MySQL password is not 'lnj031212', please modify it:

```text
src/main/resources/application.properties
```

Execute when using or resetting the database structure for the first time:

```bash
mysql -uroot -p < database/fitlife_mysql.sql
```

If the database already exists and '/app/health' displays' storage: MySQL ', subsequent startup usually does not require repeated SQL execution.

##2、 Development mode operation

Backend:

```bash
mvn spring-boot:run
```

front-end

```bash
cd frontend
npm install
npm run dev
```

visit:

```text
http://localhost:3000
```

Vite has configured proxy:

```text
/api ->  http://127.0.0.1:8080
```

The front-end has also added a direct connection fallback from 3000 to 8080. After frequently switching between the user end/management end/coach end, the login button will not remain stuck in the "login" button.

##3、 Default test account

User testing member account, password is' 123456 ':

```text
fitness_pro
strength_member
yoga_member
running_king
dance_queen
fit_newbie
```

Other ports:

```text
Administrator end: admin/admin123
Coach end: coach_1/123456
Coach end: coach_2/123456
Coach end: coach_3/123456
```

##4、 This version mainly fixes and optimizes

-Fixed the issue of login freezing after frequent port switching: login request timeout of 7 seconds, automatic release button after failure, direct connection to 8080 in development mode as a fallback.
-Increase the number of test accounts on the user login page to 3 and update the initialized user email synchronously.
-The user side homepage Community is no longer blank: the backend initializes community data, and the frontend also provides a fallback display.
-The coach's homepage retains the v6 style dashboard: only displaying statistical cards and Today's Schedule, leaving personal information/Gym crowd count/Overview in My Profile.
-The confirmation button below the coach's reserved venue usage period will no longer use the confirm pop-up window at the top of the browser.
-Add a fallback course prompt to the pop-up window of private teaching courses to avoid blank spaces.
-The management end, user end, and coach end continue to share MySQL dynamic state.


##5、 V49 Rich Demonstration Data

In order to make the defense, demonstration, and testing more complete, this version has expanded the initialization data to:

-12 members, 6 coaches;
-40 courses: 28 group courses+12 private courses;
-29 course reservation records;
-15 venues/facilities, 6 coaching venues reserved;
-18 points mall products, 5 redemption records;
-7 community updates, 6 team building teams, and multiple comments;
-Multiple complaints, consultations, and coach evaluations.

The persistent state key has been updated to 'fitlife-v49-rich database state', and new data will be automatically written when v49 is first started.

##6、 Dynamic Data Explanation

The main business data comes from backend interfaces and is persisted to MySQL:

-User, member, VIP expiration date
-Coach, course, appointment, cancellation of appointment
-Points, point based products, redemption records, inventory, and sales volume
-Community posts, replies, likes, topping, teaming up
-Check in records, consecutive check-in days, badge status
-Complaints and suggestions, coach evaluations, coach course management

`Fitlife_state 'saves the complete business status, and the following readable table is used to view the main dynamic data:

```text
fitlife_users
fitlife_coaches
fitlife_memberships
fitlife_classes
fitlife_products
fitlife_point_products
fitlife_bookings
fitlife_posts
fitlife_teams
fitlife_points
fitlife_exchanges
fitlife_facilities
fitlife_facility_bookings
fitlife_complaints
fitlife_reviews
```
