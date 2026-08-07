package com.Features.Enrollment.repository;

import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    /** Find the single ACTIVE enrollment for a student */
    Optional<Enrollment> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);

    /** Find a specific enrollment for a student in a semester */
    Optional<Enrollment> findByStudentIdAndSemesterId(UUID studentId, UUID semesterId);

    /** All enrollments for a student (full history) */
    List<Enrollment> findByStudentIdOrderByEnrolledAtDesc(UUID studentId);

    /** All ACTIVE enrollments in a section (who is currently in this section) */
    List<Enrollment> findBySectionIdAndStatus(UUID sectionId, EnrollmentStatus status);

    /** All ACTIVE enrollments in a semester */
    List<Enrollment> findBySemesterIdAndStatus(UUID semesterId, EnrollmentStatus status);

    /** All enrollments in a section + semester (for bulk promote source) */
    List<Enrollment> findBySectionIdAndSemesterIdAndStatus(
            UUID sectionId, UUID semesterId, EnrollmentStatus status);

    /** Check if student is already enrolled in this semester */
    boolean existsByStudentIdAndSemesterId(UUID studentId, UUID semesterId);

    /**
     * Check if student is enrolled in a specific subject in their active enrollment.
     * Used during attendance scan validation.
     */
    @Query("""
           SELECT COUNT(e) > 0 FROM Enrollment e
           JOIN e.subjects s
           WHERE e.student.id = :studentId
             AND e.status = 'ACTIVE'
             AND s.id = :subjectId
           """)
    boolean isStudentEnrolledInSubject(
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId);
}
