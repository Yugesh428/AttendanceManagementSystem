package com.Features.Student.repository;

import com.Features.Student.model.DeviceChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceChangeLogRepository extends JpaRepository<DeviceChangeLog, UUID> {

    /** All unacknowledged device change alerts — used by admin dashboard */
    List<DeviceChangeLog> findByAcknowledgedFalseOrderByDetectedAtDesc();

    /** All device changes for a specific student */
    List<DeviceChangeLog> findByStudentIdOrderByDetectedAtDesc(UUID studentId);

    /** Count unacknowledged alerts — for admin notification badge */
    long countByAcknowledgedFalse();
}
