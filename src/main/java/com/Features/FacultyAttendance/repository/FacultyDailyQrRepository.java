package com.Features.FacultyAttendance.repository;

import com.Features.FacultyAttendance.model.FacultyDailyQr;
import com.Features.FacultyAttendance.model.FacultyQrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyDailyQrRepository extends JpaRepository<FacultyDailyQr, UUID> {

    /** Find today's active QR */
    Optional<FacultyDailyQr> findByQrDateAndStatus(LocalDate date, FacultyQrStatus status);

    /** Find by token value — used during scan */
    Optional<FacultyDailyQr> findByTokenValue(String tokenValue);

    /** Check if QR already exists for a date */
    boolean existsByQrDate(LocalDate date);

    /** Find by date (any status) */
    Optional<FacultyDailyQr> findByQrDate(LocalDate date);
}
