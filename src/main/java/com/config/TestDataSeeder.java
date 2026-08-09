package com.config;

import com.Features.Admin.AdminPart.Repository.AdminRepository;
import com.Features.Admin.AdminPart.model.Admin;
import com.Features.Admin.AdminPart.model.AdminStatus;
import com.Features.Admin.Beacon.Beacon;
import com.Features.Admin.Beacon.Repository.BeaconRepository;
import com.Features.Admin.Building.Repository.BuildingRepository;
import com.Features.Admin.Building.model.RegisterBuildingByAdmin;
import com.Features.Admin.Classroom.model.Classroom;
import com.Features.Admin.Classroom.model.ClassType;
import com.Features.Admin.Classroom.repository.ClassroomRepository;
import com.Features.Admin.CourseCategory.model.CourseCategory;
import com.Features.Admin.CourseCategory.repository.CourseCategoryRepository;
import com.Features.Admin.Department.model.Department;
import com.Features.Admin.Department.repository.DepartmentRepository;
import com.Features.Admin.Section.Repository.SectionRepository;
import com.Features.Admin.Section.Section;
import com.Features.Admin.Semester.Repository.SemesterRepository;
import com.Features.Admin.Semester.Semester;
import com.Features.Admin.Student.model.Student;
import com.Features.Admin.Student.model.StudentAccount;
import com.Features.Admin.Student.repository.StudentAccountRepository;
import com.Features.Admin.Student.repository.StudentRepository;
import com.Features.Admin.Subject.Subject;
import com.Features.Admin.Subject.repository.SubjectRepository;
import com.Features.Admin.faculty.Faculty;
import com.Features.Admin.faculty.FacultyStatus;
import com.Features.Admin.faculty.reposityory.FacultyRepository;
import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.Teacher.model.Teacher;
import com.Features.Teacher.model.TeacherAccount;
import com.Features.Teacher.repository.TeacherAccountRepository;
import com.Features.Teacher.repository.TeacherRepository;
import com.Features.Timetable.model.DayOfWeek;
import com.Features.Timetable.model.TimetableSlot;
import com.Features.Timetable.repository.TimetableSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * ══════════════════════════════════════════════════════════════
 * TEST DATA SEEDER — runs on startup AFTER SuperAdminSeeder
 * ══════════════════════════════════════════════════════════════
 *
 * Seeds a complete dataset for API testing in Postman:
 *
 *   Admin         →  admin@test.com / Test@1234
 *   Teacher       →  teacher@test.com / test@1234
 *   Student       →  student@test.com / stud@1234
 *   Beacon UUID   →  "TEST-BEACON-001"  (use this in scan requests)
 *   Device ID     →  "TEST-DEVICE-001"  (use as X-Device-Id header)
 *
 * All data is skipped if it already exists (idempotent — safe to restart).
 */
@Slf4j
@Component
@Order(2)   // runs after SuperAdminSeeder (Order 1)
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

    // ── Repositories ──────────────────────────────────────────────────────────
    private final AdminRepository           adminRepository;
    private final DepartmentRepository      departmentRepository;
    private final FacultyRepository         facultyRepository;
    private final TeacherRepository         teacherRepository;
    private final TeacherAccountRepository  teacherAccountRepository;
    private final BuildingRepository        buildingRepository;
    private final ClassroomRepository       classroomRepository;
    private final BeaconRepository          beaconRepository;
    private final CourseCategoryRepository  courseCategoryRepository;
    private final SubjectRepository         subjectRepository;
    private final SemesterRepository        semesterRepository;
    private final SectionRepository         sectionRepository;
    private final StudentRepository         studentRepository;
    private final StudentAccountRepository  studentAccountRepository;
    private final EnrollmentRepository      enrollmentRepository;
    private final TimetableSlotRepository   timetableSlotRepository;
    private final PasswordEncoder           passwordEncoder;

    // ── Seed constants ─────────────────────────────────────────────────────────
    private static final String ADMIN_EMAIL    = "admin@test.com";
    private static final String ADMIN_PASSWORD = "Test@1234";

    private static final String TEACHER_EMAIL    = "teacher@test.com";
    private static final String TEACHER_PASSWORD = "test@1234";

    private static final String STUDENT_EMAIL    = "student@test.com";
    private static final String STUDENT_PASSWORD = "stud@1234";

    private static final String BEACON_UUID  = "TEST-BEACON-001";
    private static final String DEVICE_ID    = "TEST-DEVICE-001";

    // ── Entry point ────────────────────────────────────────────────────────────
    @Override
    public void run(String... args) {
        log.info("════════════════════════════════════════════");
        log.info("[SEED] Starting test data seeder...");

        // 1. Admin
        Admin admin = seedAdmin();

        // 2. Department + Faculty + Teacher
        Department dept = seedDepartment();
        Faculty faculty  = seedFaculty(dept);
        Teacher teacher  = seedTeacher(faculty);

        // 3. Building + Classroom + Beacon
        RegisterBuildingByAdmin building = seedBuilding();
        Classroom classroom = seedClassroom(building);
        seedBeacon(classroom);

        // 4. Academic structure
        CourseCategory category = seedCourseCategory();
        Subject subject          = seedSubject(category);
        Semester semester        = seedSemester();
        Section section          = seedSection(semester);

        // 5. Student + Enrollment
        Student student = seedStudent();
        seedEnrollment(student, semester, section, subject);

        // 6. Timetable slot
        seedTimetableSlot(teacher, subject, classroom, section);

        log.info("[SEED] Test data seeder complete.");
        log.info("════════════════════════════════════════════");
        log.info("[SEED] Admin Login    → email: {}  password: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
        log.info("[SEED] Teacher Login  → email: {}  password: {}", TEACHER_EMAIL, TEACHER_PASSWORD);
        log.info("[SEED] Student Login  → email: {}  password: {}", STUDENT_EMAIL, STUDENT_PASSWORD);
        log.info("[SEED] Beacon UUID    → {}", BEACON_UUID);
        log.info("[SEED] Device ID      → {} (send as X-Device-Id header)", DEVICE_ID);
        log.info("════════════════════════════════════════════");
    }

    // ══════════════════════════════════════════════════════════════
    // ADMIN
    // ══════════════════════════════════════════════════════════════

    private Admin seedAdmin() {
        if (adminRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("[SEED] Admin already exists — skipping");
            return adminRepository.findByEmail(ADMIN_EMAIL).orElseThrow();
        }
        Admin admin = adminRepository.save(Admin.builder()
                .firstName("Test")
                .lastName("Admin")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .phoneNumber("9800000001")
                .tenantName("Test College")
                .organizationCity("Kathmandu")
                .organizationCountry("Nepal")
                .status(AdminStatus.ACTIVE)
                .build());
        log.info("[SEED] Admin created: {}", ADMIN_EMAIL);
        return admin;
    }

    // ══════════════════════════════════════════════════════════════
    // DEPARTMENT + FACULTY + TEACHER
    // ══════════════════════════════════════════════════════════════

    private Department seedDepartment() {
        if (departmentRepository.existsByCode("CS")) {
            log.info("[SEED] Department already exists — skipping");
            return departmentRepository.findByName("Computer Science").orElseThrow();
        }
        Department dept = departmentRepository.save(Department.builder()
                .name("Computer Science")
                .code("CS")
                .description("Department of Computer Science")
                .build());
        log.info("[SEED] Department created: CS");
        return dept;
    }

    private Faculty seedFaculty(Department dept) {
        if (facultyRepository.existsByEmail(TEACHER_EMAIL)) {
            log.info("[SEED] Faculty already exists — skipping");
            return facultyRepository.findByEmail(TEACHER_EMAIL).orElseThrow();
        }
        Faculty faculty = facultyRepository.save(Faculty.builder()
                .firstName("Test")
                .lastName("Teacher")
                .email(TEACHER_EMAIL)
                .phone("9800000002")
                .designation("Lecturer")
                .status(FacultyStatus.ACTIVE)
                .department(dept)
                .build());
        log.info("[SEED] Faculty created: {}", TEACHER_EMAIL);
        return faculty;
    }

    private Teacher seedTeacher(Faculty faculty) {
        if (teacherRepository.existsByFacultyId(faculty.getId())) {
            log.info("[SEED] Teacher already exists — skipping");
            return teacherRepository.findByFacultyId(faculty.getId()).orElseThrow();
        }
        Teacher teacher = teacherRepository.save(Teacher.builder()
                .faculty(faculty)
                .active(true)
                .notes("Test teacher for API testing")
                .build());

        teacherAccountRepository.save(TeacherAccount.builder()
                .username(TEACHER_EMAIL)
                .password(passwordEncoder.encode(TEACHER_PASSWORD))
                .active(true)
                .teacher(teacher)
                .build());

        log.info("[SEED] Teacher + account created: {}", TEACHER_EMAIL);
        return teacher;
    }

    // ══════════════════════════════════════════════════════════════
    // BUILDING + CLASSROOM + BEACON
    // ══════════════════════════════════════════════════════════════

    private RegisterBuildingByAdmin seedBuilding() {
        // Check by name — BuildingRepository has no existsByName so use findAll trick
        return buildingRepository.findAll().stream()
                .filter(b -> "Test Block A".equals(b.getName()))
                .findFirst()
                .orElseGet(() -> {
                    RegisterBuildingByAdmin b = buildingRepository.save(
                            RegisterBuildingByAdmin.builder()
                                    .name("Test Block A")
                                    .location("Campus Main Gate")
                                    .build());
                    log.info("[SEED] Building created: Test Block A");
                    return b;
                });
    }

    private Classroom seedClassroom(RegisterBuildingByAdmin building) {
        return classroomRepository.findAll().stream()
                .filter(c -> "Test Room 101".equals(c.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Classroom c = classroomRepository.save(Classroom.builder()
                            .name("Test Room 101")
                            .classType(ClassType.LECTURE)
                            .building(building)
                            .build());
                    log.info("[SEED] Classroom created: Test Room 101");
                    return c;
                });
    }

    private void seedBeacon(Classroom classroom) {
        boolean exists = beaconRepository.findByClassroomId(classroom.getId())
                .stream().anyMatch(b -> BEACON_UUID.equals(b.getUuid()));
        if (exists) {
            log.info("[SEED] Beacon already exists — skipping");
            return;
        }
        beaconRepository.save(Beacon.builder()
                .uuid(BEACON_UUID)
                .major(1)
                .minor(1)
                .classroom(classroom)
                .build());
        log.info("[SEED] Beacon created: uuid={}", BEACON_UUID);
    }

    // ══════════════════════════════════════════════════════════════
    // ACADEMIC STRUCTURE
    // ══════════════════════════════════════════════════════════════

    private CourseCategory seedCourseCategory() {
        if (courseCategoryRepository.existsByCourseName("Test Category")) {
            log.info("[SEED] CourseCategory already exists — skipping");
            return courseCategoryRepository.findByCourseName("Test Category").orElseThrow();
        }
        CourseCategory cat = courseCategoryRepository.save(CourseCategory.builder()
                .courseName("Test Category")
                .courseCode("TC101")
                .description("Test course category for API testing")
                .build());
        log.info("[SEED] CourseCategory created: Test Category");
        return cat;
    }

    private Subject seedSubject(CourseCategory category) {
        if (subjectRepository.existsBySubjectCode("TEST101")) {
            log.info("[SEED] Subject already exists — skipping");
            return subjectRepository.findBySubjectCode("TEST101").orElseThrow();
        }
        Subject subject = subjectRepository.save(Subject.builder()
                .subjectName("Test Subject")
                .subjectCode("TEST101")
                .courseCategory(category)
                .build());
        log.info("[SEED] Subject created: TEST101");
        return subject;
    }

    private Semester seedSemester() {
        return semesterRepository.findAll().stream()
                .filter(s -> "Test Semester 1".equals(s.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Semester s = semesterRepository.save(
                            Semester.builder().name("Test Semester 1").build());
                    log.info("[SEED] Semester created");
                    return s;
                });
    }

    private Section seedSection(Semester semester) {
        if (sectionRepository.existsByNameAndSemesterId("A", semester.getId())) {
            log.info("[SEED] Section already exists — skipping");
            return sectionRepository.findBySemesterId(semester.getId())
                    .stream().filter(s -> "A".equals(s.getName())).findFirst().orElseThrow();
        }
        Section section = sectionRepository.save(Section.builder()
                .name("A")
                .description("Test Section A")
                .capacity(30)
                .semester(semester)
                .build());
        log.info("[SEED] Section created: A");
        return section;
    }

    // ══════════════════════════════════════════════════════════════
    // STUDENT + ACCOUNT + ENROLLMENT
    // ══════════════════════════════════════════════════════════════

    private Student seedStudent() {
        if (studentRepository.existsByEmail(STUDENT_EMAIL)) {
            log.info("[SEED] Student already exists — skipping");
            return studentRepository.findByEmail(STUDENT_EMAIL).orElseThrow();
        }
        Student student = studentRepository.save(Student.builder()
                .firstName("Test")
                .lastName("Student")
                .email(STUDENT_EMAIL)
                .phone("9800000003")
                .gender("Male")
                .build());

        // Create account with pre-registered device so scan works immediately
        studentAccountRepository.save(StudentAccount.builder()
                .username(STUDENT_EMAIL)
                .password(passwordEncoder.encode(STUDENT_PASSWORD))
                .active(true)
                .student(student)
                .deviceId(DEVICE_ID)                           // pre-registered device
                .deviceRegisteredAt(java.time.LocalDateTime.now())
                .phoneRegistered(true)                         // skip phone-first requirement
                .build());

        log.info("[SEED] Student + account created: {} (deviceId={})", STUDENT_EMAIL, DEVICE_ID);
        return student;
    }

    private void seedEnrollment(Student student, Semester semester,
                                 Section section, Subject subject) {
        if (enrollmentRepository.existsByStudentIdAndSemesterId(
                student.getId(), semester.getId())) {
            log.info("[SEED] Enrollment already exists — skipping");
            return;
        }
        enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .semester(semester)
                .section(section)
                .subjects(Set.of(subject))
                .status(EnrollmentStatus.ACTIVE)
                .remarks("Seeded for API testing")
                .build());
        log.info("[SEED] Enrollment created: {} → {} / {} / {}",
                STUDENT_EMAIL, semester.getName(), section.getName(),
                subject.getSubjectCode());
    }

    // ══════════════════════════════════════════════════════════════
    // TIMETABLE SLOT
    // ══════════════════════════════════════════════════════════════

    private void seedTimetableSlot(Teacher teacher, Subject subject,
                                    Classroom classroom, Section section) {
        boolean exists = timetableSlotRepository.findBySubjectId(subject.getId())
                .stream().anyMatch(s -> s.getSection().getId().equals(section.getId()));
        if (exists) {
            log.info("[SEED] TimetableSlot already exists — skipping");
            return;
        }
        timetableSlotRepository.save(TimetableSlot.builder()
                .teacher(teacher)
                .subject(subject)
                .classroom(classroom)
                .section(section)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .notes("Seeded test slot — Mon 09:00-10:00")
                .build());
        log.info("[SEED] TimetableSlot created: MONDAY 09:00-10:00");
    }
}
