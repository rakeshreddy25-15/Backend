package com.studyleague.controller;

import com.studyleague.model.Assignment;
import com.studyleague.model.Grade;
import com.studyleague.model.Submission;
import com.studyleague.model.User;
import com.studyleague.repository.AssignmentRepository;
import com.studyleague.repository.CourseRepository;
import com.studyleague.repository.GradeRepository;
import com.studyleague.repository.SubmissionRepository;
import com.studyleague.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
public class AssignmentsController {
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;

    private final com.studyleague.service.EmailService emailService;
    private final com.studyleague.service.NotificationService notificationService;

    public AssignmentsController(AssignmentRepository assignmentRepository, CourseRepository courseRepository, 
                               SubmissionRepository submissionRepository, GradeRepository gradeRepository, 
                               UserRepository userRepository, com.studyleague.service.EmailService emailService, com.studyleague.service.NotificationService notificationService) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.submissionRepository = submissionRepository;
        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public record CreateAssignment(@NotBlank String title, @NotBlank String description, @NotBlank String dueDate) {}
    public record GradeRequest(int score, int maxScore, String feedback) {}

    @PostMapping("/courses/{courseId}/assignments")
    public ResponseEntity<?> create(@PathVariable("courseId") String courseId, @RequestBody CreateAssignment req) {
        if (!courseRepository.existsById(courseId)) return ResponseEntity.notFound().build();
        
        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(req.title());
        assignment.setDescription(req.description());
        assignment.setDueDate(parseDueDate(req.dueDate()));
        assignment = assignmentRepository.save(assignment);
        // Notify all enrolled students by name/email if available (basic: by course enrollments)
        // For simplicity, notify the teacher too
        emailService.send("teacher@studyleague", "New assignment: " + assignment.getTitle(), "A new assignment has been created.");
        try {
            if (notificationService != null) {
                notificationService.notifyCourse(courseId, "New assignment: " + assignment.getTitle(), "A new assignment has been posted.");
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(assignmentDto(assignment));
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> getOne(@PathVariable("assignmentId") String assignmentId) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assignmentDto(assignmentOpt.get()));
    }

    @GetMapping("/courses/{courseId}/assignments")
    public List<Map<String, Object>> byCourse(@PathVariable("courseId") String courseId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Assignment a : assignmentRepository.findByCourseId(courseId)) {
            out.add(assignmentDto(a));
        }
        return out;
    }

    @PostMapping(value = "/assignments/{assignmentId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submit(@PathVariable("assignmentId") String assignmentId,
                                    @RequestParam(value = "text", required = false) String text,
                                    @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setText(text);
        if (files != null) {
            for (MultipartFile f : files) {
                submission.getFileNames().add(f.getOriginalFilename());
            }
        }
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                submission.setStudentId(user.getId());
                submission.setStudentName(user.getName());
            }
        }
        if (submission.getStudentId() == null || submission.getStudentName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Authentication required to submit assignment"));
        }
        submission = submissionRepository.save(submission);
        return ResponseEntity.ok(Map.of("status", "submitted", "_id", submission.getId()));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<Map<String, Object>> submissions(@PathVariable("assignmentId") String assignmentId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Submission s : submissionRepository.findByAssignmentId(assignmentId)) {
            out.add(submissionDto(s));
        }
        return out;
    }

    @GetMapping("/courses/{courseId}/submissions")
    public List<Map<String, Object>> submissionsByCourse(@PathVariable("courseId") String courseId) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        if (assignments.isEmpty()) return out;
        List<String> assignmentIds = new ArrayList<>();
        for (Assignment a : assignments) assignmentIds.add(a.getId());
        for (Submission s : submissionRepository.findByAssignmentIdIn(assignmentIds)) {
            out.add(submissionDto(s));
        }
        return out;
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<?> grade(@PathVariable("submissionId") String submissionId, @RequestBody GradeRequest req) {
        Optional<Submission> submissionOpt = submissionRepository.findById(submissionId);
        if (submissionOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Submission submission = submissionOpt.get();
        Grade grade = new Grade();
        grade.setScore(req.score());
        grade.setMaxScore(req.maxScore());
        grade.setFeedback(req.feedback());
        grade.setSubmission(submission);
        grade = gradeRepository.save(grade);
        
        submission.setGrade(grade);
        submissionRepository.save(submission);

        // Notify student by email with score and feedback
        try {
            String studentId = submission.getStudentId();
            Optional<User> studentOpt = studentId == null ? Optional.empty() : userRepository.findById(studentId);
            Optional<Assignment> assignmentOpt = assignmentRepository.findById(submission.getAssignmentId());
            String assignmentTitle = assignmentOpt.map(Assignment::getTitle).orElse("Assignment");
            String subject = "Your assignment graded: " + assignmentTitle;
            StringBuilder body = new StringBuilder();
            body.append("Hello ")
                .append(submission.getStudentName() == null ? "Student" : submission.getStudentName())
                .append(",\n\n")
                .append("Your submission has been graded.\n")
                .append("Score: ").append(grade.getScore()).append("/").append(grade.getMaxScore()).append("\n");
            if (grade.getFeedback() != null && !grade.getFeedback().isBlank()) {
                body.append("Feedback: ").append(grade.getFeedback()).append("\n");
            }
            body.append("\nRegards,\nStudyLeague");
            studentOpt.ifPresent(student -> {
                emailService.send(student.getEmail(), subject, body.toString());
                try {
                    if (notificationService != null) {
                        notificationService.notifyUser(student.getId(), "Assignment graded: " + assignmentTitle, "Your submission has been graded.");
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) { }

        return ResponseEntity.ok(submissionDto(submission));
    }

    private static Map<String, Object> assignmentDto(Assignment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", a.getId());
        m.put("courseId", a.getCourseId());
        m.put("title", a.getTitle());
        m.put("description", a.getDescription());
        m.put("dueDate", a.getDueDate());
        return m;
    }

    private static Map<String, Object> submissionDto(Submission s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_id", s.getId());
        m.put("assignmentId", s.getAssignmentId());
        m.put("studentName", s.getStudentName());
        m.put("submittedAt", s.getSubmittedAt());
        m.put("fileNames", s.getFileNames());
        if (s.getGrade() != null) {
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("score", s.getGrade().getScore());
            g.put("maxScore", s.getGrade().getMaxScore());
            g.put("feedback", s.getGrade().getFeedback());
            m.put("grade", g);
        } else {
            m.put("grade", null);
        }
        return m;
    }

    private static Instant parseDueDate(String input) {
        if (input == null || input.isBlank()) {
            return Instant.now();
        }
        // Try full ISO-8601 instant first (e.g., 2025-10-24T12:27:00Z)
        try {
            return Instant.parse(input);
        } catch (DateTimeParseException ignored) { }
        // Try local datetime without seconds/timezone (e.g., 2025-10-24T12:27)
        try {
            LocalDateTime ldt = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) { }
        // Fallback: try ISO_LOCAL_DATE_TIME
        try {
            LocalDateTime ldt = LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            // As a last resort, use now
            return Instant.now();
        }
    }
}



