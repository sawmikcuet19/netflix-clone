# Netflix Clone

A full-stack video streaming application built with Spring Boot and Angular.

> **Note:** This project was created by following a YouTube tutorial as a personal learning exercise. This is an individual project, not a team effort.

## 🚀 Technologies Used

### Backend (netflix-clone/)
- **Java 21**
- **Spring Boot 3.4.1**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **JavaMailSender** for email verification
- **Maven** build tool

### Frontend (netflix-clone-frontend/)
- **Angular 21.1.0**
- **TypeScript 5.9.2**
- **Angular Material** for UI components
- **RxJS** for reactive programming

## 📋 Features

- 🔐 User authentication with JWT tokens
- 📧 Email verification for new accounts
- 🎬 Video streaming with partial content support
- 👤 User roles (ADMIN, USER)
- 🎛️ Admin dashboard for video management
- 🔍 Video search functionality
- 📁 File upload (videos and thumbnails)
- 📱 Responsive design

## 🛠️ Setup Instructions

### Prerequisites
- Java 21
- Node.js 18+
- MySQL Server
- Maven

### Backend Setup
1. Create MySQL database `netflix_clone_db`
2. Configure `application.properties` with your database credentials
3. Set environment variables for Gmail SMTP:
   - `GMAIL_NAME`
   - `GMAIL_PASSWORD`
4. Run: `mvn spring-boot:run`

### Frontend Setup
1. Navigate to `netflix-clone-frontend/`
2. Run: `npm install`
3. Run: `ng serve`

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.
