# Technical Documentation - LMS Project

## Technology Stack

### Backend & Core
- **Spring Boot 4.0.5** - Enterprise framework for rapid development
- **Java 17** - Modern programming language
- **Spring Data JPA + Hibernate** - Database ORM layer
- **MySQL** - Relational database
- **Flyway** - Database version control and migrations

### Security & Authentication
- **JWT (JJWT 0.12.3)** - Stateless token-based authentication
- **Spring Security 6.0** - Role-based access control (RBAC)
- **BCrypt** - Secure password hashing (12 rounds)
- **Refresh Token Mechanism** - Token rotation for security

### Object Mapping & Code Generation
- **Lombok** - Annotation-based boilerplate generation (@Data, @Builder, @Slf4j)
- **MapStruct** - Compile-time DTO mapping optimization
- **Custom EntityToDtoMapper** - Current mapping implementation

### Email & Notifications
- **Spring Mail + Gmail SMTP** - TLS-based email delivery
- **Email Verification** - OTP/verification codes for password reset

### API & Documentation
- **SpringDoc OpenAPI 2.8.9** - Swagger UI integration
- **RESTful Architecture** - Standard API design patterns

### Infrastructure (Planned)
- **Docker & Docker Compose** - Application containerization
- **CI/CD Pipeline** - Automated build, test, and deployment
- **Monitoring Tools** - Performance metrics and health checks

### Testing
- **Spring Boot Test + JUnit 5** - Unit and integration testing
- **Spring Security Test** - Authentication testing

---

## Implemented Features

### Authentication & Authorization
- User registration with role assignment (STUDENT, TEACHER, ADMIN)
- JWT-based login with access + refresh tokens
- Three-step password change system with Gmail verification
- Role-based access control with granular permissions

### User & Academic Management
- Student enrollment and lifecycle management
- Teacher profile and department management
- Subject/course management with credits
- Group/cohort organization by specialty

### Attendance & Grading System
- Lesson-based attendance tracking (PRESENT, ABSENT, EXCUSED)
- Multi-component grading: attendance score, seminar score, continuous assessments (COL1, COL2, COL3), exam score
- Automatic total score calculation and grade status (PASS, FAIL, INCOMPLETE)
- Teacher and student grade views

### Advanced Features
- Database migrations with Flyway versioning
- Scheduled token cleanup (expired tokens deleted automatically every 30-60 minutes)
- Transaction management with ACID compliance
- Comprehensive logging for debugging
- CORS configuration for frontend integration

---

## Current Development

### Absence & Grade Management System
Currently under development to manage student absences and grade tracking:
- Students submit absence requests with justification
- Teachers/admins review and approve/reject absence requests
- Absence tracking with statistical reporting
- Integration with attendance records for comprehensive absence management

### Dashboard Implementation
Multi-role dashboard system:
- **Admin Dashboard** - System metrics, user management, and overall statistics
- **Student Dashboard** - Personal grade tracking, attendance overview, academic standing
- **Teacher Dashboard** - Class management, grade entry, and attendance marking

---

## Planned Enhancements (Future)

### Academic Records & Reports
- Transcript PDF generation with student grades and performance summary
- Student grade point average (GPA) calculation
- Performance analytics and trend analysis
- Academic standing classification

### DevOps & Deployment
- Docker containerization with multi-stage builds
- Docker Compose for local development environment
- CI/CD pipeline with automated testing and deployment
- Application monitoring and alerting infrastructure
- Load balancing for scalability

---

## Project Status

**Completion: 85%**

**Completed:**
✅ Authentication & JWT token system with refresh mechanism
✅ Password reset with three-step email verification
✅ User, student, teacher management
✅ Attendance and grade tracking
✅ Database migrations with Flyway
✅ API documentation with Swagger
✅ Comprehensive testing suite

**In Progress (15%):**
⏳ Absence & grade management system
⏳ Dashboard implementation (admin, student, teacher)
⏳ Transcript PDF generation
⏳ Docker & CI/CD pipeline
⏳ Monitoring infrastructure

**Status:** No blockers or critical issues. Architecture is solid and scalable.

---

**Last Updated:** April 2026 | **Version:** 0.0.1-SNAPSHOT

