# System Flow — Attendance Management System

**Stack:** Spring Boot 3.3 · Java 17 · MySQL · Spring Security (JWT) · Apache POI · Gmail SMTP  
**Version:** 2.2 | Base URL: `http://localhost:8080`

---

## 1. Architecture Overview

```
Client (Mobile/Web)
       │
       ▼
Spring Security Filter (JwtAuthenticationFilter)
       │
       ▼
REST Controllers  ──→  Service Layer  ──→  Repository (JPA)  ──→  MySQL DB
       │                     │
       │                EmailService (Gmail SMTP)
       │
   ApiResponse<T>  (universal envelope for every response)
```

### Package Structure

```
com/
├── AttendanceManagementApplication.java
├── security/          JWT filter, SecurityConfig, UserDetailsService per role
├── common/            ApiResponse, EmailService
├── config/            CorsConfig, SuperAdminSeeder, TestDataSeeder
├── exception/         GlobalExceptionHandler + custom exceptions
└── Features/
    ├── SuperAdmin/    Platform-level admin who creates Admins
    ├── Admin/
    │   ├── AdminPart/ Admin login + profile
    │   ├── Building/  Physical building CRUD + Excel
    │   ├── Beacon/    BLE beacon CRUD + Excel
    │   ├── Classroom/ Classroom CRUD + Excel
    │   ├── Department/Department CRUD + Excel
    │   ├── CourseCategory/ Course category CRUD + Excel
    │   ├── Semester/  Semester CRUD + Excel
    │   ├── Section/   Section CRUD + Excel
    │   ├── Subject/   Subject CRUD
    │   ├── faculty/   Faculty CRUD (staff profiles)
    │   ├── Student/   Student CRUD + Excel + password auto-gen
    │   └── Teacher/   Promote faculty → Teacher + account creation
    ├── Teacher/       Teacher login, profile, attendance report, student list
    ├── Student/       Student login (device binding), dashboard, timetable
    ├── ModuleLeader/  Subject-level academic lead: attendance overview + reports
    ├── Attendance/    QR session management (Teacher) + student scan + admin override
    ├── FacultyAttendance/ Daily QR for faculty check-in + admin reports
    ├── Enrollment/    Student enrollment into semesters/sections/subjects
    └── Timetable/     Recurring weekly slots + one-off exceptions
```

---

## 2. Authentication & Security

### JWT Flow

1. Client POSTs credentials to a public `/api/auth/**` endpoint.
2. Service validates email + password → generates JWT via `JwtUtils`.
3. JWT contains: `sub` (email), `role` (e.g. `ROLE_ADMIN`), `iat`, `exp`.
4. Client sends `Authorization: Bearer <token>` on every subsequent request.
5. `JwtAuthenticationFilter` validates the token and sets `SecurityContext`.

### Roles & Route Guards

| Role | Prefix | How created |
|---|---|---|
| `ROLE_SUPER_ADMIN` | `/api/superadmin/**` | Seeded at startup via `SuperAdminSeeder` |
| `ROLE_ADMIN` | `/api/admin/**` | SuperAdmin creates via API |
| `ROLE_TEACHER` | `/api/teacher/**`, `/api/faculty/**` | Admin promotes Faculty → Teacher |
| `ROLE_STUDENT` | `/api/student/**` | Admin creates student, account auto-generated |
| `ROLE_MODULE_LEADER` | `/api/module-leader/**` | Admin creates with subject assignment |

Public (no JWT):
- `POST /api/auth/superadmin/login`
- `POST /api/auth/admin/login`
- `POST /api/auth/teacher/login`
- `POST /api/auth/student/login`
- `POST /api/auth/module-leader/login`

### JWT Configuration
```properties
app.jwt.secret=<hex-encoded-256-bit-key>
app.jwt.expiration-ms=86400000   # 24 hours
```

### UserDetails Services
`CombinedUserDetailsService` chains: SuperAdmin → Admin → Teacher → Student → ModuleLeader.  
Each role has its own `UserDetailsService` loading from its respective account table.

---

## 3. Universal Response Envelope

Every endpoint returns `ApiResponse<T>`:

**Success:**
```json
{
  "timestamp": "2026-08-08T10:00:00",
  "success": true,
  "status": 200,
  "message": "Students retrieved successfully",
  "data": [ ... ]
}
```

**Validation Error (400):**
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "errors": { "email": "Invalid email format", "password": "must not be blank" }
}
```

**App Error (404 / 409 / 401 / 403):**
```json
{
  "success": false,
  "status": 404,
  "message": "Student not found with id = 'abc-123'"
}
```

### Exception Hierarchy

| Exception | HTTP Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` | 409 |
| `UnauthorizedException` | 401 |
| `ExcelImportException` | 422 |
| `AuthorizationDeniedException` (Spring) | 403 |
| `MethodArgumentNotValidException` (Spring) | 400 |
| Any other `Exception` | 500 |

---

## 4. Core Domain Model

### Entity Relationships

```
SuperAdmin  ──creates──▶  Admin  ──manages everything below──▶
                             │
              ┌──────────────┼─────────────────────────────┐
              │              │                             │
           Building      Department                    Faculty
              │              │                             │
           Classroom      Section ◀──── Semester      Teacher (account)
              │              │              │              │
           Beacon         Subject       Enrollment      TimetableSlot
                             │              │              │
                        ModuleLeader   Student (account)  │
                                            │             │
                                    AttendanceRecord ◀────┘
                                       (QrToken)
```

### Key Entities

| Entity | Table | Notes |
|---|---|---|
| `SuperAdmin` | `super_admins` | Platform root, seeded at startup |
| `Admin` | `admins` | Tenant admin, status: ACTIVE/INACTIVE/SUSPENDED |
| `Faculty` | `faculties` | Staff profile (name, dept, designation) |
| `Teacher` | `teachers` | Faculty with login; created by Admin from Faculty |
| `TeacherAccount` | `teacher_accounts` | BCrypt-hashed credentials for Teacher |
| `Student` | `students` | Student profile with family contacts |
| `StudentAccount` | `student_accounts` | BCrypt credentials + deviceId + phoneRegistered |
| `ModuleLeader` | `module_leaders` | One per Subject; created by Admin |
| `ModuleLeaderAccount` | `module_leader_accounts` | Login credentials |
| `Building` | `buildings` | Physical campus building |
| `Classroom` | `classrooms` | Room inside a building; type: LECTURE/TUTORIAL/PRACTICAL |
| `Beacon` | `beacons` | BLE beacon inside a classroom (uuid, major, minor) |
| `Department` | `departments` | Academic department (name + code) |
| `CourseCategory` | `course_categories` | Subject category grouping |
| `Semester` | `semesters` | Academic term with date range |
| `Section` | `sections` | Student group within a semester |
| `Subject` | `subjects` | Course/module |
| `Enrollment` | `enrollments` | Student ↔ Semester + Section + Subjects; status: ACTIVE/PROMOTED/DROPPED |
| `TimetableSlot` | `timetable_slots` | Recurring weekly class (teacher, subject, room, section, day, time) |
| `TimetableException` | `timetable_exceptions` | One-off override: CANCELLED/RESCHEDULED/SUBSTITUTED |
| `QrToken` | `qr_tokens` | Short-lived rotating token for one live class session |
| `AttendanceRecord` | `attendance_records` | PRESENT/ABSENT/LATE per student per slot per date |
| `FacultyDailyQr` | `faculty_daily_qrs` | One QR per day for faculty check-in |
| `FacultyAttendanceRecord` | `faculty_attendance_records` | Faculty PRESENT/ABSENT/LATE/LEAVE per date |
| `DeviceChangeLog` | `device_change_logs` | Audit trail when student logs in from new device |

---

## 5. System Flows

### 5.1 Setup Flow (Admin Onboarding)

```
SuperAdmin login
    │
    ▼
Create Admin (POST /api/superadmin/admins)
    │
    ▼
Admin login
    │
    ├──▶ Create Department
    ├──▶ Create Building → Classroom → Beacon (assign beacon to classroom)
    ├──▶ Create CourseCategory
    ├──▶ Create Semester
    ├──▶ Create Section (linked to Semester)
    ├──▶ Create Subject (linked to CourseCategory + Department)
    ├──▶ Create Faculty (personal profile)
    ├──▶ Register Teacher from Faculty → auto-creates TeacherAccount → email sent
    ├──▶ Create Student → auto-creates StudentAccount → generatedPassword returned once
    ├──▶ Create ModuleLeader (assign to Subject) → auto-creates account → email sent
    ├──▶ Enroll Student (Semester + Section + Subjects)
    └──▶ Create Timetable Slots (Teacher + Subject + Classroom + Section + day/time)
```

### 5.2 Student Attendance Flow (QR + BLE Beacon)

```
Teacher (mobile/browser)                  Student (mobile app)
        │                                         │
1. GET /api/teacher/timetable/today               │
        │                                         │
2. POST /api/teacher/qr/{slotId}/start            │
   → QrToken created (ACTIVE, expires in N min)   │
   → QR URL: http://localhost:8080/attend?token=X │
        │                                         │
3. Display QR on screen/projector                 │
        │                                         │
4. Poll GET /api/teacher/qr/{slotId}/current      │
   every 30s — token auto-rotates server-side     │
        │                                         │
        │         Student scans QR (camera)        │
        │                   │                     │
        │         5. POST /api/student/attendance/scan
        │             body: { qrToken, deviceId, beaconId }
        │                   │
        │         6 validations:
        │           ✓ QR valid + not expired
        │           ✓ deviceId matches registered phone
        │           ✓ beaconId matches classroom beacon
        │           ✓ student's section matches slot's section
        │           ✓ student enrolled in the subject
        │           ✓ not already marked today
        │                   │
        │         → AttendanceRecord (PRESENT) created
        │
7. POST /api/teacher/qr/{slotId}/end?date=2026-08-08
   → QrToken status → REVOKED
   → All enrolled students who did NOT scan → AttendanceRecord (ABSENT)
        │
8. GET /api/teacher/attendance/report?slotId=...&date=...
   → See per-student PRESENT/ABSENT + overall %
```

### 5.3 Student Login & Device Binding Flow

```
Student opens app (first time)
    │
    ├── On non-mobile browser → 403 Forbidden (phone-first rule)
    │
    └── On mobile browser
            │
            ├── Headers: User-Agent (auto), X-Device-Id (UUID from localStorage)
            │
            ├── If first login → deviceId saved to StudentAccount.deviceId
            │                    phoneRegistered = true
            │                    response: deviceJustRegistered = true
            │
            └── If deviceId changed → login succeeds
                                      DeviceChangeLog created
                                      Admin receives email alert
                                      response: deviceChanged = true

Admin can:
    GET  /api/admin/devices/alerts          → unacknowledged alerts
    GET  /api/admin/devices/alerts/all      → all alerts
    GET  /api/admin/devices/alerts/student/{id}
    PUT  /api/admin/devices/alerts/{id}/acknowledge
    DELETE /api/admin/devices/{studentId}/reset  → clear device binding
```

### 5.4 Faculty Attendance Flow (Daily QR + WiFi Check)

```
Admin
    │
    ▼
POST /api/admin/faculty-attendance/qr/generate?date=2026-08-08
    → FacultyDailyQr created for the day
    │
    ▼
Display QR on entrance screen / projector

Faculty (Teacher role)
    │
    ▼
GET  /api/faculty/attendance/qr/today    → see today's QR tokenValue
    │
    ▼
POST /api/faculty/attendance/scan
    body: { "qrToken": "<tokenValue>" }
    headers: real IP extracted from X-Forwarded-For / RemoteAddr
    │
    Validations:
      ✓ QR exists and not expired/revoked
      ✓ Faculty is on college WiFi (CIDR range check via app.faculty.allowed-network)
      ✓ Not already marked today
    │
    ▼
FacultyAttendanceRecord (PRESENT) created

Admin reports:
    GET /api/admin/faculty-attendance/report?date=2026-08-08
    GET /api/admin/faculty-attendance/report/department?departmentId=...&date=...
    GET /api/admin/faculty-attendance/history/{facultyId}?from=...&to=...
    PUT /api/admin/faculty-attendance/override  → PRESENT | ABSENT | LATE | LEAVE
```

### 5.5 Timetable & Exception Flow

```
Admin creates base weekly slots:
    POST /api/admin/timetable/slots
    → Clash checks: teacher, classroom, section cannot overlap same day/time

Teacher views:
    GET /api/teacher/timetable/today   → resolved slots for today
    GET /api/teacher/timetable/week?weekStart=2026-08-10

For one-off changes on a specific date:
    POST /api/admin/timetable/exceptions
    status = CANCELLED    → class is off that day
    status = RESCHEDULED  → different time or room on that date
    status = SUBSTITUTED  → different teacher on that date

Resolved slot = base slot merged with exception (if any) for that date.
```

### 5.6 Student Promotion Flow

```
Admin
    │
    ├── Single promote:
    │   POST /api/admin/enrollments/{id}/promote
    │       body: { targetSemesterId, targetSectionId, subjectIds }
    │   → Current enrollment status → PROMOTED (promotedAt set)
    │   → New ACTIVE enrollment created in target semester/section
    │
    └── Bulk promote:
        POST /api/admin/enrollments/bulk-promote
            body: { sourceSectionId, sourceSemesterId, targetSemesterId, targetSectionId, subjectIds }
        → All ACTIVE students in source section/semester are promoted
        → Students already in target semester are skipped (not an error)
```

---

## 6. Excel Import / Export

Most admin entities support bulk operations:

| Entity | Export | Template | Import |
|---|---|---|---|
| Building | `GET /excel/export` | `GET /excel/template` | `POST /excel/import` |
| Beacon | ✓ | ✓ | ✓ |
| Classroom | ✓ | ✓ | ✓ |
| Department | ✓ | ✓ | ✓ |
| CourseCategory | ✓ | ✓ | ✓ |
| Semester | ✓ | ✓ | ✓ |
| Section | ✓ | ✓ | ✓ |
| Student | ✓ | ✓ | ✓ |
| Timetable Slots | ✓ | ✓ | ✓ |

- Export returns binary `.xlsx` (not wrapped in `ApiResponse`).
- Import is `multipart/form-data` with `file` field.
- Bad data returns `422 Unprocessable Entity` with row-level error message.
- Duplicate data returns `409 Conflict`.

---

## 7. Password & Account Auto-Generation

| Entity | How password is created | Delivery |
|---|---|---|
| Admin | Set manually by SuperAdmin in `CreateAdminRequest` | None (known at creation) |
| Teacher | Auto-generated by `TeacherPasswordGenerator` | Shown once in `TeacherDTO.generatedPassword` + emailed |
| Student | Auto-generated by `StudentPasswordGenerator` | Shown once in `StudentDTO.generatedPassword` |
| ModuleLeader | Auto-generated | Shown once in response + emailed to module leader |

All passwords are BCrypt-hashed before storage. Plain text is never persisted.

---

## 8. Email Notifications

`EmailService` (Gmail SMTP via Spring Mail) sends emails for:

| Event | Recipient |
|---|---|
| Teacher account created | Teacher's email |
| ModuleLeader account created | ModuleLeader's email |
| Student enrolled | Student's email (welcome email) |
| Student device change detected | Admin email |

Configuration:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<gmail>
spring.mail.password=<app-password>    # Gmail App Password (not login password)
```

---

## 9. Dynamic QR Token Rotation

```properties
app.qr.token-validity-minutes=2          # QR expires after 2 minutes
app.qr.base-url=http://localhost:8080/attend
app.qr.rotation-threshold-seconds=30     # auto-rotate when < 30s remain
app.qr.rotation-check-interval-ms=30000  # scheduler runs every 30s
```

**Flow:**
1. Teacher starts session → `QrToken` created, `sessionId` generated (UUID shared across all rotations).
2. Scheduler checks every 30s → if active token expires within 30s → create new token (same `sessionId`, `rotationCount++`), mark old as `EXPIRED`.
3. Teacher polls `GET /api/teacher/qr/{slotId}/current` → always gets the live token.
4. Teacher can force-rotate: `POST /api/teacher/qr/{slotId}/rotate`.
5. Session end: `POST /api/teacher/qr/{slotId}/end` → token `REVOKED`, absentees marked.

---

## 10. CORS Configuration

`CorsConfig` allows cross-origin requests (configured in `CorsConfig.java`). In development, typically allows `http://localhost:3000` or `*`. Update for production domains.

---

## 11. Database Setup

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update   # auto-creates/updates tables
```

Database `attendance_db` is created automatically on first run.  
Schema is managed by Hibernate DDL (`update` mode — safe for development, use `validate` in production).

---

## 12. Seeders

| Seeder | Trigger | What it does |
|---|---|---|
| `SuperAdminSeeder` | App startup (`@PostConstruct`) | Creates the default SuperAdmin if none exists |
| `TestDataSeeder` | App startup | Seeds sample data for development/testing |

Default SuperAdmin credentials (configurable in `application.properties`):
```
email:    superadmin@attendance.com
password: SuperAdmin@123
```

---

## 13. Module Leader

A Module Leader is assigned to exactly one `Subject`. They can:
- View all timetable slots for their subject (across all sections and teachers)
- View attendance report for any of those slots on any date
- View full slot history across all dates
- See all students enrolled in their subject with attendance % 
- See all teachers who teach their subject

The Module Leader cannot modify attendance — read-only oversight role.

---

## 14. Error Codes Quick Reference

| Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Resource created |
| 400 | Validation error (check `errors` field) |
| 401 | Not authenticated / bad credentials |
| 403 | Not authorized (wrong role or phone-first rule) |
| 404 | Resource not found |
| 409 | Duplicate resource (email, clash, already enrolled, etc.) |
| 410 | QR expired or revoked |
| 422 | Excel import data error |
| 500 | Unexpected server error |
