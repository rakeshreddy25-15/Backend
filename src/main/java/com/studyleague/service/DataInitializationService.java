package com.studyleague.service;

import com.studyleague.model.*;
import com.studyleague.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private GradeRepository gradeRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Only seed if no users exist
        if (userRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Create demo teacher
        User teacher = new User();
        teacher.setName("Demo Teacher");
        teacher.setEmail("teacher@example.com");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Create demo student
        User student = new User();
        student.setName("Demo Student");
        student.setEmail("student@example.com");
        student.setPasswordHash(passwordEncoder.encode("password"));
        student.setRole(Role.STUDENT);
        student = userRepository.save(student);

        // Create demo course
        Course course = new Course();
        course.setTitle("Intro to StudyLeague");
        course.setDescription("Sample course for demonstration");
        course.setDuration("4 weeks");
        course.setTeacherId(teacher.getId());
        course = courseRepository.save(course);

        // Create demo assignment
        Assignment assignment = new Assignment();
        assignment.setCourseId(course.getId());
        assignment.setTitle("Getting Started");
        assignment.setDescription("Warm-up assignment to get familiar with the platform");
        assignment.setDueDate(Instant.now().plus(14, ChronoUnit.DAYS));
        assignment = assignmentRepository.save(assignment);

        // Create demo enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(course.getId());
        enrollment.setStudentId(student.getId());
        enrollment.setStudentName(student.getName());
        enrollment.setStudentEmail(student.getEmail());
        enrollmentRepository.save(enrollment);

        System.out.println("Database seeded with demo data");
    }
}

