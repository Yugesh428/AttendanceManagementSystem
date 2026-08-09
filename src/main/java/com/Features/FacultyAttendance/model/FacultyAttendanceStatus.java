package com.Features.FacultyAttendance.model;

public enum FacultyAttendanceStatus {
    PRESENT,  // scanned QR on college WiFi
    ABSENT,   // did not scan (auto-marked at end of day)
    LATE,     // scanned after grace period (optional future use)
    LEAVE     // admin manually marked as on leave
}
