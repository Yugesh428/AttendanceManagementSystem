package com.Features.Timetable.repository;

import com.Features.Timetable.model.TimetableException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimetableExceptionRepository extends JpaRepository<TimetableException, UUID> {

    /** Single exception for one slot on one date */
    Optional<TimetableException> findBySlotIdAndExceptionDate(UUID slotId, LocalDate date);

    /** All exceptions for a teacher within a date range (for dashboard) */
    @Query("""
           SELECT e FROM TimetableException e
           WHERE e.slot.teacher.id = :teacherId
             AND e.exceptionDate BETWEEN :from AND :to
           ORDER BY e.exceptionDate, e.slot.startTime
           """)
    List<TimetableException> findByTeacherAndDateRange(
            @Param("teacherId") UUID teacherId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** All exceptions on a specific date (admin view) */
    List<TimetableException> findByExceptionDateOrderBySlotStartTime(LocalDate date);

    /** All exceptions for a slot */
    List<TimetableException> findBySlotIdOrderByExceptionDate(UUID slotId);

    /** All exceptions for a section within a date range (for student dashboard) */
    @Query("""
           SELECT e FROM TimetableException e
           WHERE e.slot.section.id = :sectionId
             AND e.exceptionDate BETWEEN :from AND :to
           ORDER BY e.exceptionDate, e.slot.startTime
           """)
    List<TimetableException> findBySectionAndDateRange(
            @Param("sectionId") UUID sectionId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
