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
- Security: JWT
- Build: Maven
