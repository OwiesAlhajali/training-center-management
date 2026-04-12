package com.trainingcenter.management.config;

import com.trainingcenter.management.entity.*;
import com.trainingcenter.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DemoDataSeeder {

    private final TenantRepository tenantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final InstituteRepository instituteRepository;
    private final ClassRoomRepository classRoomRepository;
    private final CourseRepository courseRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    @Bean
    public CommandLineRunner seedDemoData() {
        return args -> {
            if (tenantRepository.existsByKey("TC-DEMO")) {
                return;
            }

            Tenant tenant = tenantRepository.save(Tenant.builder()
                    .key("TC-DEMO")
                    .name("Training Center Demo")
                    .address("Demo Address")
                    .build());

            Category category = new Category();
            category.setName("Programming");
            category = categoryRepository.save(category);

            User studentUser = new User();
            studentUser.setUsername("teststudent");
            studentUser.setEmail("test@gmail.com");
            studentUser.setPassword("password");
            studentUser.setUserType(User.UserType.STUDENT);
            studentUser = userRepository.save(studentUser);

            Student student = new Student();
            student.setFirstName("Test");
            student.setLastName("Student");
            student.setGender("Male");
            student.setBirthDate(LocalDate.of(2000, 1, 1));
            student.setEnrollmentDate(LocalDate.now());
            student.setInterest("Java and backend");
            student.setAddress("Demo Student Address");
            student.setUser(studentUser);
            student = studentRepository.save(student);

            User teacherUser = new User();
            teacherUser.setUsername("testteacher");
            teacherUser.setEmail("teacher@gmail.com");
            teacherUser.setPassword("password");
            teacherUser.setUserType(User.UserType.TEACHER);
            teacherUser = userRepository.save(teacherUser);

            Teacher teacher = Teacher.builder()
                    .firstName("Test")
                    .lastName("Teacher")
                    .specialization("Java")
                    .certificates("Demo Certificate")
                    .address("Demo Teacher Address")
                    .cv("Demo CV")
                    .experienceYears(5)
                    .user(teacherUser)
                    .build();
            teacher = teacherRepository.save(teacher);

            Institute institute = Institute.builder()
                    .name("Demo Institute")
                    .workingHours("9-5")
                    .description("Demo institute for payment testing")
                    .location("Demo City")
                    .tenant(tenant)
                    .build();
            institute = instituteRepository.save(institute);

            ClassRoom classRoom = ClassRoom.builder()
                    .number("CR-1")
                    .capacity(20)
                    .availableDevices("Projector, PCs")
                    .images("demo-room.jpg")
                    .institute(institute)
                    .build();
            classRoom = classRoomRepository.save(classRoom);

            Course course = Course.builder()
                    .name("Spring Boot Basics").description("Demo course for checkout test")
                    .requirements("Basic Java")
                    .hours(20)
                    .category(category)
                    .tenant(tenant)
                    .build();
            course = courseRepository.save(course);

            TrainingSession trainingSession = TrainingSession.builder()
                    .price(new BigDecimal("50.00"))
                    .availableSeats(10)
                    .minSeats(3)
                    .numberOfLectures(5)
                    .duration("2 weeks")
                    .requiredEquipment("Laptop")
                    .status(SessionStatus.UPCOMING)
                    .course(course)
                    .classRoom(classRoom)
                    .teacher(teacher)
                    .build();
            trainingSession = trainingSessionRepository.save(trainingSession);

            log.info("Demo seed ready: student email=test@gmail.com, username=teststudent, sessionId={}", trainingSession.getId());
            log.info("Use: curl -X POST http://localhost:8080/api/payments/initiate/{} -H 'X-User-Identifier: test@gmail.com'", trainingSession.getId());
        };
    }
}