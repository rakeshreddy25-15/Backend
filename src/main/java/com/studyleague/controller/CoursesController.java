package com.studyleague.controller;

import com.studyleague.model.Course;
import com.studyleague.model.Material;
import com.studyleague.model.Enrollment;
import com.studyleague.model.User;
import com.studyleague.repository.CourseRepository;
import com.studyleague.repository.EnrollmentRepository;
import com.studyleague.repository.MaterialRepository;
import com.studyleague.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/courses")
public class CoursesController {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    private final com.studyleague.service.EmailService emailService;
    private final com.studyleague.service.NotificationService notificationService;

    public CoursesController(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, UserRepository userRepository, MaterialRepository materialRepository, com.studyleague.service.EmailService emailService, com.studyleague.service.NotificationService notificationService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public record CreateCourse(@NotBlank String title, @NotBlank String description, @NotBlank String duration,
                               String teacherId) {}

    public record CreateMaterial(@NotBlank String title, String description, String resourceUrl) {}

    @GetMapping
    public List<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String currentUserId = null;
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            currentUserId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Course c : courseRepository.findAll()) {
            Map<String, Object> m = courseDto(c);
            if (currentUserId != null) {
                boolean isEnrolled = enrollmentRepository.existsByCourseIdAndStudentId(c.getId(), currentUserId);
                m.put("enrolled", isEnrolled);
            }
            // Optional: include enrollment count for UI display
            int count = enrollmentRepository.findByCourseId(c.getId()).size();
            m.put("enrollmentCount", count);
            out.add(m);
        }
        return out;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(courseDto(courseOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCourse req, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Course course = new Course();
        course.setTitle(req.title());
        course.setDescription(req.description());
        course.setDuration(req.duration());
        String teacherId = req.teacherId();
        if ((teacherId == null || teacherId.isBlank()) && authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            teacherId = userId;
        }
        if (teacherId == null || teacherId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "teacherId is required"));
        }
        course.setTeacherId(teacherId);
        course = courseRepository.save(course);
        return ResponseEntity.ok(courseDto(course));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<?> enroll(@PathVariable("id") String id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        // naive: extract user by fake token if present, else pick any student
        User student = null;
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            student = userRepository.findById(userId).orElse(null);
        }
        if (student == null) {
            // fallback to any student in database
            student = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("STUDENT"))
                .findFirst().orElse(null);
        }
        
        if (student != null) {
            // Check if already enrolled
            if (enrollmentRepository.existsByCourseIdAndStudentId(id, student.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Already enrolled"));
            }
            
            Enrollment enrollment = new Enrollment();
            enrollment.setCourseId(id);
            enrollment.setStudentId(student.getId());
            enrollment.setStudentName(student.getName());
            enrollment.setStudentEmail(student.getEmail());
            enrollmentRepository.save(enrollment);
        }
        
        emailService.send("teacher@studyleague", "New enrollment", "A student has enrolled in your course.");
        return ResponseEntity.ok(Map.of("status", "enrolled"));
    }

    @GetMapping("/{id}/enrollments")
    public List<Map<String, Object>> enrollments(@PathVariable("id") String id) {
        List<Map<String, Object>> enrollments = new ArrayList<>();
        for (Enrollment e : enrollmentRepository.findByCourseId(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", e.getId());
            m.put("studentName", e.getStudentName());
            m.put("studentEmail", e.getStudentEmail());
            m.put("enrolledAt", e.getEnrolledAt());
            enrollments.add(m);
        }
        return enrollments;
    }

    private static Map<String, Object> courseDto(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", c.getId());
        m.put("title", c.getTitle());
        m.put("description", c.getDescription());
        m.put("duration", c.getDuration());
        m.put("teacherId", c.getTeacherId());
        return m;
    }

    @GetMapping("/{id}/materials")
    public List<Map<String, Object>> materials(@PathVariable("id") String id) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Material m : materialRepository.findByCourseIdOrderByCreatedAtDesc(id)) {
            Map<String, Object> mm = new LinkedHashMap<>();
            mm.put("_id", m.getId());
            mm.put("title", m.getTitle());
            mm.put("description", m.getDescription());
            mm.put("resourceUrl", m.getResourceUrl());
            mm.put("createdAt", m.getCreatedAt());
            out.add(mm);
        }
        return out;
    }

    @PostMapping("/{id}/materials")
    public ResponseEntity<?> addMaterial(@PathVariable("id") String id, @RequestBody CreateMaterial req,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Optional: only allow teacher (by token) to add materials
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            if (!userId.equals(courseOpt.get().getTeacherId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Only course teacher can add materials"));
            }
        }

        Material material = new Material();
        material.setCourseId(id);
        material.setTitle(req.title());
        material.setDescription(req.description());
        material.setResourceUrl(req.resourceUrl());
        material = materialRepository.save(material);

        // create in-app notifications for enrolled students (if service available)
        try {
            if (this.notificationService != null) {
                this.notificationService.notifyCourse(id, "New material: " + material.getTitle(), "A new material was added to your course.");
            }
        } catch (Exception ignored) {}

        Map<String, Object> mm = new LinkedHashMap<>();
        mm.put("_id", material.getId());
        mm.put("title", material.getTitle());
        mm.put("description", material.getDescription());
        mm.put("resourceUrl", material.getResourceUrl());
        mm.put("createdAt", material.getCreatedAt());
        return ResponseEntity.ok(mm);
    }
}



