package com.Features.Timetable.model;

/**
 * Academic day of week (Monday–Saturday, Sunday excluded by default).
 * Stored as STRING in DB so it's readable without joins.
 */
public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY
}
