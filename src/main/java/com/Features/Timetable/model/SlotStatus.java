package com.Features.Timetable.model;

/**
 * Status of a timetable exception on a specific date.
 *
 * RESCHEDULED  – slot moved to a different time/room on this date
 * CANCELLED    – class cancelled on this date
 * SUBSTITUTED  – different teacher covers on this date
 */
public enum SlotStatus {
    ACTIVE,
    RESCHEDULED,
    CANCELLED,
    SUBSTITUTED
}
