# API Documentation — Attendance Management System

**Base URL:** `http://localhost:8080`  
**Auth:** `Authorization: Bearer <JWT>` (except public login endpoints)  
**Content-Type:** `application/json` (except Excel import: `multipart/form-data`)

All responses use the envelope:
```json
{ "timestamp": "...", "success": true/false, "status": 200, "message": "...", "data": {...} }
```

---

## Authentication Endpoints (Public — No JWT Required)

### POST /api/auth/superadmin/login
Login as SuperAdmin.

**Request:**
```json
{ "email": "superadmin@attendance.com", "password": "SuperAdmin@123" }
```
**Response 200:**
```json
{
  "token": "<jwt>", "tokenType": "Bearer",
  "id": "<uuid>", "firstName": "Super", "lastName": "Admin",
  "email": "superadmin@attendance.com", "role": "ROLE_SUPER_ADMIN"
}
```
**Errors:** 401 Invalid credentials, 401 Account inactive

---

### POST /api/auth/admin/login
Login as Admin.

**Request:**
```json
{ "email": "admin@org.com", "password": "Admin@123" }
```
**Response 200:**
```json
{
  "token": "<jwt>", "tokenType": "Bearer",
  "id": "<uuid>", "firstName": "...", "lastName": "...",
  "email": "...", "tenantName": "...", "status": "ACTIVE", "role": "ROLE_ADMIN"
}
```
**Errors:** 401 Invalid credentials, 401 Account is inactive/suspended

---

### POST /api/auth/teacher/login
Login as Teacher.

**Request:**
```json
{ "email": "teacher@institution.com", "password": "john@3210" }
```
**Response 200:** JWT + teacher profile  
**Errors:** 401 Invalid credentials, 401 Account inactive

---

### POST /api/auth/student/login
Login as Student. Enforces phone-first rule and device binding.

**Headers:** `User-Agent` (auto), `X-Device-Id: <uuid-from-localStorage>`

**Request:**
```json
{ "email": "student@college.com", "password": "auto-generated-password" }
```
**Response 200:**
```json
{
  "token": "<jwt>", "id": "<uuid>", "firstName": "...", "lastName": "...",
  "email": "...", "phoneRegistered": true,
  "deviceJustRegistered": false, "deviceChanged": false
}
```
**Errors:** 401 Bad credentials, 403 Must log in from phone first

---

### POST /api/auth/module-leader/login
Login as Module Leader.

**Request:**
```json
{ "email": "leader@institution.com", "password": "auto-generated" }
```
**Response 200:** JWT + module leader profile + subject info  
**Errors:** 401 Invalid credentials

---

## SuperAdmin Endpoints — `ROLE_SUPER_ADMIN`

### POST /api/superadmin/admins
Create a new Admin.

**Request:**
```json
{
  "firstName": "John", "lastName": "Doe",
  "email": "admin@school.com", "password": "Admin@123",
  "phoneNumber": "+923001234567", "tenantName": "ABC University",
  "organizationAddress": "123 Main St", "organizationCity": "Karachi",
  "organizationCountry": "Pakistan"
}
```
**Validation:** Password must have uppercase, lowercase, digit, special char (min 8).  
**Response 201:** Full AdminResponse  
**Errors:** 409 Email already exists

---

### GET /api/superadmin/admins
List all Admins.

**Response 200:** Array of AdminResponse objects

---

## Admin — Profile

### GET /api/admin/me
Get own profile. `ROLE_ADMIN`

**Response 200:**
```json
{
  "id": "<uuid>", "firstName": "...", "lastName": "...", "email": "...",
  "phoneNumber": "...", "tenantName": "...", "organizationAddress": "...",
  "organizationCity": "...", "organizationCountry": "...",
  "status": "ACTIVE", "createdAt": "...", "createdBySuperAdminId": "<uuid>"
}
```

---

## Admin — Buildings

Base: `/api/admin/buildings` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create building `{ "name": "Block A", "location": "North Campus" }` |
| GET | `/` | List all buildings |
| GET | `/{id}` | Get building by ID |
| PUT | `/{id}` | Update building |
| DELETE | `/{id}` | Delete building |
| GET | `/excel/export` | Download all as .xlsx |
| GET | `/excel/template` | Download import template |
| POST | `/excel/import` | Bulk import from .xlsx (multipart) |

**BuildingDTO fields:** `id`, `name`, `location`, `createdAt`

---

## Admin — Classrooms

Base: `/api/admin/classrooms` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create classroom |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export .xlsx |
| GET | `/excel/template` | Import template |
| POST | `/excel/import` | Bulk import |

**ClassroomDTO fields:** `id`, `name`, `classType` (LECTURE\|TUTORIAL\|PRACTICAL), `buildingId`, `buildingName`, `createdAt`

---

## Admin — Beacons

Base: `/api/admin/beacons` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create beacon |
| GET | `/{id}` | Get by ID |
| GET | `/classroom/{classroomId}` | All beacons in a classroom |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export .xlsx |
| GET | `/excel/template` | Import template (UUID, Major, Minor, ClassroomId required) |
| POST | `/excel/import` | Bulk import |

**BeaconDTO fields:** `id`, `uuid`, `major`, `minor`, `classroomId`, `classroomName`, `createdAt`

---

## Admin — Departments

Base: `/api/admin/departments` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create `{ "name": "CS", "code": "CS-01", "description": "..." }` |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export |
| GET | `/excel/template` | Template |
| POST | `/excel/import` | Bulk import |

---

## Admin — Course Categories

Base: `/api/admin/course-categories` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export |
| GET | `/excel/template` | Template |
| POST | `/excel/import` | Bulk import |

---

## Admin — Semesters

Base: `/api/admin/semesters` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create semester |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export |
| GET | `/excel/template` | Template |
| POST | `/excel/import` | Bulk import |

---

## Admin — Sections

Base: `/api/admin/sections` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create section |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| GET | `/semester/{semesterId}` | All sections in a semester |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |
| GET | `/excel/export` | Export |
| GET | `/excel/template` | Template |
| POST | `/excel/import` | Bulk import |

---

## Admin — Subjects

Base: `/api/admin/subjects` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create subject |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |

**Note:** No Excel import for Subjects.

---

## Admin — Faculty

Base: `/api/admin/faculties` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| POST | `/` | Create faculty profile |
| GET | `/` | List all |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update |
| DELETE | `/{id}` | Delete |

**FacultyDTO fields:** `id`, `firstName`, `lastName`, `email`, `phone`, `designation`, `departmentId`, `status` (ACTIVE\|INACTIVE\|ON_LEAVE)

---

## Admin — Teachers

Base: `/api/admin/teachers` · `ROLE_ADMIN`

### POST /api/admin/teachers
Promote a Faculty member to Teacher. Auto-creates login account.

**Request:**
```json
{ "facultyId": "<uuid>", "notes": "Senior teacher" }
```
**Response 201:** TeacherDTO with `generatedPassword` (shown once only)  
**Errors:** 404 Faculty not found, 409 Already a teacher

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all teachers |
| GET | `/{id}` | Get teacher by ID |
| PUT | `/{id}` | Update teacher notes/status |
| DELETE | `/{id}` | Remove teacher (Faculty record kept) |

---

## Admin — Students

Base: `/api/admin/students` · `ROLE_ADMIN`

### POST /api/admin/students
Create a student. Auto-generates login account.

**Request:**
```json
{
  "firstName": "Ali", "lastName": "Khan", "email": "ali@college.com",
  "phone": "+923001234567", "dateOfBirth": "2000-01-15", "gender": "MALE",
  "fatherName": "Hassan", "fatherPhone": "+923001111111",
  "motherName": "Fatima", "motherPhone": "+923002222222",
  "guardianName": null, "guardianPhone": null
}
```
**Response 201:** StudentDTO with `generatedPassword` (shown once, never stored in plain text)

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all students |
| GET | `/{id}` | Get student by ID |
| PUT | `/{id}` | Update student profile |
| DELETE | `/{id}` | Delete student |
| GET | `/excel/export` | Export .xlsx |
| GET | `/excel/template` | Import template |
| POST | `/excel/import` | Bulk import (generates account per student) |

---

## Admin — Module Leaders

Base: `/api/admin/module-leaders` · `ROLE_ADMIN`

### POST /api/admin/module-leaders
Create Module Leader and assign to a subject.

**Request:**
```json
{
  "firstName": "Sara", "lastName": "Ahmed",
  "email": "sara@college.com", "phone": "+923001234567",
  "subjectId": "<uuid>"
}
```
**Response 201:** ModuleLeaderResponse with `generatedPassword` + auto-emailed credentials  
**Errors:** 409 Email already used or subject already has a module leader

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all module leaders |
| GET | `/{id}` | Get by ID |
| PUT | `/{id}` | Update details or reassign subject |
| DELETE | `/{id}` | Delete module leader + account |

---

## Admin — Enrollments

Base: `/api/admin/enrollments` · `ROLE_ADMIN`

### POST /api/admin/enrollments
Enroll student in a semester.

**Request:**
```json
{
  "studentId": "<uuid>", "semesterId": "<uuid>",
  "sectionId": "<uuid>",
  "subjectIds": ["<uuid>", "<uuid>"],
  "remarks": "First enrollment"
}
```
**Response 201:** EnrollmentResponse  
**Errors:** 404 Student/Semester/Section/Subject not found, 409 Already enrolled this semester

| Method | Path | Description |
|---|---|---|
| GET | `/{id}` | Get enrollment by ID |
| GET | `/student/{studentId}/active` | Current active enrollment |
| GET | `/student/{studentId}/history` | All enrollment history |
| GET | `/section/{sectionId}` | Active students in section |
| GET | `/semester/{semesterId}` | Active students in semester |
| POST | `/{id}/promote` | Promote to new semester/section |
| POST | `/bulk-promote` | Promote all in a section at once |
| PUT | `/{id}/drop` | Drop student (add `?reason=...`) |
| PUT | `/{id}/subjects` | Replace enrolled subjects (body: `["<uuid>", ...]`) |

---

## Admin — Timetable

Base: `/api/admin/timetable` · `ROLE_ADMIN`

### POST /api/admin/timetable/slots
Create a recurring weekly slot.

**Request:**
```json
{
  "teacherId": "<uuid>", "subjectId": "<uuid>",
  "classroomId": "<uuid>", "sectionId": "<uuid>",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00", "endTime": "10:00:00",
  "effectiveFrom": "2026-01-01", "effectiveTo": "2026-06-30",
  "notes": "optional"
}
```
**Response 201:** TimetableSlotDTO  
**Errors:** 409 Teacher, classroom, or section clash detected

| Method | Path | Description |
|---|---|---|
| GET | `/slots` | List all slots |
| GET | `/slots/{id}` | Get slot by ID |
| GET | `/slots/teacher/{teacherId}` | Slots for a teacher |
| PUT | `/slots/{id}` | Update slot |
| DELETE | `/slots/{id}` | Delete slot + all its exceptions |
| GET | `/slots/excel/export` | Export .xlsx |
| GET | `/slots/excel/template` | Import template |
| POST | `/slots/excel/import` | Bulk import |

### Timetable Exceptions

| Method | Path | Description |
|---|---|---|
| POST | `/exceptions` | Create exception (CANCELLED\|RESCHEDULED\|SUBSTITUTED) |
| GET | `/exceptions?date=2026-08-10` | All exceptions on a date |
| GET | `/exceptions/slot/{slotId}` | All exceptions for a slot |
| PUT | `/exceptions/{id}` | Update exception |
| DELETE | `/exceptions/{id}` | Delete exception (restores base slot) |

---

## Admin — Attendance

Base: `/api/admin/attendance` · `ROLE_ADMIN`

### GET /api/admin/attendance/report
Get attendance report for a slot on a specific date.

**Query params:** `slotId=<uuid>`, `date=2026-08-08`  
**Response 200:** Per-student PRESENT/ABSENT list + overall percentage

### GET /api/admin/attendance/section
All attendance records for a section on a date.

**Query params:** `sectionId=<uuid>`, `date=2026-08-08`  
**Response 200:** List of AttendanceRecordResponse

### PUT /api/admin/attendance/override
Manually override attendance status.

**Query params:** `studentId`, `slotId`, `date`, `status` (PRESENT\|ABSENT\|LATE), `reason` (optional)  
**Response 200:** Updated AttendanceRecordResponse

### GET /api/admin/attendance/qr/active
All currently active QR tokens (live sessions across all teachers).

**Response 200:** List of QrTokenResponse

### DELETE /api/admin/attendance/qr/{tokenId}/revoke
Immediately revoke a QR token (terminates live session).

**Response 200:** Success message

---

## Admin — Faculty Attendance

Base: `/api/admin/faculty-attendance` · `ROLE_ADMIN`

### POST /api/admin/faculty-attendance/qr/generate
Generate daily QR for faculty check-in.

**Query params:** `date=2026-08-08` (optional, defaults to today). Idempotent — returns existing QR if already generated.  
**Response 201:** FacultyDailyQrResponse `{ tokenValue, qrUrl, date, validUntil }`

| Method | Path | Description |
|---|---|---|
| GET | `/qr/today` | Get today's active faculty QR |
| DELETE | `/qr/{qrId}/revoke` | Revoke QR |
| GET | `/report?date=...` | Full faculty attendance report for a date |
| GET | `/report/department?departmentId=...&date=...` | Department-filtered report |
| GET | `/history/{facultyId}?from=...&to=...` | History for one faculty |
| PUT | `/override` | Override status (PRESENT\|ABSENT\|LATE\|LEAVE) |

---

## Admin — Device Management

Base: `/api/admin/devices` · `ROLE_ADMIN`

| Method | Path | Description |
|---|---|---|
| GET | `/alerts` | Unacknowledged device-change alerts (for badge count) |
| GET | `/alerts/all` | All alerts (acknowledged + unacknowledged) |
| GET | `/alerts/student/{studentId}` | Device change history for a student |
| PUT | `/alerts/{alertId}/acknowledge` | Mark alert as reviewed |
| DELETE | `/{studentId}/reset` | Clear device binding → student must re-login from phone |

---

## Teacher Endpoints — `ROLE_TEACHER`

### GET /api/teacher/me
Own profile: name, email, faculty details, department, designation.

### GET /api/teacher/attendance/report
Attendance report for one class session.

**Query params:** `slotId=<uuid>`, `date=2026-08-08`  
**Response 200:** AttendanceReportResponse (per-student status + overall %)

### GET /api/teacher/slots/{slotId}/students
All students enrolled in a slot's section. Alphabetically sorted.

**Response 200:** List of `{ studentId, firstName, lastName, email, phone }`

### GET /api/teacher/slots/{slotId}/history
Full attendance history for a slot across all past dates.

**Response 200:** SlotHistoryResponse (dates + per-session breakdowns, newest first)

---

## Teacher — Timetable

Base: `/api/teacher/timetable` · `ROLE_TEACHER`

| Method | Path | Description |
|---|---|---|
| GET | `/today` | Today's resolved schedule (includes cancellations/substitutions) |
| GET | `/week?weekStart=2026-08-10` | Full week schedule (weekStart must be Monday) |

**ResolvedSlotDTO fields:** `slotId`, `subject`, `classroom`, `section`, `startTime`, `endTime`, `status` (ACTIVE\|CANCELLED\|RESCHEDULED\|SUBSTITUTED), `exceptionNote`

---

## Teacher — QR Session Management

Base: `/api/teacher/qr` · `ROLE_TEACHER`

### POST /api/teacher/qr/{slotId}/start
Start attendance session. Generates first dynamic QR token.

**Response 201:** `{ tokenValue, qrUrl, expiresAt, secondsUntilExpiry }`  
**Errors:** 403 Not your slot, 409 Session already active today

### GET /api/teacher/qr/{slotId}/current
Get currently active token. Poll every ~30 seconds.

**Response 200:** QrTokenResponse  
**Errors:** 404 No active session

### POST /api/teacher/qr/{slotId}/rotate
Force-rotate token (useful if teacher suspects screenshot sharing).

**Response 200:** New QrTokenResponse

### POST /api/teacher/qr/{slotId}/end
End attendance session. Marks all non-scanners as ABSENT.

**Query params:** `date=2026-08-08` (optional, defaults to today)  
**Response 200:** Success message

---

## Teacher — Faculty Attendance

Base: `/api/faculty/attendance` · `ROLE_TEACHER`

| Method | Path | Description |
|---|---|---|
| GET | `/qr/today` | View today's faculty QR |
| POST | `/scan` | Scan to mark own attendance. Body: `{ "qrToken": "..." }`. WiFi check applied. |
| GET | `/history?from=...&to=...` | Own attendance history for date range |

**Errors on scan:** 403 Not on college WiFi, 410 QR expired/revoked, 409 Already marked today

---

## Student Endpoints — `ROLE_STUDENT`

### GET /api/student/me
Own profile: personal info + family contacts.

**Response 200:** StudentDTO

### GET /api/student/enrollment
Current active enrollment: semester, section, enrolled subjects.

**Response 200:** EnrollmentResponse

### GET /api/student/timetable/today
Today's classes for the student's section. Includes slot status (ACTIVE/CANCELLED/RESCHEDULED).

**Response 200:** List of ResolvedSlotDTO. The `slotId` in each entry is used in the QR scan body.

### GET /api/student/timetable/week
Full week schedule.

**Query params:** `weekStart=2026-08-10`  
**Response 200:** List of ResolvedSlotDTO

---

## Student — Attendance

Base: `/api/student/attendance` · `ROLE_STUDENT`

### POST /api/student/attendance/scan
Mark attendance by scanning QR.

**Headers:** `X-Device-Id: <uuid-from-localStorage>` (must match registered device)

**Request:**
```json
{
  "qrToken": "<tokenValue-from-qr-url>",
  "deviceId": "<localStorage-fingerprint>",
  "beaconId": "<beacon-uuid-detected-via-BLE>"
}
```
**Response 201:** AttendanceRecordResponse  
**Errors:**
- 400 QR token invalid or expired
- 403 Device mismatch (not your registered phone)
- 403 Beacon mismatch (not in the right classroom)
- 403 Section mismatch (not your class)
- 403 Not enrolled in this subject
- 409 Already marked today

### GET /api/student/attendance/today
Today's attendance status across all scheduled classes.

**Response 200:** List of AttendanceRecordResponse

### GET /api/student/attendance/history
Own attendance records for a date range.

**Query params:** `from=2026-08-01`, `to=2026-08-31`  
**Response 200:** List of AttendanceRecordResponse

### GET /api/student/attendance/summary
Attendance percentage per subject for the active semester.

**Response 200:**
```json
[
  { "subjectName": "Math", "totalClasses": 20, "presentCount": 17, "percentage": 85.0 },
  { "subjectName": "CS",   "totalClasses": 18, "presentCount": 14, "percentage": 77.8 }
]
```

---

## Module Leader Endpoints — `ROLE_MODULE_LEADER`

### GET /api/module-leader/me
Own profile: name, email, assigned subject.

### GET /api/module-leader/slots
All timetable slots for the module leader's subject (all sections + teachers).

**Response 200:** List of TimetableSlotDTO

### GET /api/module-leader/attendance/report
Attendance report for a specific slot on a specific date.

**Query params:** `slotId=<uuid>`, `date=2026-08-08`  
**Note:** Access restricted to slots belonging to the module leader's subject.  
**Response 200:** AttendanceReportResponse

### GET /api/module-leader/attendance/slots/{slotId}/history
Full attendance history for a slot (all past session dates).

**Response 200:** SlotHistoryResponse

### GET /api/module-leader/students
All students enrolled in the module leader's subject across all sections, with individual attendance %.

**Response 200:** List of StudentAttendanceSummaryDTO `{ studentId, name, section, totalClasses, presentCount, percentage }`

### GET /api/module-leader/teachers
All teachers who teach this module leader's subject.

**Response 200:** List of TeacherSummaryDTO `{ teacherId, name, designation, department, slotCount }`

---

## Common DTOs

### AttendanceRecordResponse
```json
{
  "id": "<uuid>", "studentId": "<uuid>", "studentName": "Ali Khan",
  "slotId": "<uuid>", "subjectName": "Math",
  "attendanceDate": "2026-08-08", "status": "PRESENT",
  "markedAt": "2026-08-08T09:05:00"
}
```

### AttendanceReportResponse
```json
{
  "slotId": "<uuid>", "subjectName": "Math",
  "date": "2026-08-08", "totalStudents": 30,
  "presentCount": 25, "absentCount": 5, "percentage": 83.3,
  "students": [
    { "studentId": "...", "name": "Ali Khan", "status": "PRESENT" }
  ]
}
```

### QrTokenResponse
```json
{
  "id": "<uuid>", "tokenValue": "<uuid>",
  "qrUrl": "http://localhost:8080/attend?token=<uuid>",
  "slotId": "<uuid>", "sessionDate": "2026-08-08",
  "expiresAt": "2026-08-08T09:07:00",
  "secondsUntilExpiry": 87, "status": "ACTIVE",
  "rotationCount": 2
}
```

### EnrollmentResponse
```json
{
  "id": "<uuid>", "studentId": "<uuid>", "studentName": "Ali Khan",
  "semesterId": "<uuid>", "semesterName": "Fall 2026",
  "sectionId": "<uuid>", "sectionName": "Section A",
  "subjects": [{ "id": "...", "name": "Math", "code": "MTH101" }],
  "status": "ACTIVE", "enrolledAt": "2026-08-01T00:00:00"
}
```

---

## Error Reference

| Code | When |
|---|---|
| 400 | Validation failed — `errors` field contains field-level messages |
| 401 | Missing/invalid JWT or wrong credentials |
| 403 | Insufficient role, device mismatch, phone-first rule, WiFi check failed |
| 404 | Entity not found |
| 409 | Duplicate (email, enrollment, active session, already scanned) |
| 410 | QR token expired or revoked |
| 422 | Excel import data error (check message for row/field info) |
| 500 | Unexpected server error |
