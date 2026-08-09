package com.Features.Timetable.repository;

import com.Features.Timetable.model.DayOfWeek;
import com.Features.Timetable.model.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    /** All slots for a teacher — used for teacher dashboard */
    @Query("""
           SELECT s FROM TimetableSlot s
           WHERE s.teacher.id = :teacherId
             AND s.effectiveFrom <= :date
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
           ORDER BY s.dayOfWeek, s.startTime
           """)
    List<TimetableSlot> findActiveSlotsByTeacherAndDate(
            @Param("teacherId") UUID teacherId,
            @Param("date") LocalDate date);

    /** All slots for a section — used for student dashboard */
    @Query("""
           SELECT s FROM TimetableSlot s
           WHERE s.section.id = :sectionId
             AND s.effectiveFrom <= :date
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
           ORDER BY s.dayOfWeek, s.startTime
           """)
    List<TimetableSlot> findActiveSlotsBySectionAndDate(
            @Param("sectionId") UUID sectionId,
            @Param("date") LocalDate date);

    /** All slots on a specific day for a teacher */
    @Query("""
           SELECT s FROM TimetableSlot s
           WHERE s.teacher.id = :teacherId
             AND s.dayOfWeek = :day
             AND s.effectiveFrom <= :date
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
           ORDER BY s.startTime
           """)
    List<TimetableSlot> findByTeacherAndDay(
            @Param("teacherId") UUID teacherId,
            @Param("day") DayOfWeek day,
            @Param("date") LocalDate date);

    /** Admin filter: all slots for a classroom in an effective range */
    List<TimetableSlot> findByClassroomIdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID classroomId, LocalDate from, LocalDate to);

    /** Admin filter: all slots for a subject */
    List<TimetableSlot> findBySubjectId(UUID subjectId);

    /** Clash check — teacher double-booked */
    @Query("""
           SELECT COUNT(s) > 0 FROM TimetableSlot s
           WHERE s.teacher.id = :teacherId
             AND s.dayOfWeek = :day
             AND s.effectiveFrom <= :effectiveTo
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveFrom)
             AND s.startTime < :endTime
             AND s.endTime > :startTime
             AND (:excludeId IS NULL OR s.id <> :excludeId)
           """)
    boolean teacherHasClash(
            @Param("teacherId") UUID teacherId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("excludeId") UUID excludeId);

    /** Clash check — classroom double-booked */
    @Query("""
           SELECT COUNT(s) > 0 FROM TimetableSlot s
           WHERE s.classroom.id = :classroomId
             AND s.dayOfWeek = :day
             AND s.effectiveFrom <= :effectiveTo
             AND (s.effectiveTo IS NULL OR s.effectiveTo >= :effectiveFrom)
             AND s.startTime < :endTime
             AND s.endTime > :startTime
             AND (:excludeId IS NULL OR s.id <> :excludeId)
           """)
    boolean classroomHasClash(
            @Param("classroomId") UUID classroomId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("excludeId") UUID excludeId);
}
