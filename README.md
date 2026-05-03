# 📚 Manageable Uni

**Manageable Uni** is a RESTful backend API built for universities, designed to fully automate student and teacher management. Built on Spring Boot and secured with JWT.

---

## 🎯 Purpose

To bring together the key stakeholders of the academic process — **students**, **teachers**, and **admins** — on a single digital platform. Each role accesses its own dashboard, grades and attendance are tracked in real time.

---

## 👥 User Roles

| Role | Permissions |
|------|-------------|
| `STUDENT` | Views own grades, attendance records, and warnings |
| `TEACHER` | Manages assigned groups, enters grades and attendance |
| `SUPER_ADMIN` | Full system control — manages all users and data |

---

## ⚡ Core Features

### 🔐 Authentication
- **Login** and **register** with email & password
- **JWT Access Token** (short-lived) + **Refresh Token** (long-lived) mechanism
- **3-step email-verified password change** flow

### 🎓 Student Dashboard
- Enrolled **group** and **specialty** information
- **Grades** per subject (attendance score, seminar, colloquiums ×3, exam, total)
- Automatic **attendance warning system** based on absence percentage

### 👨‍🏫 Teacher Dashboard
- Assigned **groups** and **subjects**
- Student list per group
- Grade and attendance entry

### 🗂️ Admin Panel
- Full CRUD for users, students, and teachers
- Group, specialty, and subject management
- Teacher–Group–Subject assignment

---

## 🏗️ Technical Overview

```
Spring Boot 4.0  ·  Java 17  ·  MySQL  ·  Flyway  ·  JWT (jjwt 0.12)
MapStruct  ·  Lombok  ·  Spring Security  ·  OpenAPI / Swagger UI
```

### Package Structure

```
com.example/
├── controller/     ← REST endpoints (13 controllers)
├── service/        ← Business logic
├── repository/     ← JPA queries
├── entity/         ← Database models
├── dto/            ← Request / Response objects
├── security/       ← JWT filter, UserDetails
├── config/         ← Spring Security configuration
├── enums/          ← Role, Department, GradeStatus, AttendanceStatus
├── exception/      ← Global error handling
└── strategy/       ← Strategy pattern implementations
```

### Entity Relationships

```
User ──┬── Student ── Group ── Specialty
       └── Teacher ── TeacherGroupSubject ──┬── Group
                                            └── Subject

Lesson ←── Attendance ──→ Student
Subject ←── Grade ──→ Student + Teacher
```

---

## 🔑 Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/login` | Login |
| `POST` | `/api/v1/auth/register` | Register |
| `POST` | `/api/v1/auth/refresh` | Refresh access token |
| `POST` | `/api/v1/auth/change-password/request` | Initiate password change |
| `GET`  | `/api/v1/dashboard/student` | Student dashboard |
| `GET`  | `/api/v1/dashboard/teacher` | Teacher dashboard |
| `GET`  | `/api/v1/grades` | Grade records |
| `GET`  | `/api/v1/attendance` | Attendance records |

> 📖 Full interactive API docs: `http://localhost:8080/swagger-ui.html`

---

## 🚀 Getting Started

> Flyway migrations run automatically — all tables will be created on first launch.

---

## 🛡️ Security

- Every request requires `Authorization: Bearer <token>` (except public endpoints)
- Short-lived access tokens are renewed via the refresh token endpoint
- Password changes require email verification before taking effect
- Soft-delete: deactivated users are marked with `isActive = false`


## Our Team

- **Ilkin Ismayilov** — https://www.linkedin.com/in/ilkin-ismayilov2/
- **Yaqut Rasulbayli** — https://www.linkedin.com/in/yagut-rasulbayli-733417401/
- **Shabnam Muradova** — https://www.linkedin.com/in/shebnem-muradova/


## Github Repository

- [Manageable-Uni Backend](https://github.com/ilkinismayilov-905/UMS)


## Trello Board

- [Manageable Uni Trello Board](https://trello.com/invite/b/69f77f6d8ccec167a5b23b53/ATTI34406a547c35788e95c006ac13f9cb412718C2BB/manageable-university)