package com.config;

import com.Features.Admin.AdminPart.Repository.AdminRepository;
import com.Features.Admin.AdminPart.model.Admin;
import com.Features.Admin.AdminPart.model.AdminStatus;
import com.Features.Admin.Beacon.Beacon;
import com.Features.Admin.Beacon.Repository.BeaconRepository;
import com.Features.Admin.Building.Repository.BuildingRepository;
import com.Features.Admin.Building.model.RegisterBuildingByAdmin;
import com.Features.Admin.Classroom.model.Classroom;
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
import com.Features.Attendance.model.AttendanceRecord;
import com.Features.Attendance.model.AttendanceStatus;
import com.Features.Attendance.model.QrToken;
import com.Features.Attendance.repository.AttendanceRepository;
import com.Features.Attendance.repository.QrTokenRepository;
import com.Features.Enrollment.model.Enrollment;
import com.Features.Enrollment.model.EnrollmentStatus;
import com.Features.Enrollment.repository.EnrollmentRepository;
import com.Features.FacultyAttendance.model.FacultyAttendanceRecord;
import com.Features.FacultyAttendance.model.FacultyDailyQr;
import com.Features.FacultyAttendance.repository.FacultyAttendanceRepository;
import com.Features.FacultyAttendance.repository.FacultyDailyQrRepository;
import com.Features.ModuleLeader.model.ModuleLeader;
import com.Features.ModuleLeader.model.ModuleLeaderAccount;
import com.Features.ModuleLeader.repository.ModuleLeaderAccountRepository;
import com.Features.ModuleLeader.repository.ModuleLeaderRepository;
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
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Heavy Test Data Seeder
 * Creates minimum 10 records for each entity to populate the database
 * Order: 2 (runs after SuperAdminSeeder)
 * Only active when Spring profile 'test-data' is enabled.
 */
@Component
@Order(2)
@Profile("test-data")
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder implements CommandLineRunner {

    // Repositories
    private final AdminRepository adminRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAccountRepository teacherAccountRepository;
    private final BuildingRepository buildingRepository;
    private final ClassroomRepository classroomRepository;
    private final BeaconRepository beaconRepository;
    private final CourseCategoryRepository courseCategoryRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final StudentAccountRepository studentAccountRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TimetableSlotRepository timetableSlotRepository;
    private final ModuleLeaderRepository moduleLeaderRepository;
    private final ModuleLeaderAccountRepository moduleLeaderAccountRepository;
    private final FacultyDailyQrRepository facultyDailyQrRepository;
    private final FacultyAttendanceRepository facultyAttendanceRepository;
    private final AttendanceRepository attendanceRepository;
    private final QrTokenRepository qrTokenRepository;
    
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("=== Starting Heavy Test Data Seeding ===");
        
        // Check if seeding already done
        if (adminRepository.count() >= 10) {
            log.info("Test data already exists — skipping seeding");
            return;
        }

        try {
            // 1. Seed Admins (10)
            List<Admin> admins = seedAdmins();
            log.info("✓ Created {} Admins", admins.size());

            // 2. Seed Departments (10)
            List<Department> departments = seedDepartments();
            log.info("✓ Created {} Departments", departments.size());

            // 3. Seed Faculties (15)
            List<Faculty> faculties = seedFaculties(departments);
            log.info("✓ Created {} Faculties", faculties.size());

            // 4. Seed Teachers (10)
            List<Teacher> teachers = seedTeachers(faculties);
            log.info("✓ Created {} Teachers", teachers.size());

            // 5. Seed Buildings (10)
            List<RegisterBuildingByAdmin> buildings = seedBuildings();
            log.info("✓ Created {} Buildings", buildings.size());

            // 6. Seed Classrooms (15)
            List<Classroom> classrooms = seedClassrooms(buildings);
            log.info("✓ Created {} Classrooms", classrooms.size());

            // 7. Seed Beacons (15)
            List<Beacon> beacons = seedBeacons(classrooms);
            log.info("✓ Created {} Beacons", beacons.size());

            // 8. Seed Course Categories (10)
            List<CourseCategory> categories = seedCourseCategories();
            log.info("✓ Created {} Course Categories", categories.size());

            // 9. Seed Subjects (15)
            List<Subject> subjects = seedSubjects(categories);
            log.info("✓ Created {} Subjects", subjects.size());

            // 10. Seed Semesters (10)
            List<Semester> semesters = seedSemesters();
            log.info("✓ Created {} Semesters", semesters.size());

            // 11. Seed Sections (12)
            List<Section> sections = seedSections();
            log.info("✓ Created {} Sections", sections.size());

            // 12. Seed Students (20)
            List<Student> students = seedStudents();
            log.info("✓ Created {} Students", students.size());

            // 13. Seed Enrollments (20)
            List<Enrollment> enrollments = seedEnrollments(students, semesters, sections, subjects);
            log.info("✓ Created {} Enrollments", enrollments.size());

            // 14. Seed Module Leaders (10)
            List<ModuleLeader> moduleLeaders = seedModuleLeaders(subjects);
            log.info("✓ Created {} Module Leaders", moduleLeaders.size());

            // 15. Seed Timetable Slots (50)
            List<TimetableSlot> slots = seedTimetableSlots(sections, subjects, teachers, classrooms);
            log.info("✓ Created {} Timetable Slots", slots.size());

            // 16. Seed Faculty QR & Attendance (15)
            seedFacultyAttendance(faculties, beacons);
            log.info("✓ Created Faculty Attendance records");

            // 17. Seed QR Tokens & Attendance (20)
            seedStudentAttendance(students, slots);
            log.info("✓ Created Student Attendance records");

            log.info("=== Heavy Test Data Seeding Completed Successfully ===");

        } catch (Exception e) {
            log.error("Error during test data seeding", e);
            throw new RuntimeException("Test data seeding failed", e);
        }
    }

    // ======================== SEEDING METHODS ========================

    private List<Admin> seedAdmins() {
        List<Admin> admins = new ArrayList<>();
        String[] cities = {"Kathmandu", "Pokhara", "Lalitpur", "Bhaktapur", "Biratnagar", 
                          "Birgunj", "Dharan", "Hetauda", "Butwal", "Janakpur"};
        
        for (int i = 1; i <= 10; i++) {
            Admin admin = Admin.builder()
                    .firstName("Admin" + i)
                    .lastName("User" + i)
                    .email("admin" + i + "@test.com")
                    .password(passwordEncoder.encode("admin" + i + "123"))
                    .phoneNumber("9841" + String.format("%06d", i))
                    .tenantName("Tenant " + i)
                    .organizationAddress("Address " + i + ", " + cities[i-1])
                    .organizationCity(cities[i-1])
                    .organizationCountry("Nepal")
                    .status(AdminStatus.ACTIVE)
                    .build();
            admins.add(adminRepository.save(admin));
        }
        return admins;
    }

    private List<Department> seedDepartments() {
        List<Department> departments = new ArrayList<>();
        String[][] deptData = {
            {"Computer Science & Engineering", "CSE", "Engineering department focused on computing"},
            {"Electronics & Communication", "ECE", "Engineering department for electronics"},
            {"Mechanical Engineering", "MECH", "Engineering department for mechanical systems"},
            {"Civil Engineering", "CIVIL", "Engineering department for construction"},
            {"Business Administration", "BBA", "Business and management studies"},
            {"Information Technology", "IT", "Information technology and systems"},
            {"Electrical Engineering", "EE", "Electrical systems and power engineering"},
            {"Biotechnology", "BT", "Biological and technological sciences"},
            {"Architecture", "ARCH", "Architectural design and planning"},
            {"Data Science", "DS", "Data analytics and machine learning"}
        };

        for (String[] data : deptData) {
            Department dept = Department.builder()
                    .name(data[0])
                    .code(data[1])
                    .description(data[2])
                    .build();
            departments.add(departmentRepository.save(dept));
        }
        return departments;
    }

    private List<Faculty> seedFaculties(List<Department> departments) {
        List<Faculty> faculties = new ArrayList<>();
        String[] firstNames = {"Ram", "Sita", "Hari", "Gita", "Shyam", "Radha", "Krishna", "Laxmi",
                              "Bishnu", "Parvati", "Ganesh", "Saraswati", "Narayan", "Durga", "Shiva"};
        String[] lastNames = {"Sharma", "Poudel", "Adhikari", "Thapa", "Rai", "Gurung", "Tamang",
                             "Shrestha", "Maharjan", "Karki", "KC", "Bhandari", "Pandey", "Joshi", "Bhattarai"};

        for (int i = 0; i < 15; i++) {
            Faculty faculty = Faculty.builder()
                    .firstName(firstNames[i])
                    .middleName(i % 2 == 0 ? "Kumar" : "Prasad")
                    .lastName(lastNames[i])
                    .email(firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@faculty.com")
                    .phone("9851" + String.format("%06d", i + 1))
                    .address("Faculty Address " + (i + 1))
                    .description("Experienced faculty member")
                    .designation(i % 3 == 0 ? "Professor" : i % 3 == 1 ? "Associate Professor" : "Assistant Professor")
                    .joiningDate(LocalDate.now().minusYears(i % 10))
                    .status(FacultyStatus.ACTIVE)
                    .createdBy("admin")
                    .department(departments.get(i % departments.size()))
                    .build();
            faculties.add(facultyRepository.save(faculty));
        }
        return faculties;
    }

    private List<Teacher> seedTeachers(List<Faculty> faculties) {
        List<Teacher> teachers = new ArrayList<>();
        
        for (int i = 0; i < 10 && i < faculties.size(); i++) {
            Faculty faculty = faculties.get(i);
            
            Teacher teacher = Teacher.builder()
                    .faculty(faculty)
                    .notes("Promoted to teacher role - " + (i + 1))
                    .active(true)
                    .build();
            teacher = teacherRepository.save(teacher);

            // Create Teacher Account
            TeacherAccount account = TeacherAccount.builder()
                    .teacher(teacher)
                    .username(faculty.getEmail())   // use email so login works with email+password
                    .password(passwordEncoder.encode("teacher" + (i + 1) + "123"))
                    .active(true)
                    .build();
            teacherAccountRepository.save(account);
            
            teachers.add(teacher);
        }
        return teachers;
    }

    private List<RegisterBuildingByAdmin> seedBuildings() {
        List<RegisterBuildingByAdmin> buildings = new ArrayList<>();
        String[] buildingNames = {"Main Block", "Science Block", "Engineering Block", "Admin Block",
                                 "Library Block", "Sports Complex", "Auditorium Block", "Cafeteria Block",
                                 "Research Center", "Innovation Hub"};

        for (int i = 0; i < 10; i++) {
            RegisterBuildingByAdmin building = RegisterBuildingByAdmin.builder()
                    .name(buildingNames[i])
                    .location("Campus Location " + (char)('A' + i))
                    .build();
            buildings.add(buildingRepository.save(building));
        }
        return buildings;
    }

    private List<Classroom> seedClassrooms(List<RegisterBuildingByAdmin> buildings) {
        List<Classroom> classrooms = new ArrayList<>();
        com.Features.Admin.Classroom.model.ClassType[] types = {
            com.Features.Admin.Classroom.model.ClassType.LECTURE, 
            com.Features.Admin.Classroom.model.ClassType.PRACTICAL, 
            com.Features.Admin.Classroom.model.ClassType.TUTORIAL
        };

        for (int i = 0; i < 15; i++) {
            Classroom classroom = Classroom.builder()
                    .name("Room " + (101 + i))
                    .classType(types[i % types.length])
                    .building(buildings.get(i % buildings.size()))
                    .build();
            classrooms.add(classroomRepository.save(classroom));
        }
        return classrooms;
    }

    private List<Beacon> seedBeacons(List<Classroom> classrooms) {
        List<Beacon> beacons = new ArrayList<>();

        for (int i = 0; i < 15 && i < classrooms.size(); i++) {
            Beacon beacon = Beacon.builder()
                    .uuid("BEACON-UUID-" + String.format("%04d", i + 1))
                    .major(i + 100)
                    .minor(i + 1)
                    .classroom(classrooms.get(i))
                    .build();
            beacons.add(beaconRepository.save(beacon));
        }
        return beacons;
    }

    private List<CourseCategory> seedCourseCategories() {
        List<CourseCategory> categories = new ArrayList<>();
        String[][] catData = {
            {"Core Engineering", "CORE-ENG"},
            {"Mathematics", "MATH"},
            {"Physics", "PHY"},
            {"Chemistry", "CHEM"},
            {"Programming", "PROG"},
            {"Electronics", "ELEC"},
            {"Management", "MGMT"},
            {"Communication Skills", "COMM"},
            {"Project Work", "PROJ"},
            {"Electives", "ELECT"}
        };

        for (int i = 0; i < catData.length; i++) {
            CourseCategory category = CourseCategory.builder()
                    .courseName(catData[i][0])
                    .courseCode(catData[i][1])
                    .description("Category for " + catData[i][0] + " courses")
                    .build();
            categories.add(courseCategoryRepository.save(category));
        }
        return categories;
    }

    private List<Subject> seedSubjects(List<CourseCategory> categories) {
        List<Subject> subjects = new ArrayList<>();
        String[][] subjectData = {
            {"Data Structures", "SUB001"},
            {"Algorithms", "SUB002"},
            {"Database Systems", "SUB003"},
            {"Computer Networks", "SUB004"},
            {"Operating Systems", "SUB005"},
            {"Software Engineering", "SUB006"},
            {"Web Technologies", "SUB007"},
            {"Machine Learning", "SUB008"},
            {"Artificial Intelligence", "SUB009"},
            {"Cloud Computing", "SUB010"},
            {"Cyber Security", "SUB011"},
            {"Mobile Computing", "SUB012"},
            {"IoT Systems", "SUB013"},
            {"Blockchain", "SUB014"},
            {"Digital Electronics", "SUB015"}
        };

        for (int i = 0; i < subjectData.length; i++) {
            Subject subject = Subject.builder()
                    .subjectName(subjectData[i][0])
                    .subjectCode(subjectData[i][1])
                    .courseCategory(categories.get(i % categories.size()))
                    .build();
            subjects.add(subjectRepository.save(subject));
        }
        return subjects;
    }

    private List<Semester> seedSemesters() {
        List<Semester> semesters = new ArrayList<>();
        String[] semNames = {"Semester 1", "Semester 2", "Semester 3", "Semester 4",
                            "Semester 5", "Semester 6", "Semester 7", "Semester 8",
                            "Semester 9", "Semester 10"};

        for (int i = 0; i < 10; i++) {
            Semester semester = Semester.builder()
                    .name(semNames[i])
                    .build();
            semesters.add(semesterRepository.save(semester));
        }
        return semesters;
    }

    private List<Section> seedSections() {
        List<Section> sections = new ArrayList<>();
        List<Semester> allSemesters = semesterRepository.findAll();
        
        String[] sectionNames = {"Section A", "Section B", "Section C", "Section D",
                                "Section E", "Section F", "Section G", "Section H",
                                "Section I", "Section J", "Section K", "Section L"};

        for (int i = 0; i < 12; i++) {
            Section section = Section.builder()
                    .name(sectionNames[i])
                    .description("Academic section " + sectionNames[i])
                    .capacity(60 + (i * 5))
                    .semester(allSemesters.get(i % allSemesters.size()))
                    .build();
            sections.add(sectionRepository.save(section));
        }
        return sections;
    }

    private List<Student> seedStudents() {
        List<Student> students = new ArrayList<>();
        String[] firstNames = {"Anil", "Binita", "Chandan", "Deepa", "Elina", "Falguni", "Gaurav",
                              "Hemant", "Ishani", "Jeevan", "Kiran", "Lila", "Mohan", "Nikita",
                              "Ojha", "Prabin", "Queena", "Rajesh", "Sabina", "Tilak"};
        String[] lastNames = {"Basnet", "Chettri", "Dahal", "Rana", "Sapkota", "Thakuri", "Upreti",
                             "Shah", "Malla", "Regmi", "Subedi", "Gautam", "Koirala", "Dhungana",
                             "Acharya", "Bhusal", "Chapagain", "Dangol", "Ghimire", "Khadka"};

        for (int i = 0; i < 20; i++) {
            Student student = Student.builder()
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .email("student" + (i + 1) + "@test.com")
                    .phone("9861" + String.format("%06d", i + 1))
                    .dateOfBirth(LocalDate.of(2000 + (i % 5), (i % 12) + 1, (i % 28) + 1))
                    .gender(i % 2 == 0 ? "Male" : "Female")
                    .fatherName("Father of " + firstNames[i])
                    .fatherPhone("9871" + String.format("%06d", i + 1))
                    .motherName("Mother of " + firstNames[i])
                    .motherPhone("9881" + String.format("%06d", i + 1))
                    .build();
            student = studentRepository.save(student);

            // Create Student Account
            StudentAccount account = StudentAccount.builder()
                    .student(student)
                    .username(student.getEmail())   // use email so login works with email+password
                    .password(passwordEncoder.encode("student" + (i + 1) + "123"))
                    .active(true)
                    .build();
            studentAccountRepository.save(account);

            students.add(student);
        }
        return students;
    }

    private List<Enrollment> seedEnrollments(List<Student> students, List<Semester> semesters,
                                             List<Section> sections, List<Subject> subjects) {
        List<Enrollment> enrollments = new ArrayList<>();

        for (int i = 0; i < 20 && i < students.size(); i++) {
            Set<Subject> enrolledSubjects = new HashSet<>();
            // Enroll in 3-5 subjects
            for (int j = 0; j < (3 + i % 3); j++) {
                enrolledSubjects.add(subjects.get((i + j) % subjects.size()));
            }

            Enrollment enrollment = Enrollment.builder()
                    .student(students.get(i))
                    .semester(semesters.get(i % semesters.size()))
                    .section(sections.get(i % sections.size()))
                    .subjects(enrolledSubjects)
                    .status(EnrollmentStatus.ACTIVE)
                    .remarks("Regular enrollment")
                    .build();
            enrollments.add(enrollmentRepository.save(enrollment));
        }
        return enrollments;
    }

    private List<ModuleLeader> seedModuleLeaders(List<Subject> subjects) {
        List<ModuleLeader> moduleLeaders = new ArrayList<>();

        String[] firstNames = {"Arjun", "Bina", "Chitra", "Dinesh", "Elina",
                               "Faisal", "Geeta", "Hari", "Indira", "Jagat"};
        String[] lastNames  = {"Acharya", "Basnet", "Chhetri", "Dahal", "Rai",
                               "Sharma", "Thapa", "Upreti", "Shah", "Karki"};

        for (int i = 0; i < 10 && i < subjects.size(); i++) {
            ModuleLeader moduleLeader = ModuleLeader.builder()
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .email("ml" + (i + 1) + "@moduleleader.com")
                    .phone("9898" + String.format("%06d", i + 1))
                    .subject(subjects.get(i))
                    .active(true)
                    .build();
            moduleLeader = moduleLeaderRepository.save(moduleLeader);

            // Create Module Leader Account
            ModuleLeaderAccount account = ModuleLeaderAccount.builder()
                    .moduleLeader(moduleLeader)
                    .username(moduleLeader.getEmail())  // use email so login works with email+password
                    .password(passwordEncoder.encode("ml" + (i + 1) + "123"))
                    .build();
            moduleLeaderAccountRepository.save(account);

            moduleLeaders.add(moduleLeader);
        }
        return moduleLeaders;
    }

    private List<TimetableSlot> seedTimetableSlots(List<Section> sections, List<Subject> subjects,
                                                    List<Teacher> teachers, List<Classroom> classrooms) {
        List<TimetableSlot> slots = new ArrayList<>();
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                           DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
        LocalTime[] startTimes = {
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0)
        };

        LocalDate effectiveFrom = LocalDate.now();
        LocalDate effectiveTo = LocalDate.now().plusMonths(6);

        int slotCount = 0;
        for (int d = 0; d < days.length && slotCount < 50; d++) {
            for (int t = 0; t < startTimes.length && slotCount < 50; t++) {
                TimetableSlot slot = TimetableSlot.builder()
                        .section(sections.get(slotCount % sections.size()))
                        .subject(subjects.get(slotCount % subjects.size()))
                        .teacher(teachers.get(slotCount % teachers.size()))
                        .classroom(classrooms.get(slotCount % classrooms.size()))
                        .dayOfWeek(days[d])
                        .startTime(startTimes[t])
                        .endTime(startTimes[t].plusHours(1))
                        .effectiveFrom(effectiveFrom)
                        .effectiveTo(effectiveTo)
                        .notes("Regular class slot " + (slotCount + 1))
                        .build();
                slots.add(timetableSlotRepository.save(slot));
                slotCount++;
            }
        }
        return slots;
    }

    private void seedFacultyAttendance(List<Faculty> faculties, List<Beacon> beacons) {
        LocalDate today = LocalDate.now();

        // Create Faculty Daily QR (one per day)
        FacultyDailyQr dailyQr = FacultyDailyQr.builder()
                .qrDate(today)
                .tokenValue(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(12))
                .generatedBy("admin")
                .build();
        facultyDailyQrRepository.save(dailyQr);

        // Create Faculty Attendance Records for 15 faculties
        for (int i = 0; i < 15 && i < faculties.size(); i++) {
            Faculty faculty = faculties.get(i);

            FacultyAttendanceRecord attendance = FacultyAttendanceRecord.builder()
                    .faculty(faculty)
                    .attendanceDate(today)
                    .scannedFromIp("192.168.1." + (i + 10))
                    .qr(dailyQr)
                    .build();
            facultyAttendanceRepository.save(attendance);
        }
    }

    private void seedStudentAttendance(List<Student> students, List<TimetableSlot> slots) {
        LocalDate today = LocalDate.now();

        // ── Collect last 10 weekdays (excluding today) ────────────────────────
        List<LocalDate> sessionDates = new ArrayList<>();
        LocalDate cursor = today.minusDays(1);
        while (sessionDates.size() < 10) {
            java.time.DayOfWeek dow = cursor.getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) {
                sessionDates.add(cursor);
            }
            cursor = cursor.minusDays(1);
        }

        // ── Use 6 distinct slots (= 6 different subjects) ────────────────────
        int numSlots    = Math.min(6, slots.size());
        // First 5 students get full attendance history
        int numStudents = Math.min(5, students.size());

        for (int dateIdx = 0; dateIdx < sessionDates.size(); dateIdx++) {
            LocalDate sessionDate = sessionDates.get(dateIdx);

            for (int slotIdx = 0; slotIdx < numSlots; slotIdx++) {
                TimetableSlot slot = slots.get(slotIdx);

                // One QR token per (slot + date) session
                QrToken token = QrToken.builder()
                        .tokenValue(UUID.randomUUID().toString())
                        .slot(slot)
                        .sessionDate(sessionDate)
                        .generatedBy(slot.getTeacher())
                        .expiresAt(sessionDate.atTime(slot.getEndTime()))
                        .sessionId(UUID.randomUUID())
                        .rotationCount(0)
                        .build();
                token = qrTokenRepository.save(token);

                for (int sIdx = 0; sIdx < numStudents; sIdx++) {
                    Student student = students.get(sIdx);

                    // student1 → perfect attendance; others → ~80 % present
                    boolean present = (sIdx == 0) || ((dateIdx + slotIdx + sIdx) % 5 != 0);

                    AttendanceRecord record = AttendanceRecord.builder()
                            .student(student)
                            .slot(slot)
                            .attendanceDate(sessionDate)
                            .status(present ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT)
                            .qrToken(present ? token : null)
                            .deviceId(present ? "Device-Student-" + (sIdx + 1) : null)
                            .beaconId(present ? "BEACON-UUID-" + String.format("%04d", (slotIdx % 15) + 1) : null)
                            .build();
                    attendanceRepository.save(record);
                }
            }
        }
    }
}
