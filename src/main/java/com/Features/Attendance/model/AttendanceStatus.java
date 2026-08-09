package com.Features.Attendance.model;

public enum AttendanceStatus {
    PRESENT,   // student scanned QR successfully
    ABSENT,    // session ended — student did not scan
    LATE       // reserved for future use (e.g. scanned after grace period)
}
