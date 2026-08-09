package com.Features.Attendance.model;

public enum QrTokenStatus {
    /** Currently valid — students can scan */
    ACTIVE,
    /** Replaced by a rotation — no longer valid */
    EXPIRED,
    /** Teacher ended the session — no more scans accepted */
    REVOKED
}
