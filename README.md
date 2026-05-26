# KL University Portal

KL University Portal is a production-style full-stack University ERP and Admission Management System built with Spring Boot, MySQL, REST APIs, JWT security, and a responsive Bootstrap 5 frontend.

## Features

- Online student admission with validation and duplicate email prevention
- JWT authentication with BCrypt password hashing
- Role-based access for `ADMIN` and `STUDENT`
- Student dashboard, profile, announcements, fee status, payment history, hostel booking, and ID card download
- Admin analytics dashboard with Chart.js, student search, course visibility, payments, and hostel occupancy statistics
- Dynamic course fee structure for CSE, AI & DS, IT, ECE, Mechanical, and Civil
- Semester fee payments with transaction history and PDF receipt generation
- Hostel room availability, booking prevention for occupied rooms, hostel/mess fee updates
- PDF fee receipts, hostel receipts, and QR-enabled student ID cards
- Email notification service for registration, payments, hostel booking, and password reset OTP
- Global exception handling and validation error responses
- Responsive glassmorphism UI with dark/light mode, dashboards, tables, toasts, and loading states

## Technology Stack

- Frontend: HTML5, CSS3, JavaScript, Bootstrap 5, Font Awesome, Chart.js
- Backend: Java 17, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate, Lombok
- Database: MySQL
- Security: JWT, BCrypt
- Documents: OpenPDF, ZXing QR codes
- Build: Maven

## Project Structure

```text
src/main/java/com/kluniversity
  config
  controller
  dto
  entity
  exception
  repository
  security
  service
  util

src/main/resources/static
  index.html
  css
  js
  pages
```

## Database Setup

Create a MySQL database:

```sql
CREATE DATABASE kl_university_portal;
```

Update `src/main/resources/application.properties` if your MySQL username or password differs:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/kl_university_portal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=
```

Hibernate creates/updates tables automatically. `data.sql` seeds departments, courses, hostels, rooms, announcements, and an admin user.

## Default Login

- Admin username: `admin`
- Admin password: `Admin@123`

Students can register from `/pages/register.html`.

## Run Locally

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

Build a deployable JAR:

```bash
mvn clean package
java -jar target/kl-university-portal-1.0.0.jar
```

## API Documentation

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`

### Students

- `GET /api/students`
- `GET /api/students/{id}`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`
- `POST /api/students/{id}/photo`

### Courses

- `GET /api/courses`
- `POST /api/courses`
- `PUT /api/courses/{id}`

### Fees

- `POST /api/fees/pay`
- `GET /api/fees/history/{regNo}`
- `GET /api/fees/payments/{regNo}`
- `GET /api/fees/receipt/{paymentId}`

### Hostel

- `GET /api/hostel/list`
- `GET /api/hostel/rooms`
- `POST /api/hostel/book`
- `GET /api/hostel/{regNo}`
- `GET /api/hostel/receipt/{regNo}`

### Announcements

- `GET /api/announcements`
- `POST /api/announcements`

### Admin

- `GET /api/admin/dashboard`

### Documents

- `GET /api/documents/id-card/{regNo}`

## Screenshots

Add screenshots of:

- Home page
- Student dashboard
- Admin analytics dashboard
- Fee payment page
- Hostel booking page

## Production Notes

- Replace `app.jwt.secret` with a strong environment-specific secret.
- Move database, mail, and upload settings to environment variables for deployment.
- Configure real SMTP credentials before enabling outbound email.
- Integrate a real payment gateway such as Razorpay before accepting real payments.
- Serve static assets from a CDN or reverse proxy for high-traffic deployments.
