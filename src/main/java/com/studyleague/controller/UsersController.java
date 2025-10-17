package com.studyleague.controller;

import com.studyleague.model.Assignment;
import com.studyleague.model.Course;
import com.studyleague.model.Submission;
import com.studyleague.repository.AssignmentRepository;
import com.studyleague.repository.CourseRepository;
import com.studyleague.repository.SubmissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UsersController {
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public UsersController(SubmissionRepository submissionRepository,
                           AssignmentRepository assignmentRepository,
                           CourseRepository courseRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/{userId}/grades")
    public ResponseEntity<?> grades(@PathVariable("userId") String userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Submission s : submissionRepository.findByStudentId(userId)) {
            if (s.getGrade() != null) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("assignmentId", s.getAssignmentId());
                m.put("score", s.getGrade().getScore());
                m.put("maxScore", s.getGrade().getMaxScore());
                m.put("feedback", s.getGrade().getFeedback());

                // Resolve course title from assignment -> course
                Optional<Assignment> aOpt = assignmentRepository.findById(s.getAssignmentId());
                if (aOpt.isPresent()) {
                    Assignment a = aOpt.get();
                    m.put("assignmentTitle", a.getTitle());
                    Optional<Course> cOpt = courseRepository.findById(a.getCourseId());
                    cOpt.ifPresent(course -> {
                        m.put("courseId", course.getId());
                        m.put("courseTitle", course.getTitle());
                    });
                }

                out.add(m);
            }
        }
        return ResponseEntity.ok(out);
    }
}



