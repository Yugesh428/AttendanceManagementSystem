package com.Features.Attendance.repository;

import com.Features.Attendance.model.AttendanceRecord;
import com.Features.Attendance.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {

    /** Check for duplicate scan */
    boolean existsByStudentIdAndSlotIdAndAttendanceDate(
            UUID studentId, UUID slotId, LocalDate date);

    /** Find a specific record (used when admin manually overrides) */
    Optional<AttendanceRecord> findByStudentIdAndSlotIdAndAttendanceDate(
            UUID studentId, UUID slotId, LocalDate date);

    /** All records for a slot on a specific date — session report */
    List<AttendanceRecord> findBySlotIdAndAttendanceDateOrderByMarkedAt(
            UUID slotId, LocalDate date);

    /** All records for a slot across ALL dates — teacher historical view */
    @Query("""
           SELECT a FROM AttendanceRecord a
           WHERE a.slot.id = :slotId
           ORDER BY a.attendanceDate DESC, a.student.firstName ASC
           """)
    List<AttendanceRecord> findAllBySlotId(@Param("slotId") UUID slotId);

    /** All distinct dates a slot has had attendance — for date picker */
    @Query("""
           SELECT DISTINCT a.attendanceDate FROM AttendanceRecord a
           WHERE a.slot.id = :slotId
           ORDER BY a.attendanceDate DESC
           """)
    List<LocalDate> findDistinctDatesBySlotId(@Param("slotId") UUID slotId);

    /** All records for a student in a date range — student dashboard */
    List<AttendanceRecord> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID studentId, LocalDate from, LocalDate to);

    /** All records for a section on a date — admin report */
    @Query("""
           SELECT a FROM AttendanceRecord a
           WHERE a.slot.section.id = :sectionId
             AND a.attendanceDate = :date
           ORDER BY a.slot.startTime, a.student.firstName
           """)
    List<AttendanceRecord> findBySectionAndDate(
            @Param("sectionId") UUID sectionId,
            @Param("date") LocalDate date);

    /** Attendance count by status for a student and slot */
    long countByStudentIdAndSlotIdAndStatus(UUID studentId, UUID slotId, AttendanceStatus status);

    /**
     * Attendance % for a student in a subject (all dates).
     * Returns [totalClasses, presentCount] — service calculates %.
     */
    @Query("""
           SELECT COUNT(a) FROM AttendanceRecord a
           WHERE a.student.id = :studentId
             AND a.slot.subject.id = :subjectId
           """)
    long countTotalByStudentAndSubject(
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId);

    @Query("""
           SELECT COUNT(a) FROM AttendanceRecord a
           WHERE a.student.id = :studentId
             AND a.slot.subject.id = :subjectId
             AND a.status = 'PRESENT'
           """)
    long countPresentByStudentAndSubject(
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId);
}
