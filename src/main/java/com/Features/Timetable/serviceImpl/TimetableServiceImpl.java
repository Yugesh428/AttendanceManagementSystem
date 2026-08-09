package com.Features.Timetable.serviceImpl;

import com.Features.Admin.Classroom.model.Classroom;
import com.Features.Admin.Classroom.repository.ClassroomRepository;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Section.Repository.SectionRepository;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Admin.Subject.Subject;
import com.Features.Admin.Subject.repository.SubjectRepository;
import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.Teacher.model.Teacher;
import com.Features.Teacher.repository.TeacherRepository;
import com.Features.Timetable.dto.ResolvedSlotDTO;
import com.Features.Timetable.dto.TimetableExceptionDTO;
import com.Features.Timetable.dto.TimetableSlotDTO;
import com.Features.Timetable.excel.TimetableExcelHelper;
import com.Features.Timetable.model.DayOfWeek;
import com.Features.Timetable.model.SlotStatus;
import com.Features.Timetable.model.TimetableException;
import com.Features.Timetable.model.TimetableSlot;
import com.Features.Timetable.repository.TimetableExceptionRepository;
import com.Features.Timetable.repository.TimetableSlotRepository;
import com.Features.Timetable.service.TimetableService;
import com.exception.AppException;
import com.exception.ExcelImportException;
import com.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TimetableServiceImpl implements TimetableService {

    private final TimetableSlotRepository slotRepository;
    private final TimetableExceptionRepository exceptionRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ════════════════════════════════════════════════════════════════════
    // SLOTS — CRUD
    // ════════════════════════════════════════════════════════════════════

    @Override
    public TimetableSlotDTO createSlot(TimetableSlotDTO dto) {
        log.info("[TIMETABLE] Creating slot teacher='{}' day='{}' start='{}'",
                dto.getTeacherId(), dto.getDayOfWeek(), dto.getStartTime());

        Teacher teacher     = findTeacher(dto.getTeacherId());
        Subject subject     = findSubject(dto.getSubjectId());
        Classroom classroom = findClassroom(dto.getClassroomId());
        Section section     = findSection(dto.getSectionId());

        // Validate end > start
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "End time must be after start time.");
        }

        LocalDate effectiveTo = dto.getEffectiveTo() != null ? dto.getEffectiveTo() : LocalDate.of(9999, 12, 31);

        // Clash checks
        if (slotRepository.teacherHasClash(teacher.getId(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime(), dto.getEffectiveFrom(), effectiveTo, null)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Teacher already has a slot that overlaps on " + dto.getDayOfWeek()
                    + " " + dto.getStartTime() + "–" + dto.getEndTime());
        }
        if (slotRepository.classroomHasClash(classroom.getId(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime(), dto.getEffectiveFrom(), effectiveTo, null)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Classroom '" + classroom.getName() + "' is already booked on "
                    + dto.getDayOfWeek() + " " + dto.getStartTime() + "–" + dto.getEndTime());
        }

        TimetableSlot saved = slotRepository.save(TimetableSlot.builder()
                .teacher(teacher)
                .subject(subject)
                .classroom(classroom)
                .section(section)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .notes(dto.getNotes())
                .build());

        log.info("[TIMETABLE] Slot created id='{}'", saved.getId());
        return mapSlotToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableSlotDTO getSlotById(UUID id) {
        return mapSlotToDTO(findSlot(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableSlotDTO> getAllSlots() {
        return slotRepository.findAll().stream().map(this::mapSlotToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableSlotDTO> getSlotsByTeacher(UUID teacherId) {
        findTeacher(teacherId); // 404 guard
        return slotRepository
                .findActiveSlotsByTeacherAndDate(teacherId, LocalDate.now())
                .stream().map(this::mapSlotToDTO).collect(Collectors.toList());
    }

    @Override
    public TimetableSlotDTO updateSlot(UUID id, TimetableSlotDTO dto) {
        log.info("[TIMETABLE] Updating slot id='{}'", id);
        TimetableSlot slot = findSlot(id);

        Teacher teacher     = findTeacher(dto.getTeacherId());
        Subject subject     = findSubject(dto.getSubjectId());
        Classroom classroom = findClassroom(dto.getClassroomId());
        Section section     = findSection(dto.getSectionId());

        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "End time must be after start time.");
        }

        LocalDate effectiveTo = dto.getEffectiveTo() != null ? dto.getEffectiveTo() : LocalDate.of(9999, 12, 31);

        if (slotRepository.teacherHasClash(teacher.getId(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime(), dto.getEffectiveFrom(), effectiveTo, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Teacher clash detected on " + dto.getDayOfWeek());
        }
        if (slotRepository.classroomHasClash(classroom.getId(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime(), dto.getEffectiveFrom(), effectiveTo, id)) {
            throw new AppException(HttpStatus.CONFLICT, "Classroom clash detected on " + dto.getDayOfWeek());
        }

        slot.setTeacher(teacher);
        slot.setSubject(subject);
        slot.setClassroom(classroom);
        slot.setSection(section);
        slot.setDayOfWeek(dto.getDayOfWeek());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setEffectiveFrom(dto.getEffectiveFrom());
        slot.setEffectiveTo(dto.getEffectiveTo());
        slot.setNotes(dto.getNotes());

        return mapSlotToDTO(slotRepository.save(slot));
    }

    @Override
    public void deleteSlot(UUID id) {
        log.info("[TIMETABLE] Deleting slot id='{}'", id);
        if (!slotRepository.existsById(id)) throw new ResourceNotFoundException("TimetableSlot", "id", id);
        slotRepository.deleteById(id);
    }

    // ════════════════════════════════════════════════════════════════════
    // EXCEL
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportSlotsToExcel() {
        return TimetableExcelHelper.export(getAllSlots());
    }

    @Override
    public ByteArrayInputStream downloadSlotTemplate() {
        return TimetableExcelHelper.template();
    }

    @Override
    public List<TimetableSlotDTO> importSlotsFromExcel(MultipartFile file) {
        if (!TimetableExcelHelper.hasExcelFormat(file)) {
            throw new ExcelImportException("Invalid file type. Please upload a .xlsx file.");
        }
        try {
            List<TimetableSlotDTO> parsed = TimetableExcelHelper.parseExcel(file.getInputStream());
            log.info("[TIMETABLE] Importing {} slot(s) from Excel", parsed.size());
            List<TimetableSlotDTO> saved = new ArrayList<>();
            for (TimetableSlotDTO dto : parsed) {
                saved.add(createSlot(dto));   // reuses clash checks
            }
            return saved;
        } catch (ExcelImportException e) {
            throw e;
        } catch (IOException e) {
            throw new ExcelImportException("Could not read file: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // EXCEPTIONS — one-off overrides
    // ════════════════════════════════════════════════════════════════════

    @Override
    public TimetableExceptionDTO createException(TimetableExceptionDTO dto) {
        log.info("[TIMETABLE] Creating exception slotId='{}' date='{}'",
                dto.getSlotId(), dto.getExceptionDate());

        TimetableSlot slot = findSlot(dto.getSlotId());

        // Only one exception per slot per date
        if (exceptionRepository.findBySlotIdAndExceptionDate(
                dto.getSlotId(), dto.getExceptionDate()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT,
                    "An exception already exists for this slot on " + dto.getExceptionDate()
                    + ". Update the existing one instead.");
        }

        Classroom overrideClassroom = dto.getOverrideClassroomId() != null
                ? findClassroom(dto.getOverrideClassroomId()) : null;

        Teacher substituteTeacher = dto.getSubstituteTeacherId() != null
                ? findTeacher(dto.getSubstituteTeacherId()) : null;

        TimetableException saved = exceptionRepository.save(TimetableException.builder()
                .slot(slot)
                .exceptionDate(dto.getExceptionDate())
                .status(dto.getStatus())
                .overrideStartTime(dto.getOverrideStartTime())
                .overrideEndTime(dto.getOverrideEndTime())
                .overrideClassroom(overrideClassroom)
                .substituteTeacher(substituteTeacher)
                .reason(dto.getReason())
                .build());

        log.info("[TIMETABLE] Exception created id='{}'", saved.getId());
        return mapExceptionToDTO(saved);
    }

    @Override
    public TimetableExceptionDTO updateException(UUID id, TimetableExceptionDTO dto) {
        log.info("[TIMETABLE] Updating exception id='{}'", id);
        TimetableException ex = exceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableException", "id", id));

        ex.setStatus(dto.getStatus());
        ex.setOverrideStartTime(dto.getOverrideStartTime());
        ex.setOverrideEndTime(dto.getOverrideEndTime());
        ex.setOverrideClassroom(dto.getOverrideClassroomId() != null
                ? findClassroom(dto.getOverrideClassroomId()) : null);
        ex.setSubstituteTeacher(dto.getSubstituteTeacherId() != null
                ? findTeacher(dto.getSubstituteTeacherId()) : null);
        ex.setReason(dto.getReason());

        return mapExceptionToDTO(exceptionRepository.save(ex));
    }

    @Override
    public void deleteException(UUID id) {
        log.info("[TIMETABLE] Deleting exception id='{}'", id);
        if (!exceptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("TimetableException", "id", id);
        }
        exceptionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableExceptionDTO> getExceptionsByDate(LocalDate date) {
        return exceptionRepository.findByExceptionDateOrderBySlotStartTime(date)
                .stream().map(this::mapExceptionToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableExceptionDTO> getExceptionsBySlot(UUID slotId) {
        findSlot(slotId); // 404 guard
        return exceptionRepository.findBySlotIdOrderByExceptionDate(slotId)
                .stream().map(this::mapExceptionToDTO).collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════
    // RESOLVED DASHBOARD — base slots merged with exceptions
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedSlotDTO> getMyTodaySchedule(String teacherEmail) {
        LocalDate today = LocalDate.now();
        return resolveSchedule(teacherEmail, today, today);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedSlotDTO> getMyWeekSchedule(String teacherEmail, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(5); // Mon–Sat
        return resolveSchedule(teacherEmail, weekStart, weekEnd);
    }

    /**
     * Core resolution algorithm:
     *  1. Find teacher by email
     *  2. Get all base slots active during [from, to]
     *  3. Get all exceptions for teacher during [from, to]
     *  4. For each calendar date in range, for each base slot matching that day:
     *       a. Check if an exception exists for (slot, date)
     *       b. If yes → apply exception values (time, classroom, teacher, status)
     *       c. If no  → emit base slot with status=ACTIVE
     */
    private List<ResolvedSlotDTO> resolveSchedule(String teacherEmail, LocalDate from, LocalDate to) {
        Teacher teacher = teacherRepository.findByFacultyEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", teacherEmail));

        // Load all base slots active in range
        List<TimetableSlot> baseSlots = slotRepository
                .findActiveSlotsByTeacherAndDate(teacher.getId(), from);

        // Load exceptions for this teacher in date range — key: slotId::date
        Map<String, TimetableException> exceptionMap =
                exceptionRepository.findByTeacherAndDateRange(teacher.getId(), from, to)
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getSlot().getId() + "::" + e.getExceptionDate(),
                                e -> e));

        List<ResolvedSlotDTO> result = new ArrayList<>();

        // Walk each date in range
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            DayOfWeek day = DayOfWeek.valueOf(date.getDayOfWeek().name());

            for (TimetableSlot slot : baseSlots) {
                // This slot only applies on its day of week
                if (slot.getDayOfWeek() != day) continue;
                // And within its effective range
                if (date.isBefore(slot.getEffectiveFrom())) continue;
                if (slot.getEffectiveTo() != null && date.isAfter(slot.getEffectiveTo())) continue;

                String key = slot.getId() + "::" + date;
                TimetableException ex = exceptionMap.get(key);

                result.add(resolve(slot, date, ex));
            }
        }

        // Sort: date → startTime
        result.sort((a, b) -> {
            int d = a.getDate().compareTo(b.getDate());
            return d != 0 ? d : a.getStartTime().compareTo(b.getStartTime());
        });

        return result;
    }

    /** Merges one base slot with its exception (if any) into a ResolvedSlotDTO */
    private ResolvedSlotDTO resolve(TimetableSlot slot, LocalDate date, TimetableException ex) {
        if (ex == null) {
            // No exception — use base slot values
            return ResolvedSlotDTO.builder()
                    .slotId(slot.getId())
                    .date(date)
                    .dayOfWeek(slot.getDayOfWeek())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .classroomName(slot.getClassroom().getName())
                    .sectionName(slot.getSection().getName())
                    .subjectName(slot.getSubject().getSubjectName())
                    .subjectCode(slot.getSubject().getSubjectCode())
                    .teacherName(slot.getTeacher().getFaculty().getFirstName()
                            + " " + slot.getTeacher().getFaculty().getLastName())
                    .status(SlotStatus.ACTIVE)
                    .hasException(false)
                    .isCancelled(false)
                    .isSubstituted(false)
                    .build();
        }

        // Apply exception overrides
        String classroomName = ex.getOverrideClassroom() != null
                ? ex.getOverrideClassroom().getName()
                : slot.getClassroom().getName();

        String teacherName;
        boolean isSubstituted = ex.getStatus() == SlotStatus.SUBSTITUTED
                && ex.getSubstituteTeacher() != null;
        if (isSubstituted) {
            teacherName = ex.getSubstituteTeacher().getFaculty().getFirstName()
                    + " " + ex.getSubstituteTeacher().getFaculty().getLastName();
        } else {
            teacherName = slot.getTeacher().getFaculty().getFirstName()
                    + " " + slot.getTeacher().getFaculty().getLastName();
        }

        return ResolvedSlotDTO.builder()
                .slotId(slot.getId())
                .exceptionId(ex.getId())
                .date(date)
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(ex.getOverrideStartTime() != null
                        ? ex.getOverrideStartTime() : slot.getStartTime())
                .endTime(ex.getOverrideEndTime() != null
                        ? ex.getOverrideEndTime() : slot.getEndTime())
                .classroomName(classroomName)
                .sectionName(slot.getSection().getName())
                .subjectName(slot.getSubject().getSubjectName())
                .subjectCode(slot.getSubject().getSubjectCode())
                .teacherName(teacherName)
                .status(ex.getStatus())
                .reason(ex.getReason())
                .hasException(true)
                .isCancelled(ex.getStatus() == SlotStatus.CANCELLED)
                .isSubstituted(isSubstituted)
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ════════════════════════════════════════════════════════════════════

    private TimetableSlotDTO mapSlotToDTO(TimetableSlot s) {
        return TimetableSlotDTO.builder()
                .id(s.getId())
                .teacherId(s.getTeacher().getId())
                .teacherName(s.getTeacher().getFaculty().getFirstName()
                        + " " + s.getTeacher().getFaculty().getLastName())
                .subjectId(s.getSubject().getId())
                .subjectName(s.getSubject().getSubjectName())
                .subjectCode(s.getSubject().getSubjectCode())
                .classroomId(s.getClassroom().getId())
                .classroomName(s.getClassroom().getName())
                .sectionId(s.getSection().getId())
                .sectionName(s.getSection().getName())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .effectiveFrom(s.getEffectiveFrom())
                .effectiveTo(s.getEffectiveTo())
                .notes(s.getNotes())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private TimetableExceptionDTO mapExceptionToDTO(TimetableException e) {
        TimetableSlot slot = e.getSlot();
        return TimetableExceptionDTO.builder()
                .id(e.getId())
                .slotId(slot.getId())
                .exceptionDate(e.getExceptionDate())
                .status(e.getStatus())
                .overrideStartTime(e.getOverrideStartTime())
                .overrideEndTime(e.getOverrideEndTime())
                .overrideClassroomId(e.getOverrideClassroom() != null
                        ? e.getOverrideClassroom().getId() : null)
                .overrideClassroomName(e.getOverrideClassroom() != null
                        ? e.getOverrideClassroom().getName() : null)
                .substituteTeacherId(e.getSubstituteTeacher() != null
                        ? e.getSubstituteTeacher().getId() : null)
                .substituteTeacherName(e.getSubstituteTeacher() != null
                        ? e.getSubstituteTeacher().getFaculty().getFirstName()
                          + " " + e.getSubstituteTeacher().getFaculty().getLastName() : null)
                .reason(e.getReason())
                .teacherName(slot.getTeacher().getFaculty().getFirstName()
                        + " " + slot.getTeacher().getFaculty().getLastName())
                .subjectName(slot.getSubject().getSubjectName())
                .subjectCode(slot.getSubject().getSubjectCode())
                .originalClassroomName(slot.getClassroom().getName())
                .originalStartTime(slot.getStartTime())
                .originalEndTime(slot.getEndTime())
                .createdAt(e.getCreatedAt())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════
    // LOOKUP HELPERS
    // ════════════════════════════════════════════════════════════════════

    private TimetableSlot findSlot(UUID id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableSlot", "id", id));
    }

    private Teacher findTeacher(UUID id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
    }

    private Subject findSubject(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
    }

    private Classroom findClassroom(UUID id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", id));
    }

    private Section findSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
    }

    // ════════════════════════════════════════════════════════════════════
    // STUDENT SCHEDULE — resolves by section (from active enrollment)
    // ════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedSlotDTO> getStudentTodaySchedule(String studentEmail) {
        LocalDate today = LocalDate.now();
        return resolveStudentSchedule(studentEmail, today, today);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedSlotDTO> getStudentWeekSchedule(String studentEmail, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(5);
        return resolveStudentSchedule(studentEmail, weekStart, weekEnd);
    }

    /**
     * Resolves student schedule by looking up their active enrollment's section,
     * then finding all timetable slots for that section in the given date range.
     * Exceptions (cancellations, reschedules) are applied just like the teacher view.
     */
    private List<ResolvedSlotDTO> resolveStudentSchedule(String studentEmail,
                                                          LocalDate from, LocalDate to) {
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active Enrollment", "studentEmail", studentEmail));

        UUID sectionId = enrollment.getSection().getId();

        List<TimetableSlot> baseSlots =
                slotRepository.findActiveSlotsBySectionAndDate(sectionId, from);

        // Load all exceptions for any slot in this section's range
        Map<String, TimetableException> exceptionMap =
                exceptionRepository.findBySectionAndDateRange(sectionId, from, to)
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getSlot().getId() + "::" + e.getExceptionDate(),
                                e -> e));

        List<ResolvedSlotDTO> result = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            DayOfWeek day = DayOfWeek.valueOf(date.getDayOfWeek().name());

            for (TimetableSlot slot : baseSlots) {
                if (slot.getDayOfWeek() != day) continue;
                if (date.isBefore(slot.getEffectiveFrom())) continue;
                if (slot.getEffectiveTo() != null && date.isAfter(slot.getEffectiveTo())) continue;

                String key = slot.getId() + "::" + date;
                TimetableException ex = exceptionMap.get(key);
                result.add(resolve(slot, date, ex));
            }
        }

        result.sort((a, b) -> {
            int d = a.getDate().compareTo(b.getDate());
            return d != 0 ? d : a.getStartTime().compareTo(b.getStartTime());
        });

        return result;
    }
}
