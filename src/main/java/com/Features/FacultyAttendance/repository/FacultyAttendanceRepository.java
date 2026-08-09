package com.Features.FacultyAttendance.repository;

import com.Features.FacultyAttendance.model.FacultyAttendanceRecord;
import com.Features.FacultyAttendance.model.FacultyAttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyAttendanceRepository extends JpaRepository<FacultyAttendanceRecord, UUID> {

    /** Duplicate check */
    boolean existsByFacultyIdAndAttendanceDate(UUID facultyId, LocalDate date);

    /** Find specific record */
    Optional<FacultyAttendanceRecord> findByFacultyIdAndAttendanceDate(UUID facultyId, LocalDate date);

    /** All records for a date — daily report */
    List<FacultyAttendanceRecord> findByAttendanceDateOrderByFacultyFirstName(LocalDate date);

    /** All records for a faculty in a date range */
    List<FacultyAttendanceRecord> findByFacultyIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            UUID facultyId, LocalDate from, LocalDate to);

    /** Count present days for a faculty in a date range */
    long countByFacultyIdAndAttendanceDateBetweenAndStatus(
            UUID facultyId, LocalDate from, LocalDate to, FacultyAttendanceStatus status);

    /** All records for a department on a date */
    @Query("""
           SELECT a FROM FacultyAttendanceRecord a
           WHERE a.faculty.department.id = :departmentId
             AND a.attendanceDate = :date
           ORDER BY a.faculty.firstName
           """)
    List<FacultyAttendanceRecord> findByDepartmentAndDate(
            @Param("departmentId") UUID departmentId,
            @Param("date") LocalDate date);
}
