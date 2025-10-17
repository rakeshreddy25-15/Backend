package com.studyleague.store;

import com.studyleague.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryStore {
    public final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    public final Map<String, User> usersById = new ConcurrentHashMap<>();
    public final Map<String, Course> coursesById = new ConcurrentHashMap<>();
    public final Map<String, Assignment> assignmentsById = new ConcurrentHashMap<>();
    public final Map<String, List<Assignment>> assignmentsByCourse = new ConcurrentHashMap<>();
    public final Map<String, List<Submission>> submissionsByAssignment = new ConcurrentHashMap<>();

    public InMemoryStore() {
        seed();
    }

    private void seed() {
        User teacher = new User();
        teacher.setName("Demo Teacher");
        teacher.setEmail("teacher@example.com");
        teacher.setPasswordHash(hash("password"));
        teacher.setRole(Role.TEACHER);
        usersByEmail.put(teacher.getEmail(), teacher);
        usersById.put(teacher.getId(), teacher);

        User student = new User();
        student.setName("Demo Student");
        student.setEmail("student@example.com");
        student.setPasswordHash(hash("password"));
        student.setRole(Role.STUDENT);
        usersByEmail.put(student.getEmail(), student);
        usersById.put(student.getId(), student);

        Course course = new Course();
        course.setTitle("Intro to StudyLeague");
        course.setDescription("Sample course");
        course.setDuration("4 weeks");
        course.setTeacherId(teacher.getId());
        coursesById.put(course.getId(), course);

        Assignment a1 = new Assignment();
        a1.setCourseId(course.getId());
        a1.setTitle("Getting Started");
        a1.setDescription("Warm-up assignment");
        a1.setDueDate(Instant.now().plus(14, ChronoUnit.DAYS));
        assignmentsById.put(a1.getId(), a1);
        assignmentsByCourse.computeIfAbsent(course.getId(), k -> new ArrayList<>()).add(a1);
    }

    public static String hash(String raw) {
        return Base64.getEncoder().encodeToString(("salt:" + raw).getBytes());
    }

    public boolean checkPassword(User user, String raw) {
        return Objects.equals(user.getPasswordHash(), hash(raw));
    }

    public List<Course> listCourses() {
        return new ArrayList<>(coursesById.values());
    }

    public List<Assignment> listAssignmentsByCourse(String courseId) {
        return assignmentsByCourse.getOrDefault(courseId, Collections.emptyList());
    }

    public List<Submission> listSubmissions(String assignmentId) {
        return submissionsByAssignment.getOrDefault(assignmentId, Collections.emptyList());
    }

    public List<Map<String, Object>> listEnrollments(String courseId) {
        Course course = coursesById.get(courseId);
        if (course == null) return Collections.emptyList();
        return course.getEnrollments().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", e.getId());
            m.put("studentName", e.getStudentName());
            m.put("studentEmail", e.getStudentEmail());
            m.put("enrolledAt", e.getEnrolledAt());
            return m;
        }).collect(Collectors.toList());
    }
}



