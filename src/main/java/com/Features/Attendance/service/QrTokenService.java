package com.Features.Attendance.service;

import com.Features.Attendance.dto.QrTokenResponse;

import java.util.List;
import java.util.UUID;

public interface QrTokenService {

    /** Teacher starts an attendance session for a slot — generates first token */
    QrTokenResponse startSession(UUID slotId, String teacherEmail);

    /** Get the currently ACTIVE token for a slot (teacher polls this) */
    QrTokenResponse getCurrentToken(UUID slotId);

    /** Manually rotate the token (teacher can force-rotate) */
    QrTokenResponse rotateToken(UUID slotId, String teacherEmail);

    /** Teacher ends the session — revokes active token */
    void endSession(UUID slotId, String teacherEmail);

    /** Admin: all currently active tokens across all slots */
    List<QrTokenResponse> getAllActiveTokens();

    /** Admin: revoke any token immediately */
    void revokeToken(UUID tokenId);

    /** Scheduled: auto-rotate tokens nearing expiry */
    void autoRotateExpiringTokens();
}
