# Database Password Update

This version changes the default local MySQL password used by the Spring Boot backend.

## Changed file

`src/main/resources/application.yml`

```yml
spring:
  datasource:
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:lnj031212}
```

## Local CMD startup

After creating the database, you can start the backend directly:

```cmd
mvn spring-boot:run
```

If your MySQL password changes later, either edit `application.yml` again or start with an environment variable:

```cmd
set MYSQL_PASSWORD=your_password
mvn spring-boot:run
```
