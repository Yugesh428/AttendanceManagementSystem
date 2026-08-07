# Attendance Management System — Architecture & System Flow

---

## 1. What This System Is

A **Bluetooth Beacon + Dynamic QR based attendance management system** for educational institutions.

Students mark attendance by scanning a time-limited QR code displayed by their teacher. The system validates three things simultaneously:
- The student is physically present (beacon signal from the classroom)
- It is their registered phone (device fingerprint)
- The QR is live and belongs to the correct class period

Everything runs on a **web browser** — no native app required. Students use their phone browser. Teachers and admins use any browser.

---

## 2. User Roles

```
SuperAdmin
    │── Creates and manages Admins
    │── Has full system access
    └── Seeded automatically on first startup

Admin
    │── Manages all institutional data
    │── Enrolls students, manages timetable, generates reports
    └── Cannot access SuperAdmin operations

Teacher
    │── Sees their own timetable
    │── Generates QR codes for their class periods
    └── Views attendance records for their classes

Student
    │── Logs in FIRST TIME from phone (mandatory)
    │── After phone registration — can access dashboard from any device
    └── Marks attendance by scanning QR from phone browser
```

---

## 3. Full System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                 │
│                                                                     │
│  Phone Browser (Student)    Any Browser (Teacher / Admin)           │
│  - First login & device     - Dashboard                             │
│    registration             - Timetable management                  │
│  - QR scan (attendance)     - QR generation                         │
│  - Dashboard (after reg)    - Reports & enrollment                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │  HTTPS + JWT
┌──────────────────────────────▼──────────────────────────────────────┐
│                     SPRING BOOT BACKEND                             │
│                                                                     │
│  Security Layer                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  JWT Filter → CombinedUserDetailsService                     │  │
│  │  Roles: SUPER_ADMIN | ADMIN | TEACHER | STUDENT              │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  Feature Modules                                                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │  SuperAdmin │ │    Admin    │ │   Teacher   │ │   Student   │  │
│  │  Auth+CRUD  │ │  Auth+CRUD  │ │  Auth+CRUD  │ │  Auth+Device│  │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘  │
│                                                                     │
│  Core Academic Modules (Admin manages all)                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐  │
│  │Department│ │ Faculty  │ │ Building │ │Classroom │ │ Beacon  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └─────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐  │
│  │ Semester │ │ Section  │ │ Subject  │ │ Student  │ │Teacher  │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └─────────┘  │
│                                                                     │
│  Enrollment Module                                                  │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Enrollment (Student → Semester + Section + Subjects)       │   │
│  │  Promote (single / bulk)  │  Drop  │  Subject update        │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Timetable Module                                                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  TimetableSlot (Teacher + Subject + Classroom + Section)    │   │
│  │  TimetableException (cancellation / reschedule / sub)       │   │
│  │  ResolvedSchedule (base slot merged with exceptions)        │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Attendance Module (to be built)                                    │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  QrToken  │  AttendanceRecord  │  DeviceChangeLog           │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Common                                                             │
│  ┌──────────┐ ┌──────────────────┐ ┌──────────────────────────┐    │
│  │ApiResponse│ │  EmailService    │ │  GlobalExceptionHandler  │    │
│  └──────────┘ └──────────────────┘ └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                         MySQL DATABASE                              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. Data Model Relationships

```
SuperAdmin
    └── creates ──► Admin

Admin
    ├── manages ──► Department
    │                   └── has ──► Faculty
    │                               └── promoted to ──► Teacher
    │
    ├── manages ──► Building
    │                   └── has ──► Classroom
    │                               └── has ──► Beacon (uuid/major/minor)
    │
    ├── manages ──► CourseCategory
    │                   └── has ──► Subject
    │
    ├── manages ──► Semester
    │                   └── has ──► Section (A, B, C)
    │
    ├── manages ──► Student ──► StudentAccount (login credentials + deviceId)
    │
    └── manages ──► Enrollment
                        ├── student      FK → Student
                        ├── semester     FK → Semester
                        ├── section      FK → Section
                        ├── subjects     ManyToMany → Subject
                        └── status       ACTIVE / PROMOTED / DROPPED

TimetableSlot
    ├── teacher    FK → Teacher
    ├── subject    FK → Subject
    ├── classroom  FK → Classroom
    ├── section    FK → Section     ← links class period to student group
    ├── dayOfWeek  MONDAY–SATURDAY
    ├── startTime / endTime
    └── effectiveFrom / effectiveTo

TimetableException (one-off override on a specific date)
    ├── slot           FK → TimetableSlot
    ├── exceptionDate
    ├── status         CANCELLED / RESCHEDULED / SUBSTITUTED
    ├── overrideTime / overrideClassroom / substituteTeacher
    └── reason

QrToken (to be built)
    ├── slot        FK → TimetableSlot
    ├── token       random UUID (encoded in QR)
    ├── expiresAt   now + 5 minutes
    ├── generatedBy FK → Teacher
    └── revoked     boolean

AttendanceRecord (to be built)
    ├── student     FK → Student
    ├── slot        FK → TimetableSlot
    ├── date        the calendar date this attendance was marked
    ├── status      PRESENT / ABSENT / LATE
    ├── deviceId    the device fingerprint used at scan time
    ├── beaconId    the beacon detected at scan time
    └── markedAt    timestamp

DeviceChangeLog (to be built)
    ├── student          FK → Student
    ├── oldDeviceId
    ├── newDeviceId
    ├── detectedAt
    └── acknowledgedByAdmin  boolean
```

---

## 5. Authentication Flow

### SuperAdmin Login
```
POST /api/auth/superadmin/login  { email, password }
    │
    ▼
CombinedUserDetailsService checks SuperAdmin table
    │
    ▼
JWT issued with role = SUPER_ADMIN
    │
    ▼
Access: /api/superadmin/** only
```

### Admin Login
```
POST /api/auth/admin/login  { email, password }
    │
    ▼
CombinedUserDetailsService checks Admin table
    │
    ▼
JWT issued with role = ADMIN
    │
    ▼
Access: /api/admin/** only
```

### Teacher Login
```
POST /api/auth/teacher/login  { email, password }
    │
    ▼
CombinedUserDetailsService checks TeacherAccount table
    │
    ▼
JWT issued with role = TEACHER
    │
    ▼
Access: /api/teacher/** only
```

### Student Login (Phone-First Rule)
```
POST /api/auth/student/login  { email, password }
    Header: X-Device-Id: <fingerprint>  ← generated by browser, stored in localStorage
    │
    ▼
Check User-Agent header
    │
    ├── If mobile browser AND phoneRegistered = false
    │       → Save deviceId to StudentAccount
    │       → Set phoneRegistered = true
    │       → Issue JWT with role = STUDENT
    │
    ├── If mobile browser AND phoneRegistered = true
    │       → Check deviceId matches stored deviceId
    │       ├── Match   → Issue JWT normally
    │       └── Mismatch → Issue JWT BUT log DeviceChangeLog
    │                       AND email admin alert (async)
    │
    └── If NOT mobile browser AND phoneRegistered = false
            → REJECT 403: "You must log in from your phone first"

After phone registration:
    Student can log in from laptop/desktop (dashboard only)
    Laptop login: phoneRegistered = true → allow JWT
    Laptop CANNOT mark attendance (no QR scan path on desktop flow)
```

---

## 6. Enrollment Flow

```
Admin creates Student
    │
    ├── StudentAccount auto-created (hashed password)
    └── Email sent to student with login credentials (async)

Admin enrolls Student
    POST /api/admin/enrollments
    { studentId, semesterId, sectionId, subjectIds[] }
    │
    ├── Validates: student exists, semester exists, section exists
    ├── Validates: subjects all exist
    ├── Checks: student not already enrolled in this semester
    ├── Creates Enrollment record (status = ACTIVE)
    └── Sends enrollment confirmation email to student (async)
        → "You are enrolled in Semester X, Section A, Subjects: ..."

Admin promotes student (end of semester)
    POST /api/admin/enrollments/{id}/promote
    { targetSemesterId, targetSectionId, subjectIds[] }
    │
    ├── Current enrollment → status = PROMOTED, promotedAt = now
    └── New enrollment created → status = ACTIVE in new semester/section

Admin bulk promotes entire section
    POST /api/admin/enrollments/bulk-promote
    { sourceSectionId, sourceSemesterId, targetSemesterId, targetSectionId, subjectIds[] }
    │
    ├── Finds all ACTIVE enrollments in source section + semester
    ├── For each: marks old as PROMOTED, creates new ACTIVE enrollment
    └── Students already in target semester are skipped (not an error)

Admin drops student
    PUT /api/admin/enrollments/{id}/drop?reason=...
    → status = DROPPED
```

---

## 7. Timetable Flow

```
Admin creates TimetableSlot
    { teacherId, subjectId, classroomId, sectionId, dayOfWeek, startTime, endTime, effectiveFrom }
    │
    ├── Clash check: teacher not double-booked at same time
    ├── Clash check: classroom not double-booked at same time
    └── Clash check: section not double-booked at same time

Admin creates TimetableException (one-off override)
    { slotId, exceptionDate, status, reason, ... }
    status options:
        CANCELLED    → class is off on this date
        RESCHEDULED  → different time or room on this date
        SUBSTITUTED  → different teacher covers on this date

Teacher views their schedule
    GET /api/teacher/timetable/today
    GET /api/teacher/timetable/week?weekStart=2026-08-10
    │
    └── Resolution algorithm:
        For each date in range:
            For each base slot on that day:
                Does a TimetableException exist for (slot, date)?
                    YES → apply exception values (show CANCELLED / new time / sub teacher)
                    NO  → show base slot as ACTIVE
```

---

## 8. QR + Attendance Flow (to be built — design finalised)

```
TEACHER SIDE
    Teacher opens their timetable → selects current live slot
    Clicks "Generate QR"
        │
        ├── Server creates QrToken:
        │       { slotId, token (UUID), expiresAt = now+5min, generatedBy = teacher }
        └── QR is displayed on teacher's screen / projected
            QR encodes: https://yourdomain.com/attend?token=<UUID>

STUDENT SIDE
    Student opens phone browser → camera scans QR → lands on attendance page
    Browser reads:
        - qrToken from URL
        - deviceId from localStorage (set on first phone login)
        - beaconId from BLE scan OR nearby beacon detection

    POST /api/student/attendance/scan
    { qrToken, deviceId, beaconId }
        │
        ├── Validation 1: QrToken exists and not expired
        ├── Validation 2: QrToken not revoked
        ├── Validation 3: deviceId matches StudentAccount.deviceId
        │                  → if mismatch: log DeviceChangeLog, notify admin
        ├── Validation 4: beaconId matches Classroom's assigned Beacon
        │                  → proves student is physically in the room
        ├── Validation 5: student's ACTIVE enrollment section matches slot's section
        │                  → proves student belongs to this class
        ├── Validation 6: no duplicate attendance record for (student, slot, date)
        │
        └── All pass → AttendanceRecord saved (status = PRESENT)
                    → Response: "Attendance marked successfully"

ADMIN SIDE
    GET /api/admin/attendance/reports?sectionId=...&date=...
        └── Shows: who was PRESENT / ABSENT, attendance percentage

    GET /api/admin/qr/active
        └── Shows: all live QrTokens currently active

    DELETE /api/admin/qr/{id}/revoke
        └── Marks QrToken as revoked (attendance scan will fail)
```

---

## 9. Device Binding (Soft Binding)

```
CONCEPT
    One registered phone per student.
    Device is NOT a hard lock — student can still log in from new device.
    But admin is alerted every time a device mismatch is detected.

REGISTRATION
    First mobile login → deviceId captured → stored in StudentAccount
    phoneRegistered flag set to true

MISMATCH HANDLING
    Student logs in from different deviceId:
        1. JWT issued normally (attendance still allowed)
        2. DeviceChangeLog row created:
               { studentId, oldDeviceId, newDeviceId, detectedAt, acknowledged=false }
        3. Admin gets email alert (async, never blocks login)

ADMIN ACTIONS
    GET  /api/admin/devices/alerts          → list unacknowledged device changes
    PUT  /api/admin/devices/{studentId}/reset  → clear deviceId (student must re-register)
    PUT  /api/admin/devices/alerts/{id}/acknowledge → mark alert as reviewed
```

---

## 10. Email Notifications

All emails are sent **asynchronously** — they never delay or fail an API response.

| Trigger | Email sent to | Content |
|---|---|---|
| Student account created | Student | Login credentials (username + password) |
| Student enrolled | Student | Semester, section, subject list confirmation |
| Student device mismatch | Admin | Alert with old/new device IDs and student name |

SMTP: Gmail with App Password (configured in `application.properties`)

---

## 11. Project Package Structure

```
com/
├── AttendanceManagementApplication.java    ← main + @EnableAsync
├── common/
│   ├── ApiResponse.java                    ← universal response envelope
│   └── EmailService.java                   ← async email sender
├── config/
│   └── SuperAdminSeeder.java               ← seeds superadmin on startup
├── exception/
│   ├── AppException.java
│   ├── DuplicateResourceException.java     ← 409
│   ├── ResourceNotFoundException.java      ← 404
│   ├── ExcelImportException.java           ← 422
│   ├── UnauthorizedException.java          ← 401
│   └── GlobalExceptionHandler.java
├── security/
│   ├── SecurityConfig.java                 ← JWT filter chain, roles, BCrypt
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── CombinedUserDetailsService.java     ← loads SuperAdmin/Admin/Teacher/Student
└── Features/
    ├── SuperAdmin/                          ← superadmin auth + admin management
    ├── Admin/
    │   ├── AdminPart/                       ← admin auth + profile
    │   ├── Department/
    │   ├── faculty/
    │   ├── Building/
    │   ├── Classroom/
    │   ├── Beacon/
    │   ├── CourseCategory/
    │   ├── Subject/
    │   ├── Semester/
    │   ├── Section/
    │   ├── Student/                         ← registration + account + email
    │   └── Teacher/                         ← promote faculty → teacher
    ├── Teacher/                             ← teacher auth + profile
    ├── Enrollment/                          ← enroll + promote + drop + bulk
    ├── Timetable/                           ← slots + exceptions + resolved view
    └── Attendance/  (to be built)
        ├── QrToken/
        ├── AttendanceRecord/
        └── DeviceChangeLog/
```

---

## 12. Build Progress

| Phase | Status |
|---|---|
| SuperAdmin + Admin Auth | ✅ Complete |
| Teacher Auth + Management | ✅ Complete |
| Department / Faculty / Building / Classroom | ✅ Complete |
| Beacon | ✅ Complete |
| CourseCategory / Subject | ✅ Complete |
| Semester / Section | ✅ Complete |
| Student Registration + Email | ✅ Complete |
| Enrollment + Promotion + Bulk Promote | ✅ Complete |
| Timetable Slots + Exceptions + Resolved View | ✅ Complete |
| Student Auth + Phone-First Device Registration | 🔲 Next |
| QrToken — Generate / Revoke / Validate | 🔲 Planned |
| AttendanceRecord — Scan + Validation | 🔲 Planned |
| DeviceChangeLog + Admin Alerts | 🔲 Planned |
| Admin Attendance Reports | 🔲 Planned |
| Admin QR Management | 🔲 Planned |
| Student Dashboard (own attendance %) | 🔲 Planned |
