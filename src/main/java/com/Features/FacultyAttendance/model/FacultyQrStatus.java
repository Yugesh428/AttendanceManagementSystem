package com.Features.FacultyAttendance.model;

public enum FacultyQrStatus {
    ACTIVE,   // valid for today
    EXPIRED,  // past midnight — no longer scannable
    REVOKED   // admin manually revoked
}
